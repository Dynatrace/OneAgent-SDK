package com.dynatrace.oneagent.sdk.samples;

import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.CustomServiceTracer;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This sample shows the usage of SDK for tracing custom services.
 */
public class CustomServiceSample {

	private static class PeriodicCleanupTask {

		public static void onTimer() {
			String serviceMethod = "onTimer";
			String serviceName = "PeriodicCleanupTask";
			CustomServiceTracer tracer = oneAgentSDK.traceCustomService(serviceMethod, serviceName);
			tracer.start();
			try {
				doMyCleanup();
			} catch (Exception e) {
				tracer.error(e.getMessage());
				throw e;
			} finally {
				tracer.end();
			}
		}

		private static void doMyCleanup() {
			System.out.println("Cleaning up...");
			// ...
			System.out.println("Done cleaning up.");
		}
	}

	private static OneAgentSDK oneAgentSDK;

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		oneAgentSDK = OneAgentSDKFactory.createInstance();
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(PeriodicCleanupTask::onTimer, 0, 10, TimeUnit.SECONDS).get();
	}
}
