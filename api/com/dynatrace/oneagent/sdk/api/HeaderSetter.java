package com.dynatrace.oneagent.sdk.api;

/**
 * This interface is used for setting headers (key-value pairs)
 */
public interface HeaderSetter<Carrier> {
	/**
	 * Sets the value for the specified name. If a header with this name already exists, the value is overwritten.
	 * @param name a valid HTTP header name, never null or empty
	 * @param value the header value to be set, never null
	 * @param carrier the header carrier (i.e., the web request object or its map of headers),
	 *                forwarded as it is passed to the calling method (could be null therefore)
	 */
  void setHeader(String name, String value, Carrier carrier);
}
