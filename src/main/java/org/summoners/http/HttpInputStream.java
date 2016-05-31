package org.summoners.http;

import java.io.*;

public class HttpInputStream extends InputStream {
	final byte[] left;
	final long length;
	long alreadyRead = 0;
	final InputStream actual;

	public HttpInputStream(byte[] left, InputStream actual, int length) {
		this.left = left;
		this.actual = actual;
		this.length = length;
	}

	@Override
	public int read() throws IOException {
		if (alreadyRead >= length && length != -1)
			return -1;
		
		int read = actual.read();
		if (alreadyRead < left.length)
			read = left[(int) alreadyRead];
		
		alreadyRead++;
		return read;
	}

	@Override
	public int read(byte[] bytes, int offset, int count) throws IOException {
		// this function reads from the buffer "left" first, then just reads
		// from the inputstream. (when getting headers the get function will likely have
		// read too much, data that was supposed to be the response body)
		if (alreadyRead >= length && length != -1)
			return -1;
		
		count = Math.min(count, bytes.length - offset);
		if (alreadyRead < left.length) {
			if (length == -1) {
				count = (int) Math.min(count, left.length - alreadyRead);
			} else {
				count = (int) Math.min(count, Math.min(length, left.length) - alreadyRead);
			}
			System.arraycopy(left, (int) alreadyRead, bytes, offset, count);
		} else {
			if (length != -1) {
				count = (int) Math.min(count, length - alreadyRead);
			}
			count = actual.read(bytes, offset, count);
		}
		alreadyRead += count;
		return count;
	}

	@Override
	public int read(byte[] bytes) throws IOException {
		return read(bytes, 0, bytes.length);
	}
}