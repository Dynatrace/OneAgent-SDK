package com.dynatrace.oneagent.sdk.samples;

import java.util.HashMap;

import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.IncomingRemoteCallTracer;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import com.dynatrace.oneagent.sdk.api.OutgoingRemoteCallTracer;
import com.dynatrace.oneagent.sdk.api.enums.ChannelType;

/**
 * This sample shows the usage of the SDK for tracing incoming and outgoing remote calls.
 */
public class RemoteCallSample {

	static class RMIMethod {

		String methodName;
		String serviceName;
		String endPoint;
		String remoteHost;
		HashMap<String, String> additionalInfo = new HashMap<String, String>();

		public RMIMethod(String methodName, String serviceName, String endPoint, String remoteHost) {
			this.methodName = methodName;
			this.serviceName = serviceName;
			this.endPoint = endPoint;
			this.remoteHost = remoteHost;
		}

		public void addAdditionalInfo(String key, String value) {
			additionalInfo.put(key, value);
		}

		public String getAdditionalInfo(String key) {
			return additionalInfo.get(key);
		}

		// called at client side -> proxy
		public void call() {
			OutgoingRemoteCallTracer outgoingRemoteCallTracer = oneAgentSDK.traceOutgoingRemoteCall(methodName, serviceName, endPoint, ChannelType.TCP_IP, remoteHost);

			outgoingRemoteCallTracer.start();

			// set Dynatrace tag on remote method call
			addAdditionalInfo("X-Dynatrace", outgoingRemoteCallTracer.getDynatraceStringTag());
			try {
				/*
				 *	actual client-side code
				 */
			} catch (Exception e) {
				outgoingRemoteCallTracer.error(e.getMessage());
			} finally {
				outgoingRemoteCallTracer.end();
			}
		}

		// called at server side -> implementation
		public void execute() {
			IncomingRemoteCallTracer incomingRemoteCallTracer = oneAgentSDK.traceIncomingRemoteCall(methodName, serviceName, endPoint);

			// read Dynatrace tag from remote method call
			incomingRemoteCallTracer.setDynatraceStringTag(getAdditionalInfo("X-Dynatrace"));

			incomingRemoteCallTracer.start();
			try {
				/*
				 *	actual server-side code
				 */
			} catch (Exception e) {
				incomingRemoteCallTracer.error(e.getMessage());
			} finally {
				incomingRemoteCallTracer.end();
			}
		}

	}

	private static OneAgentSDK oneAgentSDK;

	public static void main(String[] args) {
		oneAgentSDK = OneAgentSDKFactory.createInstance();

		RMIMethod rmiMethod = new RMIMethod("remoteCall", "remoteService", "rmi://remoteService/remoteCall", "192.168.0.56:6000");

		doClientRemoteCall(rmiMethod);
		doServerRemoteCall(rmiMethod);
	}


	public static void doClientRemoteCall(RMIMethod rmiMethod) {
		// actual client side call
		rmiMethod.call();
	}

	public static void doServerRemoteCall(RMIMethod rmiMethod) {
		// actual server side execution
		rmiMethod.execute();
	}

}
