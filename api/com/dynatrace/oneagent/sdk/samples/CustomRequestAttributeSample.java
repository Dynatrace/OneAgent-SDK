package com.dynatrace.oneagent.sdk.samples;

import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.LoggingCallback;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;

/**
 * This sample shows the usage of the SDK to record custom request attribute values (SCAVs).
 */
public class CustomRequestAttributeSample {

	private static OneAgentSDK oneAgentSDK;

	private static class ConsoleLogger implements LoggingCallback {

		@Override
		public void warn(String message) {
			System.out.println("[Warning] OneAgent SDK: " + message);
		}

		@Override
		public void error(String message) {
			System.out.println("[Error] OneAgent SDK: " + message);
		}
		
	}
	
	public static void main(String[] args) {
		oneAgentSDK = OneAgentSDKFactory.createInstance();

		oneAgentSDK.setLoggingCallback(new ConsoleLogger());
		
		oneAgentSDK.addCustomRequestAttribute("url", "http://www.dynatrace.com");
		oneAgentSDK.addCustomRequestAttribute("requestCount", 400);
		
		oneAgentSDK.addCustomRequestAttribute("billingAmount", 34.99);
		oneAgentSDK.addCustomRequestAttribute("billingAmount", "unlimited");
		
	}

}
