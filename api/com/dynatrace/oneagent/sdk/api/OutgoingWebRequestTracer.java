package com.dynatrace.oneagent.sdk.api;

public interface OutgoingWebRequestTracer extends Tracer, OutgoingTaggable {

	/**
	 * All HTTP request headers should be provided to this method. Selective capturing will be done based on sensor configuration.
	 *
	 * @param name		HTTP request header field name
	 * @param value		HTTP request header field value
	 */
	void addRequestHeader(String name, String value);

	/**
	 * All HTTP response headers returned by the server should be provided to this method. Selective capturing will 
	 * be done based on sensor configuration.
	 *
	 * @param name		HTTP response header field name
	 * @param value		HTTP response header field value
	 */
	void addResponseHeader(String name, String value);

	/**
	 * Sets the HTTP response status code.
	 *
	 * @param statusCode		HTTP status code retrieved from server
	 */
	void setStatusCode(int statusCode);

}