package com.dynatrace.oneagent.sdk.samples;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import com.dynatrace.oneagent.sdk.api.OutgoingWebRequestTracer;

public class OutgoingWebRequestSample {

	private static OneAgentSDK oneAgentSDK;

	public static void main(String[] args) {
		oneAgentSDK = OneAgentSDKFactory.createInstance();
		executeTracedHttpGetRequest("http://www.mycompany.com?q=myQuery");
	}
	
	private static void executeTracedHttpGetRequest(String url) {
		OutgoingWebRequestTracer outgoingWebRequestTracer = oneAgentSDK.traceOutgoingWebRequest(url, "GET");
		outgoingWebRequestTracer.start();
		try {
			Map<String, String> headerFields = new HashMap<>();

			// add the Dynatrace tag or W3C Trace Context (based on your configuration) to request headers to allow
			// the agent in the web server to link the request together for end-to-end tracing
			// Option 1: passing a stateful lambda, directly accessing 'headerFields'
			outgoingWebRequestTracer.injectTracingHeaders((key, value, _carrier) -> headerFields.put(key, value), null);
			// Option 2: passing a stateless implementation, which gets 'headerFields' passed as carrier
			outgoingWebRequestTracer.injectTracingHeaders((key, value, carrier) -> carrier.put(key, value), headerFields);

			outgoingWebRequestTracer.setStatusCode(executeHttpGetRequest(url, headerFields));
		} catch (Exception e) {
			outgoingWebRequestTracer.error(e.getMessage());
			// add application specific exception handling
		} finally {
			outgoingWebRequestTracer.end();
		}
	}
	
	private static int executeHttpGetRequest(String url, Map<String, String> headerFields) throws MalformedURLException, IOException {
		// forward to http client and return the status code
		HttpURLConnection connection = ((HttpURLConnection)new URL(url).openConnection());
		for (Entry<String, String> entry : headerFields.entrySet()) {
			connection.addRequestProperty(entry.getKey(), entry.getValue());
		}
		return connection.getResponseCode();
	}
}
