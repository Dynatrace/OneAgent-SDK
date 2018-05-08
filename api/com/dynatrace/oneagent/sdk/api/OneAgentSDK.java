package com.dynatrace.oneagent.sdk.api;

import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.enums.ChannelType;
import com.dynatrace.oneagent.sdk.api.enums.MessageDestinationType;
import com.dynatrace.oneagent.sdk.api.enums.MessageSystemVendor;
import com.dynatrace.oneagent.sdk.api.enums.SDKState;
import com.dynatrace.oneagent.sdk.api.enums.OneAgentInfo;
import com.dynatrace.oneagent.sdk.api.infos.DatabaseInfo;
import com.dynatrace.oneagent.sdk.api.infos.WebApplicationInfo;
import com.dynatrace.oneagent.sdk.api.infos.MessagingSystemInfo;
import com.dynatrace.oneagent.sdk.api.metric.FloatCounter;
import com.dynatrace.oneagent.sdk.api.metric.FloatGauge;
import com.dynatrace.oneagent.sdk.api.metric.FloatStatistics;
import com.dynatrace.oneagent.sdk.api.metric.IntegerCounter;
import com.dynatrace.oneagent.sdk.api.metric.IntegerGauge;
import com.dynatrace.oneagent.sdk.api.metric.IntegerStatistics;

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
	 * @param channelEndpoint	this represents the communication endpoint for the remote service. This information allows Dynatrace to tie the database requests to a specific process or cloud service. It is optional.
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
	 * Creates tracer for an incoming asynchronous message (receive).
	 * 
	 * @param messagingSystem	information about the messaging system (see createMessagingSystemInfo methods).
	 * @return {@link IncomingMessageReceiveTracer} to work with
	 */
	IncomingMessageReceiveTracer traceIncomingMessageReceive(MessagingSystemInfo messagingSystem);
	
	/**
	 * Creates tracer for processing (consuming) an received message (onMessage).
	 * 
	 * @param messagingSystem	information about the messaging system (see createMessagingSystemInfo methods).
	 * @return {@link IncomingMessageProcessTracer} to work with
	 */
	IncomingMessageProcessTracer traceIncomingMessageProcess(MessagingSystemInfo messagingSystem);

	// ***** Metrics *****
	
	/**
	 * Creates a counter metric instance. Counter used for all metrics, that are counting something like sent/received bytes to/from network.
	 * 
	 * Counter sums up provided samples and reports the sum only.
	 * 
	 * All metrics created with same values for metricKey and dimension will be aggregated locally. Aggregated metrics sent in 60 
	 * second wall clock interval to server.
	 * 
	 * @param metricKey	path like name of metric with unit-name as last part. e. g. 
	 *					<ul>
	 * 						<li>"disk.written.bytes"</li>
	 * 						<li>"page.hits"</li>
	 * 					</ul>
	 * 					maximum length of metricKey is 100 char and must match the following pattern:
	 * 					<BLOCKQUOTE>
	 * 						PackageComponent: [a-zA-Z][a-zA-Z0-9_-]*
	 * 						MetricKey: PackageComponent { "." PackageComponent }
	 * 					</BLOCKQUOTE> 
	 * 					If metricKey doesn't match this pattern, it will be ignored.
	 * @return the metric instance being used for reporting. Returned instances are thread safe and should be reused whenever possible.
	 * 					Calling this method twice or more with same metric key might return same instance.
	 */
	IntegerCounter createIntegerCounterMetric(String metricKey);
	
	/**
	 * Floating point variant of {@link #createIntegerCounterMetric(String)}.
	 * 
	 * @param metricKey same restrictions apply as for metricKey in {@link #createIntegerCounterMetric(String)}
	 * @return For details see return value of {@link #createIntegerCounterMetric(String)}.
	 */
	FloatCounter createFloatCounterMetric(String metricKey);

	/**
	 * Creates a gauge metric instance. Gauges can be used for metrics describing a current state like 
	 * temperature, number of items in a cache.
	 * 
	 * Gauges are intended for periodical sampling and reporting min, max and average of provided samples.
	 * 
	 * @param metricKey same restrictions apply as for metricKey in {@link #createIntegerCounterMetric(String)}
	 * @return For details see return value of {@link #createIntegerCounterMetric(String)}.
	 */
	IntegerGauge createIntegerGaugeMetric(String metricKey);

	/**
	 * Floating point variant of {@link #createIntegerGaugeMetric(String)}.
	 *  
	 * @param metricKey same restrictions apply as for metricKey in {@link #createIntegerCounterMetric(String)}
	 * @return For details see return value of {@link #createIntegerCounterMetric(String)}.
	 */
	FloatGauge createFloatGaugeMetric(String metricKey);
	
	/**
	 * Creates a statistics metric instance. Statistics can/should be used for event driven metrics like 
	 * packet size of network interface.
	 * 
	 * Statistics are reporting min, max, average and count.
	 * 
	 * @param metricKey same restrictions apply as for metricKey in {@link #createIntegerCounterMetric(String)}
	 * @return For details see return value of {@link #createIntegerCounterMetric(String)}.
	 * 
	 */
	IntegerStatistics createIntegerStatisticsMetric(String metricKey);

	/**
	 * Floating point variant of {@link #createIntegerStatisticsMetric(String)}.
	 *  
	 * @param metricKey same restrictions apply as for metricKey in {@link #createIntegerCounterMetric(String)}
	 * @return For details see return value of {@link #createIntegerCounterMetric(String)}.
	 */
	FloatStatistics createFloatStatisticsMetric(String metricKey);
	
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

}
