package com.dynatrace.oneagent.sdk.api;

/**
 * Interface for outgoing message tracer.
 * <a href="https://github.com/Dynatrace/OneAgent-SDK#messaging">https://github.com/Dynatrace/OneAgent-SDK#messaging</a>
 */
public interface OutgoingMessageTracer extends Tracer, OutgoingTaggable {

	/**
	 * Adds optional information about a traced message: message id provided by messaging system.
	 *  
	 * @param vendorMessageId the messageId
	 */
	public void setVendorMessageId(String vendorMessageId);

	/**
	 * Adds optional information about a traced message: correlation id used by messaging system.
	 *  
	 * @param correlationId correlationId
	 */
	public void setCorrelationId(String correlationId);

}
