package com.dynatrace.oneagent.sdk.api.enums;

/**
 * Provides more details about the OneAgent used by the SDK.
 */
public interface OneAgentInfo {

	/**
	 * Checks if agent is available. 
	 *  
	 * @return true if agent has been found - even it is not compatible with this SDK version.
	 */
	boolean agentFound();

	/**
	 * Checks if found agent is compatible with this SDK version.
	 * @return true if agent is available and compatible. False in any other case.
	 */
	boolean agentCompatible();
	
	/**
	 * @return Version of agent found. Null in case of no agent or agent doesn't support reporting the version.  
	 */
	String version();

}
