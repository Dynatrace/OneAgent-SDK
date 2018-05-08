package com.dynatrace.oneagent.sdk.samples;

import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import com.dynatrace.oneagent.sdk.api.metric.IntegerCounter;
import com.dynatrace.oneagent.sdk.api.metric.IntegerGauge;
import com.dynatrace.oneagent.sdk.api.metric.IntegerStatistics;

/**
 * This sample shows the usage of the SDK to record metrics.
 */
public class MetricSample {

	private static OneAgentSDK oneAgentSDK;
	private static IntegerCounter diskWrittenBytes;
	private static IntegerGauge cpuTempCelsius;
	private static IntegerStatistics networkFramesizeBytes;

	
	public static void main(String args[]) {
		oneAgentSDK = OneAgentSDKFactory.createInstance();
		
		diskWrittenBytes = oneAgentSDK.createIntegerCounterMetric("disk.written.bytes");
		cpuTempCelsius = oneAgentSDK.createIntegerGaugeMetric("cpu.temperature.celsius");
		networkFramesizeBytes = oneAgentSDK.createIntegerStatisticsMetric("network.framesize.bytes");
	}

	// periodic sampling
	public static void onTimer() {
		cpuTempCelsius.setValue(getCurrentCpuTemperature(), "cpu0");
	}
	
	// event driven
	public static void onDiskWrite(byte[] blockWritten) {
		diskWrittenBytes.increaseBy(blockWritten.length, "partition0");
	}

	public static void onNetworkFrameSentOrReceived(byte[] frame) {
		networkFramesizeBytes.addValue(frame.length, "eth0");
	}
	
	public static void onNetworkShutdown() {
		networkFramesizeBytes.release();
	}

	private static long getCurrentCpuTemperature() {
		return 0;
	}
	
}
