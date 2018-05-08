package com.dynatrace.oneagent.sdk.api;

/**
 * Logging-Callback gets called only inside a OneAgentSDK API call when
 * error/warning has occurred.
 * <p>
 * Never call any SDK API, when inside one of this callback methods.
 */
public interface LoggingCallback {

	/**
	 * Just warning. Something is missing, but agent is working normal.
	 *
	 * @param message
	 *            message text. never null.
	 */
	void warn(String message);

	/**
	 * Something that should be done can't be done. (e. g. path couldn't be started)
	 *
	 * @param message
	 *            message text. never null.
	 */
	void error(String message);

}
