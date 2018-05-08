package com.dynatrace.oneagent.sdk.api.metric;

interface Metric {

	/**
	 * closes this metric reporter instance and releases resources therefore.
	 * reporting on an this instance is forbidden after this method has been called.
	 */
	void release();
}
