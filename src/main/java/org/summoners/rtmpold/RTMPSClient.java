package org.summoners.rtmpold;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.summoners.rtmp.data.*;
import org.summoners.rtmpold.encoding.*;

/**
 * A very basic RTMPS client
 * 
 * @author Gabriel Van Eyck
 */
public class RTMPSClient {
    private static char[] passphrase = "changeit".toCharArray();

    /** Server information */
    protected String server;
    protected int port;
    protected String app;
    protected String swfUrl;
    protected String pageUrl;

    /** Connection information */
    protected String DSId;

    /** Socket and streams */
    protected SSLSocket sslsocket;
    protected InputStream in;
    protected DataOutputStream out;
    protected RTMPPacketReader pr;

    /** State information */
    protected volatile boolean connected = false;
    protected volatile boolean reconnecting = false;
    protected int invokeID = 2;

    /** Used for generating handshake */
    protected Random rand = new Random();

    /** Encoder */
    protected AMF3Encoder aec = new AMF3Encoder();

    /** Pending invokes */
    protected Set<Integer> pendingInvokes = Collections.synchronizedSet(new HashSet<Integer>());

    /** Map of decoded packets */
    private Map<Integer, TypedObject> results = Collections.synchronizedMap(new HashMap<Integer, TypedObject>());

    /** Callback list */
    protected Map<Integer, RTMPCallback> callbacks = Collections.synchronizedMap(new HashMap<Integer, RTMPCallback>());

    /** Receive handler */
    protected volatile RTMPCallback receiveCallback = null;

    /**
     * A simple test for doing the basic RTMPS connection to Riot
     * 
     * @param args Unused
     */
    public static void main(String[] args) {
        RTMPSClient client = new RTMPSClient("prod.na2.lol.riotgames.com", 2099, "", "app:/mod_ser.dat", null);
        try {
            client.connect();
            if (client.isConnected())
                System.out.println("Success");
            else
                System.out.println("Failure");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        client.close();
    }

    /**
     * Basic constructor, need to use setConnectionInfo
     */
    public RTMPSClient() {
    }

    /**
     * Sets up the client with the given parameters
     * 
     * @param server The RTMPS server address
     * @param port The RTMPS server port
     * @param app The app to use in the connect call
     * @param swfUrl The swf URL to use in the connect call
     * @param pageUrl The page URL to use in the connect call
     */
    public RTMPSClient(String server, int port, String app, String swfUrl, String pageUrl) {
        setConnectionInfo(server, port, app, swfUrl, pageUrl);
    }

    /**
     * Sets up the client with the given parameters
     * 
     * @param server The RTMPS server address
     * @param port The RTMPS server port
     * @param app The app to use in the connect call
     * @param swfUrl The swf URL to use in the connect call
     * @param pageUrl The page URL to use in the connect call
     */
    public void setConnectionInfo(String server, int port, String app, String swfUrl, String pageUrl) {
        this.server = server;
        this.port = port;

        this.app = app;
        this.swfUrl = swfUrl;
        this.pageUrl = pageUrl;
    }

    /**
     * Wrapper for sleep
     * 
     * @param ms The time to sleep
     */
    protected void sleep(long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
        }
    }

    /**
     * Closes the connection
     */
    public void close() {
        connected = false;

        // We could join here, but should leave that to the programmer
        // Typically close should be preceded by a call to join if necessary

        try {
            if (sslsocket != null)
                sslsocket.close();
        }
        catch (IOException e) {
            // Do nothing
            // e.printStackTrace();
        }

        // Reset pending invokes and callbacks so this connection can be
        // restarted
        pendingInvokes = Collections.synchronizedSet(new HashSet<Integer>());
        callbacks = Collections.synchronizedMap(new HashMap<Integer, RTMPCallback>());
    }

    /**
     * Does a threaded reconnect
     */
    public void doReconnect() {
        if (reconnecting || !connected)
            return;

        Thread t = new Thread() {
            public void run() {
                reconnect();
            }
        };
        t.setName("RTMPSClient (reconnect)");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Attempts a reconnect (connect until success)
     */
    public void reconnect() {
        reconnecting = true;

        close();

        // Attempt reconnects every 5s
        while (!isConnected()) {
            try {
                connect();
            }
            catch (IOException e) {
                System.err.println("Error when reconnecting: ");
                e.printStackTrace(); // For debug purposes

                sleep(5000);
            }
        }

        reconnecting = false;
    }

    /**
     * Opens the socket with the default or a previously saved certificate
     * 
     * @return A special TrustManager to save the certificate if necessary
     * @throws IOException
     */
    private SavingTrustManager openSocketWithCert() throws IOException {
        try {
            // Load the default KeyStore or a saved one
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            File file = new File("certs/" + server + ".cert");
            if (!file.exists() || !file.isFile())
                file = new File(System.getProperty("java.home") + "/lib/security/cacerts");

            InputStream in = new FileInputStream(file);
            ks.load(in, passphrase);

            // Set up the socket factory with the KeyStore
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            X509TrustManager defaultTrustManager = (X509TrustManager)tmf.getTrustManagers()[0];
            SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
            context.init(null, new TrustManager[] { tm }, null);
            SSLSocketFactory factory = context.getSocketFactory();
            System.out.println("hit socket factory");
            sslsocket = (SSLSocket)factory.createSocket(server, port);

            return tm;
        }
        catch (Exception e) {
            // Hitting an exception here is very bad since we probably won't
            // recover
            // (unless it's a connectivity issue)

        	e.printStackTrace();
            // Rethrow as an IOException
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Downloads and installs a certificate if necessary
     * 
     * @throws IOException
     */
    private void getCertificate() throws IOException {
        try {
            SavingTrustManager tm = openSocketWithCert();

            // Try to handshake the socket
            boolean success = false;
            try {
                sslsocket.startHandshake();
                success = true;
            }
            catch (SSLException e) {
                sslsocket.close();
            }

            // If we failed to handshake, save the certificate we got and try
            // again
            if (!success) {
                // Set up the directory if needed
                File dir = new File("certs");
                if (!dir.isDirectory()) {
                    dir.delete();
                    dir.mkdir();
                }

                // Reload (default) KeyStore
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                File file = new File(System.getProperty("java.home") + "/lib/security/cacerts");

                InputStream in = new FileInputStream(file);
                ks.load(in, passphrase);

                // Add certificate
                X509Certificate[] chain = tm.chain;
                if (chain == null)
                    throw new Exception("Failed to obtain server certificate chain");

                X509Certificate cert = chain[0];
                String alias = server + "-1";
                ks.setCertificateEntry(alias, cert);

                // Save certificate
                OutputStream out = new FileOutputStream("certs/" + server + ".cert");
                ks.store(out, passphrase);
                out.close();
                System.out.println("Installed cert for " + server);
            }
        }
        catch (Exception e) {
            // Hitting an exception here is very bad since we probably won't
            // recover
            // (unless it's a connectivity issue)

            // Rethrow as an IOException
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Attempts to connect given the previous connection information
     * 
     * @throws IOException
     */
    public void connect() throws IOException {
        try {
            sslsocket = (SSLSocket)SSLSocketFactory.getDefault().createSocket(server, port);
            in = new BufferedInputStream(sslsocket.getInputStream());
            out = new DataOutputStream(sslsocket.getOutputStream());

            doHandshake();
        }
        catch (IOException e) {
            // If we failed to set up the socket, assume it's because we needed
            // a certificate
            getCertificate();
            // And use the certificate
            openSocketWithCert();

            // And try to handshake again
            in = new BufferedInputStream(sslsocket.getInputStream());
            out = new DataOutputStream(sslsocket.getOutputStream());

            doHandshake();
        }

        // Start reading responses
        pr = new RTMPPacketReader(in);

        // Handle preconnect Messages?
        // -- 02 | 00 00 00 | 00 00 05 | 06 00 00 00 00 | 00 03 D0 90 02

        // Connect
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("app", app);
        params.put("flashVer", "WIN 10,1,85,3");
        params.put("swfUrl", swfUrl);
        params.put("tcUrl", "rtmps://" + server + ":" + port);
        params.put("fpad", false);
        params.put("capabilities", 239);
        params.put("audioCodecs", 3191);
        params.put("videoCodecs", 252);
        params.put("videoFunction", 1);
        params.put("pageUrl", pageUrl);
        params.put("objectEncoding", 3);

        byte[] connect = aec.encodeConnect(params);
        System.out.println(Arrays.toString(connect));

        out.write(connect, 0, connect.length);
        out.flush();
        
    	System.out.println("Connection packet sent"); // 5

        while (!results.containsKey(1))
            sleep(10);
        TypedObject result = results.get(1);
        DSId = result.getTO("data").getString("id");

        connected = true;
    }

    /**
     * Executes a full RTMP handshake
     * 
     * @throws IOException
     */
    private void doHandshake() throws IOException {
        // C0
    	
    	System.out.println("establishing handshake");
        byte C0 = 0x03;
        out.write(C0);

        // C1
        long timestampC1 = System.currentTimeMillis();
        byte[] randC1 = new byte[1528];
        rand.nextBytes(randC1);

        out.writeInt((int)timestampC1);
        out.writeInt(0);
        out.write(randC1, 0, 1528);
        out.flush();

        // S0
        byte S0 = (byte)in.read();
        if (S0 != 0x03)
            throw new IOException("Server returned incorrect version in handshake: " + S0);

        // S1
        byte[] S1 = new byte[1536];
        in.read(S1, 0, 1536);

        // C2
        long timestampS1 = System.currentTimeMillis();
        out.write(S1, 0, 4);
        out.writeInt((int)timestampS1);
        out.write(S1, 8, 1528);
        out.flush();

        // S2
        byte[] S2 = new byte[1536];
        for (int i = 0; i < S2.length; i++)
            S2[i] = (byte)in.read();
        // in.read(S2, 0, 1536);

        // Validate handshake
        boolean valid = true;
        for (int i = 8; i < 1536; i++) {
            if (randC1[i - 8] != S2[i]) {
                valid = false;
                break;
            }
        }

        if (!valid)
            throw new IOException("Server returned invalid handshake");
    }

    /**
     * Invokes something
     * 
     * @param packet The packet completely setup just needing to be encoded
     * @return The invoke ID to use with getResult(), peekResult, and join()
     * @throws IOException
     */
    public synchronized int invoke(TypedObject packet) throws IOException {
        int id = nextInvokeID();
        pendingInvokes.add(id);
        System.out.println("INVOKED");
        try {
            byte[] data = aec.encodeInvoke(id, packet);
            out.write(data, 0, data.length);
            out.flush();

            return id;
        }
        catch (IOException e) {
            // Clear the pending invoke
            pendingInvokes.remove(id);

            // Rethrow
            throw e;
        }
    }    
    protected TypedObject handleResult(TypedObject result) throws IOException {
        if(result.get("result").equals("_error")) {
            System.err.println(result);
           // String reason = result.getExceptionMessage();
        }
        return result.getTO("data");
    }
    
    public TypedObject invoke2(String service, String method, Object arguments) throws IOException {
        try {
            return getResult(invoke(service, method, arguments));
        } catch(IOException e) {
        	e.printStackTrace();
        }
        return null;
    }
    
    protected TypedObject call(String method, String service, Object arguments) throws IOException {
        return handleResult(this.invoke2(service, method, arguments));
        /*try {
int id = _connection.getInternalRTMPSClient(0).invoke(getServiceName(), method, arguments);
TypedObject result = _connection.getInternalRTMPSClient(0).getResult(id);
return handleResult(result);
} catch(IOException ex) {
throw new LeagueException(LeagueErrorCode.NETWORK_ERROR, ex.getMessage());
}*/
    }

    /**
     * Invokes something
     * 
     * @param destination The destination
     * @param operation The operation
     * @param body The arguments
     * @return The invoke ID to use with getResult(), peekResult(), and join()
     * @throws IOException
     */
    public synchronized int invoke(String destination, Object operation, Object body) throws IOException {
        return invoke(wrapBody(body, destination, operation));
    }

    /**
     * Invokes something asynchronously
     * 
     * @param destination The destination
     * @param operation The operation
     * @param body The arguments
     * @param cb The callback that will receive the result
     * @return The invoke ID to use with getResult(), peekResult(), and join()
     * @throws IOException
     */
    public synchronized int invokeWithCallback(String destination, Object operation, Object body, RTMPCallback cb) throws IOException {
        callbacks.put(invokeID, cb); // Register the callback
        return invoke(destination, operation, body);
    }

    /**
     * Sets up a body in a full RemotingMessage with headers, etc.
     * 
     * @param body The body to wrap
     * @param destination The destination
     * @param operation The operation
     * @return
     */
    protected TypedObject wrapBody(Object body, String destination, Object operation) {
        TypedObject headers = new TypedObject();
        headers.put("DSRequestTimeout", 60);
        headers.put("DSId", DSId);
        headers.put("DSEndpoint", "my-rtmps");

        TypedObject ret = new TypedObject("flex.messaging.messages.RemotingMessage");
        ret.put("destination", destination);
        ret.put("operation", operation);
        ret.put("source", null);
        ret.put("timestamp", 0);
        ret.put("messageId", AMF3Encoder.randomUID());
        ret.put("timeToLive", 0);
        ret.put("clientId", null);
        ret.put("headers", headers);
        ret.put("body", body);

        return ret;
    }

    /**
     * Returns the next invoke ID to use
     * 
     * @return The next invoke ID
     */
    protected int nextInvokeID() {
        return invokeID++;
    }

    /**
     * Returns the connection status
     * 
     * @return True if connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Removes and returns a result for a given invoke ID if it's ready
     * Returns null otherwise
     * 
     * @param id The invoke ID
     * @return The invoke's result or null
     */
    public TypedObject peekResult(int id) {
        if (results.containsKey(id)) {
            TypedObject ret = results.remove(id);
            return ret;
        }
        return null;
    }

    /**
     * Blocks and waits for the invoke's result to be ready, then removes and
     * returns it
     * 
     * @param id The invoke ID
     * @return The invoke's result
     */
    public TypedObject getResult(int id) {
        while (connected && !results.containsKey(id)) {
            sleep(10);
        }

        if (!connected)
            return null;

        TypedObject ret = results.remove(id);
        return ret;
    }

    /**
     * Waits until all results have been returned
     */
    public void join() {
        while (!pendingInvokes.isEmpty()) {
            sleep(10);
        }
    }

    /**
     * Waits until the specified result returns
     */
    public void join(int id) {
        while (connected && pendingInvokes.contains(id)) {
            sleep(10);
        }
    }

    /**
     * Cancels an invoke and related callback if any
     * 
     * @param id The invoke ID to cancel
     */
    public void cancel(int id) {
        // Remove from pending invokes (only affects join())
        pendingInvokes.remove(id);

        // Check if we've already received the result
        if (peekResult(id) != null)
            return;
        // Signify a cancelled invoke by giving it a null callback
        else {
            callbacks.put(id, null);

            // Check for race condition
            if (peekResult(id) != null)
                callbacks.remove(id);
        }
    }

    /**
     * Sets the handler for receive packets (things like champ select)
     * 
     * @param cb The handler to use
     */
    public void setReceiveHandler(RTMPCallback cb) {
        receiveCallback = cb;
    }

    /**
     * Reads RTMP packets from a stream
     */
    public class RTMPPacketReader {
        /** The stream to read from */
        private BufferedInputStream in;

        /** The AMF3 decoder */
        private final AMF3Decoder adc = new AMF3Decoder();

        /**
         * Starts a packet reader on the given stream
         * 
         * @param stream The stream to read packets from
         */
        public RTMPPacketReader(InputStream stream) {
            this.in = new BufferedInputStream(stream, 16384);

            Thread curThread = new Thread() {
                public void run() {
                    parsePackets(this);
                }
            };
            curThread.setName("RTMPSClient (PacketReader)");
            curThread.setDaemon(true);
            curThread.start();
        }

        private byte readByte(InputStream in) throws IOException {
            byte ret = (byte)in.read();
            // System.out.println(String.format("%02X", ret));
            return ret;
        }

        /**
         * The main loop for the packet reader
         */
        private void parsePackets(Thread thread) {
            try {
                Map<Integer, Packet> packets = new HashMap<Integer, Packet>();

                while (true) {
                    // Parse the basic header
                	System.out.println("1: " + in.available()); //0
                	
                    byte basicHeader = readByte(in);
                	System.out.println("2: " + in.available()); //16

                    int channel = basicHeader & 0x2F;
                    int headerType = basicHeader & 0xC0;

                    int headerSize = 0;
                    if (headerType == 0x00)
                        headerSize = 12;
                    else if (headerType == 0x40)
                        headerSize = 8;
                    else if (headerType == 0x80)
                        headerSize = 4;
                    else if (headerType == 0xC0)
                        headerSize = 1;

                    //S: 12, H: 2, C: 2, T: 0
                    /*establishing handshake
						Connection packet sent
						2: 16
						S: 12, H: 2, C: 2, T: 0
						Type: 6, Size: 5
						3: 5
						Packet completed fucker
						Continuing
						250000
						2
						1: 0
						2: 234
						S: 12, H: 3, C: 3, T: 0
						Type: 20, Size: 222
						3: 223
						1: 95
						2: 94
						S: 1, H: -61, C: 3, T: 192
						3: 94
						Packet completed fucker
						Continuing
						Object decoded: {result=_result, invokeId=1, serviceCall=null, data={DSMessagingVersion=1, code=NetConnection.Connect.Success, level=status, description=Connection succeeded., details=null, id=ACC6603B-801C-E2B3-B114-C7399C7B0329, objectEncoding=3, }, }
						1: 0
						Success
						Success
						*/
                    System.out.println("S: " + headerSize + ", H: " + basicHeader + ", C: " + channel + ", T: " + headerType);
                    // Retrieve the packet or make a new one
                    if (!packets.containsKey(channel))
                        packets.put(channel, new Packet());
                    Packet p = packets.get(channel);

                    // Parse the full header
                    if (headerSize > 1) {
                        byte[] header = new byte[headerSize - 1];
                        for (int i = 0; i < header.length; i++)
                            header[i] = readByte(in);

            			
            			System.out.println("Data: " + Arrays.toString(header));
                        if (headerSize >= 8) {
                            int size = 0;
                            for (int i = 3; i < 6; i++)
                                size = size * 256 + (header[i] & 0xFF);
                            p.setSize(size);

                            p.setType(header[6]);
                        }
                        
                        System.out.println("Type: " + p.getType() + ", Size: "+ p.getSize());
                    }
                	System.out.println("3: " + in.available()); // 5

                    // Read rest of packet
                    for (int i = 0; i < 128; i++) {
                        byte b = readByte(in);
                        p.add(b);

                        if (p.isComplete()) {
                        	System.out.println("Packet completed fucker");
                        	break;
                        }
                    }

                    // Continue reading if we didn't complete a packet
                    if (!p.isComplete())
                        continue;
                    
                    System.out.println("Continuing");

                    // Remove the read packet
                    packets.remove(channel);

                    // Decode result
                    final TypedObject result;
                    if (p.getType() == 0x14) // Connect
                        result = adc.decodeConnect(p.getData());
                    else if (p.getType() == 0x11) // Invoke
                        result = adc.decodeInvoke(p.getData());
                    else if (p.getType() == 0x06) // Set peer bandwidth
                    {
                        byte[] data = p.getData();
                        int windowSize = 0;
                        for (int i = 0; i < 4; i++)
                            windowSize = windowSize * 256 + (data[i] & 0xFF);
                        
                        System.out.println(windowSize);
                        int type = data[4];
                        System.out.println(type);
                        continue;
                    }
                    else if (p.getType() == 0x03) // Ack
                    {
                        byte[] data = p.getData();
                        int ackSize = 0;
                        for (int i = 0; i < 4; i++)
                            ackSize = ackSize * 256 + (data[i] & 0xFF);
                        continue;
                    }
                    else
                    // Skip most messages
                    {
                        System.out.println("Unrecognized message type");
                        System.out.print(String.format("%02X ", p.getType()));
                        for (byte b : p.getData())
                            System.out.print(String.format("%02X", b & 0xff));
                        System.out.println();
                        continue;
                    }
                    
                	System.out.println("Object decoded: " + result); // 5

                    // Store result
                    Integer id = result.getInt("invokeId");

                    // Receive handler
                    if (id == null || id == 0) {
                        if (receiveCallback != null)
                            receiveCallback.callback(result);
                    }
                    // Callback handler
                    else if (callbacks.containsKey(id)) {
                        final RTMPCallback cb = callbacks.remove(id);
                        if (cb != null) {
                            // Thread the callback so it doesn't hang us
                            Thread t = new Thread() {
                                public void run() {
                                    cb.callback(result);
                                }
                            };
                            t.setName("RTMPSClient (Callback-" + id + ")");
                            t.start();
                        }
                    }
                    else {
                        results.put(id, result);
                    }
                    pendingInvokes.remove(id);
                }
            }
            catch (IOException e) {
                if (!reconnecting && connected) {
                    System.out.println("Error while reading from stream");
                    e.printStackTrace();
                }
            }

            // Attempt to reconnect if this was an unintentional disconnect
            if (!reconnecting && connected) {
                doReconnect();
            }
        }
    }
}
