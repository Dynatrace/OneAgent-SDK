package com.dynatrace.oneagent.sdk.api.enums;

import com.dynatrace.oneagent.sdk.api.OneAgentSDK;

/**
 * Enumerates all well-known messaging systems. See {@link OneAgentSDK#createDatabaseInfo(String, String, ChannelType, String)}.
 * Using these constants ensures that services captured by OneAgentSDK are handled the same way as traced via built-in sensors. 
 */
public enum MessageSystemVendor {

	HORNETQ("HornetQ"),
	ACTIVE_MQ("ActiveMQ"),
	RABBIT_MQ("RabbitMQ"),
	ARTEMIS("Artemis"),
	WEBSPHERE("WebSphere"),
	MQSERIES_JMS("MQSeries JMS"),
	MQSERIES("MQSeries"),
	TIBCO("Tibco");
	
	private String vendorName;
	
	private MessageSystemVendor(String vendorName) {
		this.vendorName = vendorName;
	}
	
	public String getVendorName() {
		return vendorName;
	}

	@Override
	public String toString() {
		return vendorName;
	}

}
