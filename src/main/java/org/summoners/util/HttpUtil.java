package org.summoners.util;

import java.net.*;

import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.summoners.math.*;

public class HttpUtil {

	public static CloseableHttpClient getDefaultClient() {
		return HttpClients.custom().setRetryHandler(new StandardHttpRequestRetryHandler()).build();
	}
	
	public static CloseableHttpResponse getResponse() {
		return null;
	}

	/**
	 * Builds an HTTP 'GET' request with the specified headers. <br>
	 * 	(assumes no specified range of bytes to read and a keep-alive connection)
	 *
	 * @param uri
	 *            the uri being requested from
	 * @param server
	 *            the server being contacted
	 * @return the http query request
	 */
	public static HttpGet getRequest(URI uri, String server) {
		return getRequest(uri, server, false, null);
	}

	/**
	 * Builds an HTTP 'GET' request with the specified headers. <br>
	 * 	(assumes no specified range of bytes to read)
	 *
	 * @param uri
	 *            the uri being requested from
	 * @param server
	 *            the server being contacted
	 * @param close
	 *            if the connection should close or keep alive
	 * @return the http query request
	 */
	public static HttpGet getRequest(URI uri, String server, boolean close) {
		return getRequest(uri, server, close, null);
	}
	
	/**
	 * Builds an HTTP 'GET' request with the specified headers.
	 *
	 * @param uri
	 *            the uri being requested from
	 * @param server
	 *            the server being contacted
	 * @param close
	 *            if the connection should close or keep alive
	 * @param range
	 *            the range of bytes to read, if available
	 * @return the http query request
	 */
	public static HttpGet getRequest(URI uri, String server, boolean close, Range2l range) {
		HttpGet request = new HttpGet(uri);
		request.addHeader("Host", server);
		request.addHeader("Accept", "text/html");
		request.addHeader("Content-Length", "0");
		request.addHeader("Connection", close ? "close" : "keep-alive");
		request.addHeader("User-Agent", "summonersHttpClient");
		request.addHeader("Accept", "*/*");
		if (range != null)
			request.addHeader("Range", "bytes=" + range.getMinimum() + "-" + (range.getMaximum() > range.getMinimum() ? range.getMaximum() : ""));
		
		return request;
	}

}
