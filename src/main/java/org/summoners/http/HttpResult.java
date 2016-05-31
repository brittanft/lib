package org.summoners.http;

import java.io.*;
import java.util.*;

/**
 * The resulting data from an HTTP connection.
 * @author Xupwup
 */
public class HttpResult {

	/**
	 * Instantiates a new HTTP result.
	 *
	 * @param headers
	 *            the headers featured in this HTTP result
	 * @param inputStream
	 *            the input stream loaded from the HTTP client
	 * @param code
	 *            the opcode received by the HTTP client
	 * @param url
	 *            the url this result was taken from
	 */
	public HttpResult(List<String> headers, InputStream inputStream, int code, String url) {
		this.headers = headers;
		this.inputStream = inputStream;
		this.code = code;
		this.url = url;
	}
	
	/**
	 * The headers featured in this HTTP result.
	 */
	private final List<String> headers;
	
	/**
	 * Gets the headers featured in this HTTP result.
	 *
	 * @return the headers featured in this HTTP result
	 */
	public List<String> getHeaders() {
		return headers;
	}
	
	/**
	 * The input stream loaded from the HTTP client.
	 */
	private final InputStream inputStream;
	
	/**
	 * Gets the input stream loaded from the HTTP client.
	 *
	 * @return the input stream loaded from the HTTP client
	 */
	public InputStream getInputStream() {
		return inputStream;
	}
	
	/**
	 * The opcode received by the HTTP client.
	 */
	private final int code;
	
	/**
	 * Gets the opcode received by the HTTP client.
	 *
	 * @return the opcode received by the HTTP client
	 */
	public int getCode() {
		return code;
	}
	
	/**
	 * The url this result was taken from.
	 */
	private String url;
	
	/**
	 * Gets the url this result was taken from.
	 *
	 * @return the url this result was taken from
	 */
	public String getURL() {
		return url;
	}
	
	/**
	 * Sets the url this result was taken from.
	 *
	 * @param url
	 *            the new url this result was taken from
	 */
	public void setURL(String url) {
		this.url = url;
	}
}
