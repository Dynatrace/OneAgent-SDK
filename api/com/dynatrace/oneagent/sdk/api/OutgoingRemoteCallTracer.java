package com.dynatrace.oneagent.sdk.api;

/**
 * Interface for outgoing remote call tracer.
 * <a href="https://github.com/Dynatrace/OneAgent-SDK#remoting">https://github.com/Dynatrace/OneAgent-SDK#remoting</a>
 */
public interface OutgoingRemoteCallTracer extends Tracer, OutgoingTaggable {

	/**
	 * Sets the name of the used remoting protocol. This is completely optional and just for display purposes.
	 *
	 * @param protocolName	protocol name
	 */
	void setProtocolName(String protocolName);

}
	