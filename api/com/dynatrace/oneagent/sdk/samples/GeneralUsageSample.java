package com.dynatrace.oneagent.sdk.samples;

import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import com.dynatrace.oneagent.sdk.api.infos.OneAgentInfo;

/**
 * This sample shows the general usage of the SDK:
 * - acquire OneAgentSDK instance
 * - check for current SDK state
 * - check agent version
 */
public class GeneralUsageSample {

	public static void main(String args[]) {
		
		// acquire OneAgentSDK instance (probably done once per application)
		OneAgentSDK oneAgentSDK = OneAgentSDKFactory.createInstance();

		System.out.println("== OneAgentSDK:");

		// after got instance: check for it's state 
		switch (oneAgentSDK.getCurrentState()) {
		case ACTIVE:
			// SDK is active and capturing
			break;
		case PERMANENTLY_INACTIVE:
			// SDK isn't active (e. g. no agent found; agent incompatible)
			// if possible: avoid using the SDK to save resources (CPU time, memory, ...)
			break;
		case TEMPORARILY_INACTIVE:
			// SDK isn't active now (e. g. capturing is disabled), but can change at any point of time to ACTIVE
			break;
		default:
			break;
		}

		// check for agent state:
		OneAgentInfo agentInfo = oneAgentSDK.getAgentInfo();
		if (agentInfo.agentFound()) {
			System.out.println("   Agent version: " + (agentInfo.version() == null ? "<not available>" : agentInfo.version()));
			System.out.println("   Agent is " + (agentInfo.agentCompatible() ? "": "*NOT*") + " compatible with OneAgentSDK");
		} else {
			System.out.println("   No agent found.");
		}
	}
	
}
