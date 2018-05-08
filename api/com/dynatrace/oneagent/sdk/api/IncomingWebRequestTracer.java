package com.dynatrace.oneagent.sdk.api;

/**
 * Interface for incoming webrequest tracer.
 * <a href="https://github.com/Dynatrace/OneAgent-SDK#webrequests">https://github.com/Dynatrace/OneAgent-SDK#webrequests</a>
 */
public interface IncomingWebRequestTracer extends Tracer, IncomingTaggable {

	/**
	 * Validates and sets the remote IP address of the incoming web request. This information is very useful to gain information about 
	 * Load balancers, Proxies and ultimately the end user that is sending the request.
	 *
	 * <p>This function can not be used after the tracer was started.
	 * 
	 * <p> The remote address is the peer address of the socket connection via which the request was received. In case one or more proxies
	 * are used, this will be the address of the last proxy in the proxy chain. To enable the agent to determine the client IP address
	 * (=the address where the request originated), an application should also call {@link #addRequestHeader(String, String)} to add 
	 * any HTTP request headers.
	 * 
	 * @param remoteAddress		remote IP address. if null, remoteAddress will reset any value that was set previously.
	 */
	void setRemoteAddress(String remoteAddress);

	/**
	 * All HTTP request headers should be provided to this method. Selective capturing will be done based on sensor configuration.
	 * 
     * <p>If an HTTP request contains multiple header lines with the same header name, an application should call this function once per
     * line. Alternatively, depending on the header, the application can call this function once per header name, with an appropriately
     * concatenated header value.
     * 
	 * <p>This function can not be used after the tracer was started.
	 *
	 * @param name		HTTP request header field name
	 * @param value		HTTP request header field value
	 */
	void addRequestHeader(String name, String value);

	/**
	 * All HTTP POST parameters should be provided to this method. Selective capturing will be done based on sensor configuration.
	 *
	 * @param name		HTTP parameter name
	 * @param value		HTTP parameter value
	 */
	void addParameter(String name, String value);

	/**
	 * All HTTP response headers should be provided to this method. Selective capturing will be done based on sensor configuration.
	 * 
	 * <p>If the HTTP response contains multiple header lines with the same header name, an application should call this function once per
	 * line. Alternatively, depending on the header, the application can call this function once per header name, with an appropriately
	 * concatenated header value.
	 *
	 * @param name		HTTP response header field name
	 * @param value		HTTP response header field value
	 */
	void addResponseHeader(String name, String value);

	/**
	 * Sets the HTTP status code for an incoming web request.
	 * 
	 * @param statusCode		The HTTP status code of the response sent to the client.
	 */
	void setStatusCode(int statusCode);

}
