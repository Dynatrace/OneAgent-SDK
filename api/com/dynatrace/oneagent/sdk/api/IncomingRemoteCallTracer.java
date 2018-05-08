package com.dynatrace.oneagent.sdk.api;

/**
 * Interface for incoming remote call tracer.
 * <a href="https://github.com/Dynatrace/OneAgent-SDK#remoting">https://github.com/Dynatrace/OneAgent-SDK#remoting</a>
 */
public interface IncomingRemoteCallTracer extends Tracer, IncomingTaggable {

	/**
	 * Sets the name of the used remoting protocol. This is completely optional and just for display purposes.
	 *
	 * @param protocolName		protocol name
	 */
	void setProtocolName(String protocolName);

}
