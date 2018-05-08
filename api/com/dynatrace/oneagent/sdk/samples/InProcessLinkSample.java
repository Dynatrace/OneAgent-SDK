package com.dynatrace.oneagent.sdk.samples;

import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.InProcessLinkTracer;
import com.dynatrace.oneagent.sdk.api.InProcessLink;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;

/**
 * This sample shows the usage of the SDK for tracing in-process links (e.g. asynchronous code execution).
 */
public class InProcessLinkSample {

	private static OneAgentSDK oneAgentSDK;
	private static MyThreadPool threadPool = new MyThreadPool();
	
	private static class MyThreadPool {
		public void submit(Task task) {
			
		}
	}
	
	private interface Task {
		public void myRun();
	}

	public static void main(String[] args) {
		oneAgentSDK = OneAgentSDKFactory.createInstance();
		doInProcessLinking();
	}


	public static void doInProcessLinking() {
		InProcessLink inProcessLink = oneAgentSDK.createInProcessLink();
		threadPool.submit(new Task() {
			@Override
			public void myRun() {
				
				InProcessLinkTracer inProcessLinkTracer = oneAgentSDK.traceInProcessLink(inProcessLink);
				inProcessLinkTracer.start();
				try {
					doTheWork();
				} catch (Exception e) {
					inProcessLinkTracer.error(e.getMessage());
				} finally {
					inProcessLinkTracer.end();
				}
			}
			
			private void doTheWork() {
				
			}
			
		});
	}
}
