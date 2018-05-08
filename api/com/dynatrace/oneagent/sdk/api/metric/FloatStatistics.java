package com.dynatrace.oneagent.sdk.api.metric;

public interface FloatStatistics extends Metric {

	/**
	 * Adds new value to this statistics metric.
	 * 
	 * @param value
	 *            the sample to be added
	 * @param dimension
	 *            optional dimension. e. g. name of the concerned resource (disk
	 *            name, page name, ...). Parameter is optional - set to null if 
	 *            no dimension should be reported.
	 */
	void addValue(double value, String dimension);

}
