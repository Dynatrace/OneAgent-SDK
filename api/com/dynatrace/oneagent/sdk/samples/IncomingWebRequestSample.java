package com.dynatrace.oneagent.sdk.samples;

import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.IncomingWebRequestTracer;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import com.dynatrace.oneagent.sdk.api.infos.WebApplicationInfo;

/**
 * This sample shows the usage of the SDK for tracing incoming webrequests.
 */
public class IncomingWebRequestSample {

	static class KeyValuePair {

		String key;
		String value;

		public KeyValuePair(String key, String value) {
		}

	}

	static class MyWebServer {

		String name;
		String applicationID;
		String contextRoot;

		WebApplicationInfo webApplicationInfo;

		public MyWebServer(String name, String applicationID, String contextRoot) {
			this.name = name;
			this.applicationID = applicationID;
			this.contextRoot = contextRoot;

			webApplicationInfo = oneAgentSDK.createWebApplicationInfo(name, applicationID, contextRoot);
		}

		public void handleGETRequest(String url, KeyValuePair[] requestHeaders, KeyValuePair[] parameters, String clientIP) {
			IncomingWebRequestTracer webRequestTracer = oneAgentSDK.traceIncomingWebRequest(webApplicationInfo, url, "GET");

			for (KeyValuePair requestHeader : requestHeaders) {
				webRequestTracer.addRequestHeader(requestHeader.key, requestHeader.value);
			}

			webRequestTracer.start();
			try {
				/*
				 *	actual request/response handling
				 */
				KeyValuePair[] responseHeaders = new KeyValuePair[1];
				int statusCode = 200;

				for (KeyValuePair responseHeader : responseHeaders) {
					webRequestTracer.addRequestHeader(responseHeader.key, responseHeader.value);
				}
				webRequestTracer.setStatusCode(statusCode);
			} catch (Exception e) {
				webRequestTracer.error(e.getMessage());
			} finally {
				webRequestTracer.end();
			}
		}

		public void handlePOSTRequest(String url, KeyValuePair[] requestHeaders, KeyValuePair[] parameters, String clientIP) {
			IncomingWebRequestTracer webRequestTracer = oneAgentSDK.traceIncomingWebRequest(webApplicationInfo, url, "POST");

			for (KeyValuePair requestHeader : requestHeaders) {
				webRequestTracer.addRequestHeader(requestHeader.key, requestHeader.value);
			}
			for (KeyValuePair parameter : parameters) {
				webRequestTracer.addParameter(parameter.key, parameter.value);
			}

			webRequestTracer.start();
			try {
				/*
				 *	actual request/response handling
				 */
				KeyValuePair[] responseHeaders = new KeyValuePair[1];
				int statusCode = 200;

				for (KeyValuePair responseHeader : responseHeaders) {
					webRequestTracer.addRequestHeader(responseHeader.key, responseHeader.value);
				}
				webRequestTracer.setStatusCode(statusCode);
			} catch (Exception e) {
				webRequestTracer.error(e.getMessage());
			} finally {
				webRequestTracer.end();
			}
		}

	}

	private static OneAgentSDK oneAgentSDK;

	public static void main(String[] args) {
		oneAgentSDK = OneAgentSDKFactory.createInstance();

	}

}
