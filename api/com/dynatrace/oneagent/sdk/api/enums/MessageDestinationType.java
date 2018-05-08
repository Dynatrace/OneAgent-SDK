package com.dynatrace.oneagent.sdk.api.enums;

import com.dynatrace.oneagent.sdk.api.OneAgentSDK;

/**
 * Enumerates all well-known messagingdestination types. See
 * {@link OneAgentSDK#createMessagingSystemInfo(String, String, MessageDestinationType, ChannelType, String)}
 */
public enum MessageDestinationType {

	QUEUE("Queue"),
	TOPIC("Topic");

	private String name;

	private MessageDestinationType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
