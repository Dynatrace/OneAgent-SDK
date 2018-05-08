package com.dynatrace.oneagent.sdk.api;

/**
 * Common interface for Tracers able to allow incoming tags after they have been started. Not to be
 * directly used by SDK user.
 */
public interface Joinable {

	/**
	 * Adds a joining tag from another trace using the string format.
	 * 
	 * An application can call this function to add a joining tag when this tracer
	 * has already been started. 
	 * 
	 * <p>
	 * This function can only be used after the tracer was started.
	 * 
	 * @param tag
	 *            if null or an empty string, call will be ignored.
	 * 
	 */
	void addJoiningDynatraceStringTag(String tag);

	/**
	 * Same as {@link #addJoiningDynatraceStringTag(String)}, but tag is provided in binary
	 * format.
	 * 
	 * @param tag
	 *            if null or an empty array, call will be ignored.
	 */
	void addJoiningDynatraceByteTag(byte[] tag);

}