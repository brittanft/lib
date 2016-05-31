package org.summoners.http;

import java.io.*;
import java.util.*;

public class ChunkedInputStream extends InputStream {
	private final HttpInputStream in;
	int chunkRemaining = 0;
	boolean firstChunk = true;

	public ChunkedInputStream(HttpInputStream in) {
		this.in = in;
	}

	private String[] getChunkHeader() throws IOException {
		StringBuilder sb = new StringBuilder();
		boolean fi = false;
		while (true) {
			int read = in.read();
			if (read == -1) {
				throw new IOException("Connection closed.");
			}
			if (read == '\n' && fi)
				break;
			if (read != '\r') {
				sb.append((char) read);
			} else {
				fi = true;
			}
		}
		return sb.toString().split(";");
	}

	private void updateChunk() throws IOException {
		if (!firstChunk) { // if this is not the first chunk, skip over the CRLF
			int r = in.read();
			int n = in.read();
			if (r != '\r' || n != '\n') {
				throw new IOException("Invalid chunked encoding");
			}
		}
		firstChunk = false;
		String[] chunkHeader = getChunkHeader();
		chunkRemaining = Integer.parseInt(chunkHeader[0], 16);
		if (chunkRemaining == 0) {
			byte[] bytesLeft = HttpClient.getHeaders(in, new ArrayList<>());
			if (bytesLeft.length > 0) {
				throw new IOException("Invalid chunked encoding");
			}
			if (in.left.length > in.alreadyRead) {
				throw new IOException("Invalid chunked encoding");
			}
			chunkRemaining = -1;
		}
	}

	@Override
	public int read() throws IOException {
		if (chunkRemaining == 0) {
			updateChunk();
		}
		if (chunkRemaining == -1) {
			return -1;
		}
		chunkRemaining--;
		return in.read();
	}

	@Override
	public int read(byte[] bytes, int offset, int count) throws IOException {
		if (chunkRemaining == 0) {
			updateChunk();
		}
		if (chunkRemaining == -1) {
			return -1;
		}
		count = Math.min(count, chunkRemaining);
		int read = in.read(bytes, offset, count);
		chunkRemaining -= read;
		return read;
	}
}
