package com.dynatrace.oneagent.sdk.api.infos;

/**
 * Provides information about a current PurePath node using the TraceContext
 * (Trace-Id, Span-Id) model as defined in https://www.w3.org/TR/trace-context.
 * The Span-Id represents the currently active PurePath node.
 * This Trace-Id and Span-Id information is not intended for tagging and context-propagation
 * scenarios and primarily designed for log-enrichment use cases.
 */
public interface TraceContextInfo {
	public static final String INVALID_TRACE_ID = "00000000000000000000000000000000";
	public static final String INVALID_SPAN_ID = "0000000000000000";

	/**
	 * @return True if the value returned by {@link getTraceId()} is not equal to
	 * {@link INVALID_TRACE_ID} and the value returned by {@link getSpanId}
	 * is not equal to {@link INVALID_SPAN_ID}.
	 */
	boolean isValid();

	/**
	 * @return The Trace-Id represented lower-case, hex-encoded string
	 * (see: https://tools.ietf.org/html/rfc4648#section-8).
	 * {@link INVALID_TRACE_ID} in case of:
	 * - No OneAgent is present.
	 * - OneAgent doesn't support reporting the trace id.
	 * - There is no currently active PurePath context.
	 */
	String getTraceId();

	/**
	 * @return The Span-Id represented as lower-case, hex-encoded string
	 * (see: https://tools.ietf.org/html/rfc4648#section-8).
	 * {@link INVALID_SPAN_ID} in case of:
	 * - No OneAgent is present.
	 * - OneAgent doesn't support reporting the span id.
	 * - There is no currently active PurePath context.
	 */
	String getSpanId();
}
