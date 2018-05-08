package com.dynatrace.oneagent.sdk.api.metric;

public interface IntegerStatistics extends Metric {

	/**
	 * Adds new value to this statistics metric.
	 * 
	 * @param value
	 *            the value to be added
	 * @param dimension
	 *            optional dimension. e. g. name of the concerned resource (disk
	 *            name, page name, ...). Parameter is optional - set to null if 
	 *            no dimension should be reported.
	 */
	void addValue(long value, String dimension);

}
