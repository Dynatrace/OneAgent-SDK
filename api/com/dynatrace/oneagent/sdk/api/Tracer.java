package com.dynatrace.oneagent.sdk.api;

/**
 * Common interface for timing-related methods. Not to be directly used by SDK
 * user.
 */
public interface Tracer {

	/**
	 * Starts this Tracer. This will capture all entry fields of the Tracer and
	 * start the time measurement. Some entry fields must be set, before the Tracer
	 * is being started (eg.
	 * {@link IncomingWebRequestTracer#addRequestHeader(String, String)}). See
	 * documentation of corresponding field for details. In case no other
	 * restriction is documented, fields must be set prior calling {@link #end()}.
	 * {@link #start()} might only be called once per Tracer.
	 */
	void start();

	/**
	 * Sets error information for this traced operation. An application should call
	 * this function to notify a Tracer that the traced operations has failed (e. g.
	 * an Exception has been thrown).
	 * 
	 * {@link #error(String)} must only be called once. If a traced operation
	 * results in multiple errors and the application wants all of them to be
	 * captured, it must concatenate/combine them and then call
	 * {@link #error(String)} once before calling {@link #end()}.
	 * 
	 * @param message
	 *            error message(s)
	 */
	void error(String message);

	/**
	 * Ends this Tracer and stops time measurement. {@link #end()} might only be called
	 * once per Tracer.
	 */
	void end();

}