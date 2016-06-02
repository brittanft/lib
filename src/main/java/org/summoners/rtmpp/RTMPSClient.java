package org.summoners.rtmpp;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.*;

import javax.net.ssl.*;

public class RTMPSClient implements AutoCloseable {
	
	private static final int READ_BUFFER_SIZE = 2000;
	private static final int WRITE_BUFFER_SIZE = 2000;
	private String server;
	private int port;
	private String app;
	private String swf;
	private String page;

	private ByteBuffer readBuf = ByteBuffer.allocateDirect(READ_BUFFER_SIZE);
	private ByteBuffer writeBuf = ByteBuffer.allocateDirect(WRITE_BUFFER_SIZE);
	
	public static void main(String[] args) {
		try (RTMPSClient client = new RTMPSClient("prod.na2.lol.riotgames.com", 2099, "", "app:/mod_ser.dat", null)) {
			client.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public RTMPSClient(String server, int port, String app, String swf, String page) {
		this.server = server;
		this.port = port;
		this.app = app;
		this.swf = swf;
		this.page = page;
	}
	
	private SSLSocket socket;
	private SocketChannel channel;
	private Selector selector;
	private Thread thread;
	private InputStream inputStream;
	private DataOutputStream outputStream;
	
	public void connect() throws IOException, InterruptedException {
		Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "Client connection initiated.");
		try {
			selector = Selector.open();
			socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(server, port);
			/*inputStream = new BufferedInputStream(socket.getInputStream());
			outputStream = new DataOutputStream(socket.getOutputStream());
			
			outputStream.write(0x03);
			outputStream.writeInt((int) System.currentTimeMillis());
			outputStream.writeInt(0);
	        byte[] randC1 = new byte[1528];
	        new Random().nextBytes(randC1);
	        outputStream.write(randC1, 0, 1528);
	        outputStream.flush();
	        
	        int version = inputStream.read();
	        System.out.println("version: " + version);*/
			socket.setUseClientMode(true);
			Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "Socket created.");
			channel = SocketChannel.open();
			Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "Channel opened.");
			channel.configureBlocking(false);
			channel.socket().setSendBufferSize(WRITE_BUFFER_SIZE);
			channel.socket().setReceiveBufferSize(READ_BUFFER_SIZE);
			//channel.socket().setKeepAlive(true);
			//channel.socket().setReuseAddress(true);
			//channel.socket().setSoTimeout(0);
			//channel.socket().setTcpNoDelay(true);
			//channel.socket().setSoLinger(false, 0);
			
			channel.connect(socket.getRemoteSocketAddress());
			channel.register(selector, SelectionKey.OP_CONNECT);
			
			thread = new Thread(() -> {
				while (channel.isOpen()) {
					try {
						if (selector.select() > 0)
						processSelectedKeys(selector.selectedKeys());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			thread.setDaemon(false);
			thread.start();
			Thread.sleep(3000L);

			ByteBuffer buf = ByteBuffer.allocate(1 + 4 + 4 + 1528);
			buf.put((byte) 0x03);
			buf.putInt((int) System.currentTimeMillis());
	        byte[] randC1 = new byte[1528];
	        new Random().nextBytes(randC1);
			buf.putInt(0);

	        
	        buf.put(randC1, 0, 1528);
	        
	        buf.flip();
	        
	        System.out.println("C1");
	        send(buf);
	        //channel.write(buf);
	        System.out.println("C1d");
	        
			//send(buf);
	        /*buf = ByteBuffer.allocate(1024);
	        
	        System.out.println(buf.remaining() + ", " + buf.position() + ", " + buf.capacity() + ", " + buf.limit());
	        int read = channel.read(buf);
	        System.out.println(buf.remaining() + ", " + buf.position() + ", " + buf.capacity() + ", " + buf.limit());
	        buf.put((byte) 0x03);
	        System.out.println(buf.remaining() + ", " + buf.position() + ", " + buf.capacity() + ", " + buf.limit());
	        buf.flip();
	        
	        System.out.println("R: "+ read + "V: " + buf.get());
	        */
			Thread.sleep(10000L);
		} catch (IOException ex) {
			Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, null, ex);
		} finally {
			channel.close();
			selector.close();
			Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "Client disconnected.");
		}
	}

	public void send(ByteBuffer buffer) throws InterruptedException, IOException {
		synchronized (writeBuf) {
			// try direct write of what's in the buffer to free up space
			if (writeBuf.remaining() < buffer.remaining()) {
				writeBuf.flip();
				int bytesOp = 0, bytesTotal = 0;
				while (writeBuf.hasRemaining() && (bytesOp = channel.write(writeBuf)) > 0)
					bytesTotal += bytesOp;
				writeBuf.compact();
			}
			// if didn't help, wait till some space appears
			if (Thread.currentThread().getId() != thread.getId()) {
				while (writeBuf.remaining() < buffer.remaining())
					writeBuf.wait();
			} else {
				if (writeBuf.remaining() < buffer.remaining())
					throw new IOException("send buffer full"); // TODO: add reallocation or buffers chain
			}
			writeBuf.put(buffer);
			// try direct write to decrease the latency
			writeBuf.flip();
			int bytesOp = 0, bytesTotal = 0;
			while (writeBuf.hasRemaining() && (bytesOp = channel.write(writeBuf)) > 0)
				bytesTotal += bytesOp;
			writeBuf.compact();
			if (writeBuf.hasRemaining()) {
				SelectionKey key = channel.keyFor(selector);
				key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
				selector.wakeup();
			}
		}
	}

	private void processSelectedKeys(Set<SelectionKey> keys) throws IOException {
		Iterator<SelectionKey> itr = keys.iterator();
		while (itr.hasNext()) {
			SelectionKey key = itr.next();
			System.out.println("LOL: " + key);
			if (key.isValid() && key.isReadable())
				processRead(key);
			if (key.isValid() && key.isWritable())
				processWrite(key);
			if (key.isValid() && key.isConnectable())
				processConnect(key);
			if (key.isValid() && key.isAcceptable())
				;
			itr.remove();
		}
	}
	
	private void processConnect(SelectionKey key) throws IOException {
		Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "Client init connection.");
		SocketChannel ch = (SocketChannel) key.channel();
		if (ch.finishConnect()) {
			Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "Client connected to: " + ch.getRemoteAddress());
			key.interestOps(key.interestOps() ^ SelectionKey.OP_CONNECT);
			key.interestOps(key.interestOps() | SelectionKey.OP_READ);
			onConnected();
		}
	}

	private void onConnected() {
		Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "Client connected.");
	}

	private void processWrite(SelectionKey key) throws IOException {
		Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "Client writing.");
		WritableByteChannel ch = (WritableByteChannel) key.channel();
		synchronized (writeBuf) {
			writeBuf.flip();
			
			int bytesOp = 0, bytesTotal = 0;
			while (writeBuf.hasRemaining() && (bytesOp = ch.write(writeBuf)) > 0) bytesTotal += bytesOp;
			
			if (writeBuf.remaining() == 0)
				key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
			
			if (bytesTotal > 0)
				writeBuf.notify();
			else if (bytesOp == -1) {
				Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "Peer closed write channel.");
				ch.close();
			}

			key.interestOps(key.interestOps() | SelectionKey.OP_READ);
			writeBuf.compact();
		}
		Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "Client finished writing.");
	}

	private void processRead(SelectionKey key) throws IOException {
		Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "Client reading.");
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Clear out our read buffer so it's ready for new data
		this.readBuf.clear();

		// Attempt to read off the channel
		int numRead;
		try {
			numRead = socketChannel.read(this.readBuf);
			Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "asdf123");
		} catch (IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			key.cancel();
			socketChannel.close();
			Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "asdf");
			return;
		}

		if (numRead == -1) {
			Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "asdf123333");
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			key.channel().close();
			key.cancel();
			return;
		}

		// Handle the response
		this.handleResponse(socketChannel, this.readBuf.array(), numRead);
		/*ReadableByteChannel ch = (ReadableByteChannel) key.channel();
		
		int bytesOp = 0, bytesTotal = 0;
		
		int read = ch.read(readBuf);
		System.out.println("Bytes read: " + read);
		while (readBuf.hasRemaining() && (bytesOp = ch.read(readBuf)) > 0) bytesTotal += bytesOp;
		
		if (bytesTotal > 0) {
			readBuf.flip();
			onRead();
			readBuf.compact();
		} else if (bytesOp == -1) {
			//Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "Peer closed read channel.");
			ch.close();
		}*/
	}

	private void handleResponse(SocketChannel socketChannel, byte[] array, int numRead) {
		Logger.getLogger(RTMPSClient.class.getName()).log(Level.INFO, "Response: " + numRead);
	}

	private void onRead() {
		int version = readBuf.get();
		System.out.println(version + "=" + (0x03));
	}

	private boolean connected;

	@Override
	public void close() throws Exception {
		connected = false;
	}
	
}
