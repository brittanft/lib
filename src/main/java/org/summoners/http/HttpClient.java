package org.summoners.http;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * An HTTP client for making connections to URLs.
 * @author Xupwup
 */
public class HttpClient implements AutoCloseable {
	
	private Socket sock;
	private OutputStream os;
	private InputStream in;
	private String server;
	private int port = 80;
	private boolean close;
	private boolean closeConnection;
	public HttpResult lastResult;
	public boolean throwExceptionWhenNot200 = false;
	private ErrorHandler<Exception> errorHandler = null;
	
	public ErrorHandler<Exception> getErrorHandler() {
		return errorHandler;
	}

	/**
	 * Opens a connection to a server.
	 *
	 * @param server
	 *            for example "google.com". Must not include "http://"
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public HttpClient(String server) throws IOException {
		this(server, false);
	}

	/**
	 * Opens a connection to a server.
	 * 
	 * @param server
	 *            for example "google.com". Must not include "http://"
	 * @param closeConnection
	 *            whether the connection should be closed after every file
	 * @throws IOException
	 */
	public HttpClient(String server, boolean closeConnection) throws IOException {
		String[] sp = server.split(":");
		if (sp.length > 1) {
			port = Integer.parseInt(sp[1]);
		}
		this.server = sp[0];
		this.closeConnection = closeConnection;
		close = true;
	}

	private static void sendRequest(OutputStream os, String query, String... headers) throws IOException {
		os.write((query + "\r\n").getBytes());
		for (String hdr : headers) {
			os.write((hdr + "\r\n").getBytes());
		}
		os.write("\r\n".getBytes("ASCII"));
	}

	private void readEverything(InputStream in) throws IOException {
		byte[] bytes = new byte[1024];
		while (in.read(bytes) != -1) {
			// do nothing
		}
	}

	@Override
	public void close() throws IOException {
		if (sock != null) {
			sock.close();
		}
		close = true;
	}

	public void setErrorHandler(ErrorHandler<Exception> handler) {
		errorHandler = handler;
	}

	private void reopenSocket() throws IOException {
		boolean error;
		do {
			try {
				if (sock != null) {
					close();
				}
				sock = new Socket(server, port);
				in = new BufferedInputStream(sock.getInputStream());
				os = new BufferedOutputStream(sock.getOutputStream());
				error = false;
			} catch (IOException e) {
				int handle = -1;
				if (errorHandler == null || (handle = errorHandler.handle(e)) == -1) {
					throw e;
				}
				error = true;
				if (handle != -1) {
					try {
						Thread.sleep(handle);
					} catch (InterruptedException ex) {
						throw new IOException(ex);
					}
				}
			}
		} while (error);
	}

	public HttpResult get2(String url, long offset, long endOffset) throws IOException {
		boolean error = false;
		if (lastResult != null) {
			try {
				readEverything(lastResult.getInputStream()); // make sure everything is read,
												// so we dont read old data instead of headers
			} catch (IOException e) {
				close = true; // reopen the socket
			}
		}
		if (close || sock.isClosed()) {
			reopenSocket();
		}
		close = closeConnection;
		byte[] left = null;
		ArrayList<String> headers = new ArrayList<>();
		do {
			try {
				if (offset != -1) {
					String rangeStr = offset + "-";
					if (endOffset != -1) {
						rangeStr += endOffset;
					}
					sendRequest(os, "GET " + url + " HTTP/1.1", "Host: " + server, "Accept: text/html", "Content-Length: 0", "Connection: " + (closeConnection ? "close" : "keep-alive"), "User-Agent: rickHttpClient", "Accept: */*", "Range: bytes=" + rangeStr);
				} else {
					sendRequest(os, "GET " + url + " HTTP/1.1", "Host: " + server, "Accept: text/html", "Content-Length: 0", "Connection: " + (closeConnection ? "close" : "keep-alive"), "User-Agent: rickHttpClient", "Accept: */*");
				}
				os.flush();
				left = getHeaders(in, headers);
				error = false;
			} catch (IOException e) {
				int handle = -1;
				if (errorHandler == null || (handle = errorHandler.handle(e)) == -1) {
					throw e;
				}
				error = true;
				if (handle != -1) {
					try {
						Thread.sleep(handle);
					} catch (InterruptedException ex) {
						throw new IOException(ex);
					}
				}
				reopenSocket();
			}
		} while (error);
		boolean chunked = false;
		int length = -1;
		for (String header : headers) {
			String[] split = header.split(":");
			if (split[0].equalsIgnoreCase("Content-Length")) {
				length = Integer.parseInt(split[1].trim());
			}
			if (split[0].equalsIgnoreCase("Connection") && split[1].trim().equalsIgnoreCase("close")) {
				close = true;
			}
			if (split[0].equalsIgnoreCase("Transfer-Encoding") && split[1].trim().equalsIgnoreCase("chunked")) {
				chunked = true;
			}
		}
		if (close) {
			length = -1;
		}
		int status = Integer.parseInt(headers.get(0).split(" ")[1]);
		InputStream httpStream;
		if (chunked) {
			assert (length == -1);
			httpStream = new ChunkedInputStream(new HttpInputStream(left, in, length));
		} else {
			httpStream = new HttpInputStream(left, in, length);
		}
		HttpResult res = new HttpResult(headers, httpStream, status, url);
		if (!(offset == -1 && status == 200 || offset != -1 && status == 206) && throwExceptionWhenNot200) {
			throw new IOException(headers.get(0) + ", for url: " + url);
		}
		return res;
	}

	/**
	 * Issues a get request and flushes the last returned response object.
	 * 
	 * @param url
	 *            Relative urls only! For example "/test.html"
	 * @return HTTPResult object
	 * @throws IOException
	 */
	public HttpResult get(String url) throws IOException {
		HttpResult res = get2(url, -1, -1);
		lastResult = new HttpResult(res.getHeaders(), new InputStreamWrapper(this, res, 0, -1), res.getCode(), res.getURL());
		return lastResult;
	}

	public HttpResult get(String url, long start, long end) throws IOException {
		HttpResult res = get2(url, start, end);
		lastResult = new HttpResult(res.getHeaders(), new InputStreamWrapper(this, res, start, end), res.getCode(), res.getURL());
		return lastResult;
	}

	/**
	 * 
	 * @param in
	 * @param headers
	 * @return bytes it read that belong to the response body.
	 * @throws IOException
	 */
	public static byte[] getHeaders(InputStream in, ArrayList<String> headers) throws IOException {
		int left = 0;
		byte[] buffer = new byte[2048];
		byte[] obuffer = new byte[2048];
		int read;
		while ((read = in.read(buffer, left, buffer.length - left)) != -1) {
			int idx = getNewlineInByteArray(buffer, 0, read + left);
			if (idx == -1 && read + left == buffer.length) {
				throw new IOException("Header line > " + buffer.length);
			}
			int start = 0;
			while (idx != -1) {
				if (idx == start) {
					start = idx + 2;
					break;
				}
				
				headers.add(new String(buffer, start, idx - start));
				start = idx + 2;
				idx = getNewlineInByteArray(buffer, start, read + left);
			}
			left = read + left - start;
			System.arraycopy(buffer, start, obuffer, 0, left);
			byte[] h = buffer;
			buffer = obuffer;
			obuffer = h;
			if (idx + 2 == start) {
				break;
			}
		}
		return Arrays.copyOfRange(buffer, 0, left);
	}

	/**
	 * Gets the first occurrence of a newline in a bytearray.
	 * 
	 * @param in
	 *            the bytearray to search in
	 * @param o
	 *            offset
	 * @param max
	 *            the length of the array. (if this is less than in.length only the first 'max' elements will be considered)
	 * @return
	 */
	private static int getNewlineInByteArray(byte[] in, int o, int max) {
		byte r = "\r".getBytes()[0];
		byte n = "\n".getBytes()[0];
		for (int i = o; i < Math.min(max, in.length) - 1; i++) {
			if (in[i] == r && in[i + 1] == n) {
				return i;
			}
		}
		return -1;
	}
}