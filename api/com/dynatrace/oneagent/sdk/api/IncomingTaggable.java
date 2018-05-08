package com.dynatrace.oneagent.sdk.api;

/**
 * Common interface for incoming requests which include linking. Not to be
 * directly used by SDK user.
 */
public interface IncomingTaggable {

	/**
	 * Sets the tag using the string format.
	 * 
	 * An application can call this function to set the incoming tag of an "incoming
	 * taggable" tracer using the string representation. An "incoming taggable"
	 * tracer has one tag. Calling this method more than once, will overwrite any
	 * tag that was set by either {@link #setDynatraceByteTag(byte[])} or
	 * {@link #setDynatraceStringTag(String)}.
	 * 
	 * <p>
	 * This function can not be used after the tracer was started.
	 * 
	 * @param tag
	 *            if null or an empty string, the incoming tag will be reset
	 *            (cleared).
	 * 
	 */
	void setDynatraceStringTag(String tag);

	/**
	 * Same as {@link #setDynatraceStringTag(String)}, but tag is provided in binary
	 * format.
	 * 
	 * @param tag
	 *            if null or an empty string, the incoming tag will be reset
	 *            (cleared).
	 */
	void setDynatraceByteTag(byte[] tag);

}