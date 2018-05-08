package com.dynatrace.oneagent.sdk.api;

/**
 * Common interface for outgoing requests which include tagging. Not to be directly used by SDK user.
 */
public interface OutgoingTaggable {

	/**
	 * Returns the string format of the outgoing tag for this tracer. This tracer
	 * must be already started (see {@link Tracer#start()}) .

	 * @return the tag. used with {@link IncomingTaggable#setDynatraceStringTag(String)}.
	 */
	String getDynatraceStringTag();

	/**
	 * same as {@link #getDynatraceStringTag()} but returning the tag in binary format.
	 * 
	 * @return the tag. used with {@link IncomingTaggable#setDynatraceByteTag(byte[])}.
	 */
	byte[] getDynatraceByteTag();

}