package org.summoners.http;

import java.io.*;

public class InputStreamWrapper extends InputStream {
	HttpClient client;
	InputStream in;
	long offset = 0;
	long endoff = -1;
	boolean acceptRanges = false;
	HttpResult res;

	/**
	 * @param res
	 */
	public InputStreamWrapper(HttpClient client, HttpResult res, long off, long endoff) {
		this.client = client;
		this.in = res.getInputStream();
		offset = off;
		this.endoff = endoff;
		this.res = res;
		for (String header : res.getHeaders()) {
			if (header.toLowerCase().trim().startsWith("accept-ranges:") && header.substring("accept-ranges:".length()).trim().equals("bytes")) {
				acceptRanges = true;
				break;
			}
		}
	}

	@Override
	public int read() throws IOException {
		byte[] b = new byte[1];
		int r = read(b);
		if (r == -1)
			return r;
		
		return b[0];
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		while (true) {
			try {
				int rd = in.read(b, off, len);
				offset += rd;
				return rd;
			} catch (IOException e) {
				int handle;
				if (!acceptRanges || client.getErrorHandler() == null || (handle = client.getErrorHandler().handle(e)) == -1) {
					throw e;
				}
				try {
					Thread.sleep(handle);
				} catch (InterruptedException ex) {
					throw new IOException("Interrupted exception while reading", e);
				}
				HttpResult r2 = client.get2(res.getURL(), offset, -1);
				in = r2.getInputStream();
			}
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
}