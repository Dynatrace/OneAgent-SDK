package com.dynatrace.oneagent.sdk.api;

import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.enums.ChannelType;
import com.dynatrace.oneagent.sdk.api.enums.MessageDestinationType;
import com.dynatrace.oneagent.sdk.api.enums.MessageSystemVendor;
import com.dynatrace.oneagent.sdk.api.enums.SDKState;
import com.dynatrace.oneagent.sdk.api.infos.DatabaseInfo;
import com.dynatrace.oneagent.sdk.api.infos.MessagingSystemInfo;
import com.dynatrace.oneagent.sdk.api.infos.OneAgentInfo;
import com.dynatrace.oneagent.sdk.api.infos.WebApplicationInfo;
import com.dynatrace.oneagent.sdk.api.infos.TraceContextInfo;

/**
 * Interface implemented by OneAgentSDK. Retrieved by {@link OneAgentSDKFactory#createInstance()}. For details see:
 * <a href="https://github.com/Dynatrace/OneAgent-SDK#oneagentsdkobject">https://github.com/Dynatrace/OneAgent-SDK#oneagentsdkobject</a>
 */
public interface OneAgentSDK {

	/**
	 * Using this headername to transport Dynatrace tag inside an outgoing http request ensures compatibility to Dynatrace built-in sensors.
	 */
	public static final String DYNATRACE_HTTP_HEADERNAME = "X-dynaTrace";
	
	/**
	 * Using this propertyname to transport Dynatrace tag along with the message, ensures compatibility to Dynatrace built-in sensors.
	 */
	public static final String DYNATRACE_MESSAGE_PROPERTYNAME = "dtdTraceTagInfo";
	
	
	// ***** Web Server Initialization *****

	/**
	 * Initializes a WebApplicationInfo instance that is required for tracing incoming web requests. This information determines the identity and name of the resulting Web Request service in dynatrace.
	 * Also see https://www.dynatrace.com/support/help/server-side-services/introduction/how-does-dynatrace-detect-and-name-services/#web-request-services for detail description of the meaning of the parameters.
	 *
	 * @param webServerName		logical name of the web server. In case of a cluster every node in the cluster must report the same name here.
	 * 							Attention: Make sure not to use the host header for this parameter. Host headers are often spoofed and contain things like google or baidoo which do not reflect your setup.
	 * @param applicationID		application ID of the web application
	 * @param contextRoot		context root of the application.
	 * 							All URLs traced with the returned WebApplicationInfo, should start with provided context root. 
	 * @return					{@link WebApplicationInfo} instance to work with
	 */
	WebApplicationInfo createWebApplicationInfo(String webServerName, String applicationID, String contextRoot);

	// ***** Database Initialization *****

	/**
	 * Initializes a DatabaseInfo instance that is required for tracing database requests.
	 *
	 * @param name				name of the database
	 * @param vendor			database vendor name (e.g. Oracle, MySQL, ...), can be a user defined name
	 *                          If possible use a constant defined in com.dynatrace.oneagent.sdk.api.enums.DatabaseVendor
	 * @param channelType		communication protocol used to communicate with the database.
	 * @param channelEndpoint	this represents the communication endpoint for the database. This information allows Dynatrace to tie the database requests to a specific process or cloud service. It is optional.
	 * 							* for TCP/IP: host name/IP of the server-side (can include port in the form of "host:port") 
	 * 							* for UNIX domain sockets: name of domain socket file
	 * 							* for named pipes: name of pipe
	 * @return					{@link DatabaseInfo} instance to work with
	 */
	DatabaseInfo createDatabaseInfo(String name, String vendor, ChannelType channelType, String channelEndpoint);

	// ***** Web Requests (incoming) *****

	/**
	 * Creates a tracer for an incoming web request.
	 *
	 * @param webApplicationInfo	information about web application
	 * @param url					(parts of a) URL, which will be parsed into: scheme, hostname/port, path & query
	 * 								does not have to contain a scheme or host. You can use the URL as it was sent in the HTTP request.
	 * 								Note: the hostname will be resolved by the Agent (asynchronously) at start() call
	 * @param method				HTTP request method
	 * @return						{@link IncomingWebRequestTracer} to work with
	 */
	IncomingWebRequestTracer traceIncomingWebRequest(WebApplicationInfo webApplicationInfo, String url, String method);

	// ***** Web Requests (outgoing) *****
	
	/**
	 * Traces an outgoing web request.
	 *
	 * @param url					URL, which will be parsed into: scheme, hostname/port, path & query
	 * 								Note: the hostname will be resolved by the Agent at start() call
	 * @param method				HTTP request method
	 * @return						{@link OutgoingWebRequestTracer} to work with
	 */
	OutgoingWebRequestTracer traceOutgoingWebRequest(String url, String method);
	
	// ***** Database Calls (outgoing only) *****

	/**
	 * Creates a tracer for tracing outgoing SQL database requests.
	 *
	 * @param databaseInfo			information about database
	 * @param statement				database SQL statement
	 * @return						{@link DatabaseRequestTracer} to work with
	 */
	DatabaseRequestTracer traceSQLDatabaseRequest(DatabaseInfo databaseInfo, String statement);

	// ***** Remote Calls (outgoing & incoming) *****

	/**
	 * Creates a tracer for an outgoing remote call.
	 *
	 * @param serviceMethod		name of the called remote method
	 * @param serviceName		name of the remote service
	 * @param serviceEndpoint	logical deployment endpoint on the server side
	 *							In case of a clustered/load balanced service, the serviceEndpoint represents the common logical endpoint (e.g. registry://staging-environment/myservices/serviceA) where as the @channelEndpoint represents the actual communication endpoint. As such a single serviceEndpoint can have many channelEndpoints.
	 * @param channelType		communication protocol used by remote call
	 * @param channelEndpoint	this represents the communication endpoint for the remote service. It is optional.
	 * 							* for TCP/IP: host name/IP of the server-side (can include port)
	 * 							* for UNIX domain sockets: path of domain socket file
	 * 							* for named pipes: name of pipe
	 * @return					{@link OutgoingRemoteCallTracer} instance to work with
	 */
	OutgoingRemoteCallTracer traceOutgoingRemoteCall(String serviceMethod, String serviceName, String serviceEndpoint, ChannelType channelType, String channelEndpoint);

	/**
	 * Creates a tracer for an incoming remote call.
	 *
	 * @param serviceMethod		name of the called remote method
	 * @param serviceName		name of the remote service
	 * @param serviceEndpoint	logical deployment endpoint on the server side
	 *                          In case of a clustered/load balanced service, the serviceEndpoint represents the common logical endpoint (e.g. registry://staging-environment/myservices/serviceA). As such a single serviceEndpoint can have many processes on many hosts that services requests for it.
	 * @return					{@link IncomingRemoteCallTracer} instance to work with
	 */
	IncomingRemoteCallTracer traceIncomingRemoteCall(String serviceMethod, String serviceName, String serviceEndpoint);

	// ***** in-process-linking *****

	/**
	 * Creates a link for in-process-linking.
	 *  
	 * @return 			{@link InProcessLink} instance to work with. Use it with {@link #traceInProcessLink(InProcessLink)}
	 */
	InProcessLink createInProcessLink();
	
	/**
	 * Creates a tracer for an asynchronous in-process code execution.
	 * 
	 * @param inProcessLink a InProcessLink received via {@link #createInProcessLink()}
	 * @return			{@link InProcessLinkTracer} to work with.
	 */
	InProcessLinkTracer traceInProcessLink(InProcessLink inProcessLink);

	// ***** Custom request attributes *****
	/**
	 * Adds a custom request attribute to currently traced service call. Might be called multiple times, to add more than one attribute.
	 * Check via {@link #setLoggingCallback(LoggingCallback)} if error happened. If two attributes with same key are set, both 
	 * attribute-values are captured. 
	 * 
	 * @param key				key of the attribute. required parameter. 
	 * @param value 			value of the attribute. required parameter.
	 */
	void addCustomRequestAttribute(String key, String value);

	/**
	 * Does exactly the same as {@link #addCustomRequestAttribute(String, String)}, but request-attribute type long.
	 */
	void addCustomRequestAttribute(String key, long value);
	
	/**
	 * Does exactly the same as {@link #addCustomRequestAttribute(String, String)}, but request-attribute type double.
	 */
	void addCustomRequestAttribute(String key, double value);

	// ***** Messaging (outgoing & incoming) *****

	/**
	 * Initializes a MessagingSystemInfo instance that is required for tracing messages (with topology EXTERNAL).
	 *
	 * @param vendorName	one of {@link MessageSystemVendor} if well known vendor. custom provided in any other case.
	 * @param destinationName	destination name (e.g. queue name, topic name)
	 * @param destinationType	destination type: 'Topic' or 'Queue'
	 * @param channelType		communication protocol used
	 * @param channelEndpoint	optional and depending on protocol:
	 * 							* for TCP/IP: host name/IP of the server-side (can include port)
	 * 							* for UNIX domain sockets: name of domain socket file
	 * 							* for named pipes: name of pipe
	 * @return					{@link MessagingSystemInfo} instance to work with
	 */
	MessagingSystemInfo createMessagingSystemInfo(String vendorName, String destinationName, MessageDestinationType destinationType, ChannelType channelType, String channelEndpoint);

	/**
	 * Creates a tracer for an outgoing asynchronous message (send).
	 * 
	 * @param messagingSystem	information about the messaging system (see createMessagingSystemInfo methods).
	 * @return {@link OutgoingMessageTracer} to work with 
	 */
	OutgoingMessageTracer traceOutgoingMessage(MessagingSystemInfo messagingSystem);

	/**
	 * Creates a tracer for an incoming asynchronous message (blocking receive).
	 * 
	 * @param messagingSystem	information about the messaging system (see createMessagingSystemInfo methods).
	 * @return {@link IncomingMessageReceiveTracer} to work with
	 */
	IncomingMessageReceiveTracer traceIncomingMessageReceive(MessagingSystemInfo messagingSystem);
	
	/**
	 * Creates a tracer for processing (consuming) a received message (onMessage).
	 * 
	 * @param messagingSystem	information about the messaging system (see createMessagingSystemInfo methods).
	 * @return {@link IncomingMessageProcessTracer} to work with
	 */
	IncomingMessageProcessTracer traceIncomingMessageProcess(MessagingSystemInfo messagingSystem);
	
	/**
	 * Creates a tracer for a customer transaction (Dynatrace call them Custom service). Used whenever a transaction 
	 * should be traced, that does not match any of the specialised transaction types (e. g. DB-request, webrequest, ...).
	 * 
	 * @param serviceMethod service method being used for service creation.
	 * @param serviceName service name being used for service creation.
	 * 
	 * @return {@link CustomServiceTracer} to work with
	 */
	CustomServiceTracer traceCustomService(String serviceMethod, String serviceName);

	// ***** various *****

	/**
     * Returns the current SDKState. See {@link SDKState} for details.
     *
     * @return current state - never null.
     */
    SDKState getCurrentState();
    
    /**
     * Returns detailed information about the OneAgent used by the SDK.
     * 
     * @return see {@link OneAgentInfo} for more details. Never returns null.
     */
    OneAgentInfo getAgentInfo();
    
    /**
     * Installs a callback that gets informed, if any SDK action has failed. For details see {@link LoggingCallback} interface. The
     * provided callback must be thread-safe, when using this {@link OneAgentSDK} instance in multithreaded environments.
     *
     * @param loggingCallback            may be null, to remove current callback. provided callback replaces any previously set callback.
     */
    void setLoggingCallback(LoggingCallback loggingCallback);

    /**
     * Returns information about the current PurePath node using the TraceContext
		 * (Trace-Id, Span-Id) model as defined in https://www.w3.org/TR/trace-context.
		 *  
     * @return see {@link TraceContextInfo} for more details. Never returns null.
     */
    TraceContextInfo getTraceContextInfo();
}
