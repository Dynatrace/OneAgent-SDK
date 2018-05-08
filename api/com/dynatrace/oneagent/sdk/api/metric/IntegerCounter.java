package com.dynatrace.oneagent.sdk.api.metric;

public interface IntegerCounter extends Metric {

	/**
	 * increase the counter by provided value
	 * 
	 * @param by
	 *            value
	 * @param dimension
	 *            optional dimension. e. g. name of the concerned resource (disk
	 *            name, page name, ...). Parameter is optional - set to null if no
	 *            dimension should be reported.
	 */
	void increaseBy(long by, String dimension);

}
