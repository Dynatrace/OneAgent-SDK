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

	/**
	 * <p> Sets HTTP request headers required for linking requests end-to-end.
	 * <p> This method can only be called on an active tracer (i.e., between start and end).
	 *
	 * <p> Based on your configuration, this method will add the 'X-dynaTrace' header and/or the W3C Trace Context headers ('traceparent' and 'tracestate').<br>
	 * Therefore it is no longer necessary to manually add the Dynatrace tag and thus {@see #getDynatraceStringTag()}
	 * must not be used together with this method.
	 *
	 * <blockquote>Example usage:
	 * <pre>{@code
	 *Map<String, String> requestHeaderFields = new HashMap<>();
	 *outgoingWebRequestTracer.injectTracingHeaders((key, value, _carrier) -> requestHeaderFields.put(key, value), null);
	 * // or as a stateless implementation:
	 *outgoingWebRequestTracer.injectTracingHeaders((key, value, carrier) -> carrier.put(key, value), requestHeaderFields);
	 * }</pre></blockquote>
	 *
	 * @param headerSetter An implementation of {@see HeaderSetter} which sets the respective headers on the HTTP request.
	 * @param carrier The (nullable) header carrier object passed to {@code headerSetter} (i.e., the web request object or its map of headers)
	 */
	<Carrier> void injectTracingHeaders(HeaderSetter<Carrier> headerSetter, Carrier carrier);

}
