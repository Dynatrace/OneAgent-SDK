# How to use this repository

This repository contains the specification of the Dynatrace OneAgent SDK. All interfaces are specified in Java as the language of choice. These interfaces are implemented in different languages. We try to stay as close to the specification as possible, while still ensuring that language specific concepts are used where this makes the interface more usable.

This repository therefore can be considered a language independent documentation of the SDK's APIs and concepts.

## Table of Contents

* [Dynatrace OneAgent SDK](#oneagentsdk)  
* [Language specific SDKs](#languagesdks)
* [API Concepts](#apiconcepts)
  * [OneAgentSDK object](#oneagentsdkobject)
  * [Trace Context](#tracecontext)
  * [Tracers](#tracers)
  * [Service endpoints and communication endpoints](#endpoints)
* [Features](#features)
  * [Trace incoming and outgoing remote calls](#remoting)
  * [Trace database requests](#database)
  * [Trace web requests](#webrequests)
    * [Trace incoming web requests](#inwebrequests)
    * [Trace outgoing web requests](#outwebrequests)
  * [Trace in-process asynchronous execution](#in-process-linking)
  * [Trace messaging](#messaging)
    * [Trace outgoing messages](#trace-outgoing-messages)
    * [Trace incoming messages](#trace-incoming-messages)
  * [Trace custom services](#customservice)
  * [Add custom request attributes](#scav)
* [Limits](#limits)
* [Troubleshooting](#troubleshooting)
* [Help & Support](#help)
* [Further reading](#furtherreading)

<a name="oneagentsdk"></a>

## Dynatrace OneAgent SDK

The Dynatrace OneAgent SDK can be used to add custom instrumentation for proprietary frameworks or programming languages that are not supported out-of-the-box by Dynatrace. The primary purpose of this SDK is to facilitate end-to-end tracing of transactions.
While other tracing frameworks are rather generic, the Dynatrace OneAgent SDK has more semantics to explicitly model remote calls, database requests, web requests, message passing, in-process context passing and more.

In order to use the Dynatrace OneAgent SDK you need to have access to the source code of the application in question. In languages like Java and Node.js you might have other possibilities to use the SDK even if you do not want or cannot modify the original code (aspects and monkey patching).

OneAgent automatically detects that your application is instrumented with the OneAgent SDK and immediately begins monitoring it. A restart of the application is required following OneAgent installation on the host. For most languages the SDK does not contain much actual implementation as the real work is done by the Dynatrace OneAgent itself. The SDK just acts as an API to the OneAgent.

> Note: The OneAgent SDK is not supported on serverless code modules, including those for AWS Lambda.
> Consider using [OpenTelemetry](https://www.dynatrace.com/support/help/shortlink/opentel-lambda) instead in these scenarios.

<a name="languagesdks"></a>

## Language specific SDKs

The language specific SDKs are open source and published directly to GitHub, together with technical documentation and sample code.

* [OneAgent SDK for Node.js](https://github.com/Dynatrace/OneAgent-SDK-for-NodeJs)
* [OneAgent SDK for Java](https://github.com/Dynatrace/OneAgent-SDK-for-Java)
* [OneAgent SDK for C/C++](https://github.com/Dynatrace/OneAgent-SDK-for-C)
* [OneAgent SDK for Python](https://github.com/Dynatrace/OneAgent-SDK-for-Python)
* [OneAgent SDK for .NET](https://github.com/Dynatrace/OneAgent-SDK-for-dotnet)
* [OneAgent SDK for Go](https://github.com/Dynatrace/OneAgent-SDK-for-Go)
* [OneAgent SDK for PHP](https://github.com/Dynatrace/OneAgent-SDK-for-PHP)

The [Java API contained in this repository](https://github.com/Dynatrace/OneAgent-SDK/blob/master/api) is for design reference only. The actual Java API is available in the [OneAgent SDK for Java](https://github.com/Dynatrace/OneAgent-SDK-for-Java).

<a name="apiconcepts"></a>

## API Concepts

<a name="oneagentsdkobject"></a>

## OneAgentSDK object

All interactions with the OneAgentSDK are done via a central interface. You can create an instance of this interface via a factory.

```Java
OneAgentSDK oneAgentSDK = OneAgentSDKFactory.createInstance();
```

You can create more than one object of this in your application. This ensures that you do not need to coordinate a singleton behavior across the whole application and that different frameworks can use the SDK independently from each other. The OneAgentSDK object enables you to create Tracers for different aspects of your application.

<a name="tracecontext"></a>

## Trace Context

An instance of the `OneAgentSDK` provides access to `TraceContextInfo` which holds information
about the *Trace-Id* and *Span-Id* of the current PurePath node.
This information can then be used to provide e.g. additional context in log messages.

Please note that `TraceContextInfo` is not intended for tagging or context-propagation use cases.
Dedicated APIs (e.g. [remote calls](#remoting) or [web requests](#webreqeusts)) as well as
built-in OneAgent sensors take care of linking services correctly.

```Java
TraceContextInfo traceContextInfo = oneAgentSDK.getTraceContextInfo();
String traceId = traceContextInfo.getTraceId();
String spanId = traceContextInfo.getSpanId();
logger.info("[!dt dt.trace_id={},dt.span_id={}] sending request ...", traceId, spanId);
```

<a name="tracers"></a>

## Tracers

To trace any kind of call you first need to create a Tracer. The Tracer object represents the logical and physical endpoint that you want to call. A Tracer serves two purposes. On the one hand to time the call (duration, CPU time and more) and on the other to report errors. That is why each Tracer has these three methods. The error method must be called only once, and it must be in between start and end.

```Java
void start();

void error(String message);

void end();
```

It is good practice to use a Tracer with the following pattern:

```Java
tracer.start();
try {
	//make the call you want to trace
} catch (Throwable e) {
	tracer.error(e);
} finally {
	tracer.end();
}
```

To allow tracing across process and technology boundaries, tracers can be supplied with so-called tags. Tags are strings or byte arrays generated by the SDK that enable Dynatrace to trace a transaction end-to-end. The user has to take care of transporting the tag from one process to the other.

A Tracer instance can only be used from the thread on which it was created. See [Trace in-process asynchronous execution](#in-process-linking) for tracing across thread boundaries.

<a name="endpoints"></a>

## Service name, service endpoints and communication endpoints

Dynatrace supports the idea that the same service is deployed in different environments or just multiple times with different configs. One such logical deployment is usually identified some sort of endpoint string, most of times an URL. Therefore you can have the same *service (same name)* with multiple *service endpoints* (deployed instances of the service).

Additionally you can supply the actual endpoint for the communication channel. This might sound strange until you think about clustered and highly available services. In such a case the same logical endpoint might have multiple communication endpoints. These might even change over time. The communication endpoint allows Dynatrace to understand which process, device or cloud service will receive the request. Thus even if you cannot install a OneAgent on that receiving end, the Dynatrace AI can reason about its impact on your system.

Imagine making a call to a cloud based clustered service. You can trace this call with the SDK, but you cannot install a OneAgent on that cloud based service. Due to the distinction of service endpoint and communication endpoint Dynatrace will understand that you are making calls to this service, it will understand that there are multiple instances, and if one of those starts to fail, the Dynatrace AI will be able to tell you about this and the impact this has.

If you can trace the call end-to-end though, the extra information about the communication endpoint will enable Dynatrace to understand the extra hops in between due to load balancers and proxies, which its AI can use again to better determine the root cause of any issue.

See <a href="https://www.dynatrace.com/support/help/server-side-services/service-analysis/how-do-i-analyze-individual-service-instances/" target="_blank">How do I analyze service instances?</a> in the Dynatrace product documentation to learn more.

<a name="features"></a>

## Features

The feature sets differ slightly with each language implementation - see the respective language-specific documentation for feature availability. More functionality will be added over time, see <a href="https://answers.dynatrace.com/spaces/483/dynatrace-product-ideas/idea/198106/planned-features-for-oneagent-sdk.html" target="_blank">Planned features for OneAgent SDK</a> for details on upcoming features.

<a name="remoting"></a>

### Trace incoming and outgoing remote calls

You can use the SDK to trace proprietary IPC communication from one process to the other. This will enable you to see full Service Flow, PurePath and Smartscape topology for remoting technologies that Dynatrace is not aware of.

To trace any kind of remote call you first need to create a Tracer. The Tracer object represents the endpoint that you want to call, as such you need to supply the name of the remote service and remote method. In addition you need to transport the tag in your remote call to the server side if you want to trace it end-to-end.

```Java
OutgoingRemoteCallTracer tracer = OneAgentSDK.traceOutgoingRemoteCall("remoteMethodToCall", "RemoteServiceName", "rmi://Endpoint/service", ChannelType.TCP_IP, "remoteHost:1234");

tracer.start();
try {
	tracer.setProtocolName("RMI/custom");
	String tag = tracer.getDynatraceStringTag();
	// make the call and transport the tag across to the server to link both sides of the remote call together
} catch (Throwable e) {
	tracer.error(e);
} finally {
	tracer.end();
}

```

On the server side you need to wrap the handling and processing of your remote call as well. This will not only trace the server side call and everything that happens, it will also connect it to the calling side.

```Java
IncomingRemoteCallTracer tracer = OneAgentSDK.traceIncomingRemoteCall("remoteMethodToCall", "RemoteServiceName", "rmi://Endpoint/service");

tracer.setDynatraceStringTag(tag); // link both sides of the remote call together
tracer.start();
try {
	tracer.setProtocolName("RMI/custom");
	doSomeWork();
} catch (Exception e) {
	tracer.error(e);
} finally {
	tracer.end();
}
```

<a name="database"></a>

### Trace database requests

You can use the SDK to trace database requests that Dynatrace doesn't detect automatically. This will not only enable you to see single SQL statements within the traced requests, it will also extend SmartScape to include the traced database in the topology. This in turn will extend the reach of the Dynatrace AI, because it will baseline the behaviour of every single reported SQL statement and alert you on errors or slowdowns down to the single SQL statement.

To trace any kind of database request you first need to create a `DatabaseInfo` object. The info object represents the database itself.

```Java
DatabaseInfo databaseInfo = OneAgentSDK.createDatabaseInfo("myDB", DatabaseVendor.PROGRESS, ChannelType.TCP_IP, "dbHost:1234");
```

To trace a specific SQL statement you then need to create a Tracer object.

```Java
String stmt = "SELECT name FROM User";
DatabaseRequestTracer tracer = OneAgentSDK.traceSQLDatabaseRequest(databaseInfo, stmt);

tracer.start();
try {
	result = myDB.executeSQL(stmt);
	tracer.setRowsReturned(result.rows);
} catch (Exception e) {
	tracer.error(e);
} finally {
	tracer.end();
}
```

Please note that SQL database traces are only created if they occur within some other SDK trace (e.g. incoming remote call)
or a OneAgent built-in trace (e.g. incoming web request).

<a name="webrequests"></a>

### Trace web requests

<a name="inwebrequests"></a>

#### Trace incoming web requests

You can use the SDK to trace incoming web requests. This might be useful if Dynatrace does not support the respective web server framework or language.

To trace any kind of incoming web request you first need to create a `WebApplicationInfo` object. The info object represents the endpoint of your web server.

```Java
WebApplicationInfo waInfo = OneAgentSDK.createWebApplicationInfo("WebShopProduction", "CheckoutService", "/api");
```

To trace a specific incoming web request you then need to create a Tracer object.

```Java
IncomingWebRequestTracer tracer = OneAgentSDK.traceIncomingWebRequest(waInfo, "https://www.oursupershop.com/api/checkout", "POST")

tracer.setDynatraceStringTag(tag);
tracer.start();
try {
	int statusCodeReturnedToClient = processWebRequest(); // link both sides of the web request together
	tracer.setStatusCode(statusCodeReturnedToClient);
} catch (Exception e) {
	tracer.error(e);
} finally {
	tracer.end();
}
```

<a name="outwebrequests"></a>

#### Trace outgoing web requests

You can use the SDK to trace outgoing web requests. This might be useful if Dynatrace does not support the respective http library or language.

To trace an outgoing web request you need to create a Tracer object. It is important to include the Dynatrace header. This ensures that tagging with our built-in sensor is working.

```Java
OutgoingWebRequestTracer tracer = oneAgentSdk.traceOutgoingWebRequest(url, "GET");
tracer.start();
try {
	request = MyHttpLibrary.newGetRequest(url);

	// sending HTTP header OneAgentSDK.DYNATRACE_HTTP_HEADERNAME is necessary for tagging:
	request.addHeader(OneAgentSDK.DYNATRACE_HTTP_HEADERNAME, tracer.getDynatraceStringTag());

	// provide all request headers to tracer (optional):
	for (Entry<String, String> entry : request.getHeaders().entrySet()) {
		tracer.addRequestHeader(entry.getKey(), entry.getValue());
	}

	response = request.execute();

	for (Entry<String, List<String>> entry : response.getHeaders().entrySet()) {
		for (String value : entry.getValue()) {
			tracer.addResponseHeader(entry.getKey(), value);
		}
	}
	tracer.setStatusCode(response.getResponseCode());

} catch (Exception e) {
	tracer.error(e);
} finally {
	tracer.end();
}
```

<a name="in-process-linking"></a>

### Trace in-process asynchronous execution

You can use the SDK to trace asynchronous in-process code execution. This might be useful if the OneAgent does not support the threading framework or specific asynchronous libraries. In-process-linking should be used to link other services (Database, web requests, ...) between thread or queueing boundaries currently not supported out-of-the-box by the OneAgent.

To link asynchronous execution, you need to create an ``InProcessLink``, where async execution forks:

```Java
InProcessLink inProcessLink = OneAgentSDK.createInProcessLink();
```

The provided `InProcessLink` must not be serialized and can only be used inside the process in which it was created. It must be used to start tracing where the async execution takes place:

```Java
InProcessLinkTracer tracer = oneAgentSDK.traceInProcessLink(inProcessLink);

tracer.start();
try {
	// do the asynchronous job
} catch (Exception e) {
	tracer.error(e);
} finally {
	tracer.end();
}
```

<a name="messaging"></a>

### Trace messaging

You can use the SDK to trace messages sent or received via a messaging system. When tracing messages, we distinguish between:

* sending a message
* receiving a message
* processing a received message

#### Trace outgoing messages

An outgoing message is traced by calling `traceOutgoingMessage()` with a `MessagingSystemInfo` object.  
This instance of `MessagingSystemInfo` can be created by calling `createMessagingSystemInfo` with the following arguments:

* `vendorName:` Mandatory - the messaging system vendor name (e.g. RabbitMq, Apache Kafka, ...), which can be a user defined
string. If possible, use a constant defined in `MessageSystemVendor`.
* `destinationName:` Mandatory - the destination name (e.g. queue name, topic name).
* `destinationType:` Mandatory - specifies the type of the destination. Valid values are defined by the `MessageDestinationType` enum.
* `channelType`: Mandatory - A value from  the `ChannelType` enum to specify the protocol used as communication channel (e.g. TCP/IP, IN_PROCESS,... ).
* `channelEndpoint:` Optional - a string describing the endpoint according to the protocol set in `channelType`:
  * TCP/IP: Host name or IP address of the server-side, may include the port number (e.g., "1.2.3.4:8080" or "example.com:1234").
  * UNIX domain sockets: name of the domain socket file.
  * Named pipes: name of the pipe.

Instances of `MessagingSystemInfo` can and should be reused across tracing calls.

The result of `traceIncomingMessage()` is a tracer object to be used for further operations related to this particular trace
(see [Tracers](#tracers) for details).

Besides the common APIs for outgoing tracers, this tracer offers the additional methods `setVendorMessageId()` and
`setCorrelationId()` which may be used to provide more details about the sent message. Both APIs take a string as a parameter
which may be used to report the `correlationId` or `vendorMessageId` provided by the messaging system.

**Example:**

```Java
MessagingSystemInfo messagingSystemInfo = oneAgentSDK.createMessagingSystemInfo("myMessagingSystem",
		"requestQueue", MessageDestinationType.QUEUE, ChannelType.TCP_IP, "localhost:4711");
OutgoingMessageTracer tracer = oneAgentSDK.traceOutgoingMessage(messagingSystemInfo);
tracer.start();
try {
	// transport the Dynatrace tag along with the message to allow the outgoing message tracer to be linked
	// with the message processing tracer on the receiving side
	messageToSend.setHeaderField(
		OneAgentSDK.DYNATRACE_MESSAGE_PROPERTYNAME, tracer.getDynatraceStringTag());
	theQueue.send(messageToSend);

	// optional:  add messageid provided from messaging system
	tracer.setVendorMessageId(messageToSend.getMessageId());
	// optional:  add correlationId
	tracer.setCorrelationId(messageToSend.getCorrelationId());
} catch (Exception e) {
	tracer.error(e.getMessage());
	Logger.logError(e);
} finally {
	tracer.end();
}
```

#### Trace incoming messages

On the incoming side, we need to distinguish between the blocking receive operation and the processing of the received message.
Therefore two different tracers are being used: `IncomingMessageReceiveTracer` and `IncomingMessageProcessTracer`.

An instance of `IncomingMessageReceiveTracer` is created by calling `traceIncomingMessageReceive` and similarly,
`traceIncomingMessageProcess` must be called to get an instance of `IncomingMessageProcessTracer`.

Both functions expect an argument of type `MessagingSystemInfo` which is created in the same way as
[for outgoing messages](#trace-outgoing-messages).

The result of both `traceIncomingMessageReceive` and `traceIncomingMessageProcess` are tracer objects to be used for further operations related to this trace (see [Tracers](#tracers) for details).

Besides the common APIs for incoming tracers, an instance of `IncomingMessageProcessTracer` offers the same additional
methods `setVendorMessageId()` and `setCorrelationId()` as described for [for outgoing messages](#trace-outgoing-messages).

**Example:**

```Java
MessagingSystemInfo messagingSystemInfo = oneAgentSDK.createMessagingSystemInfo("myMessagingSystem",
		"requestQueue", MessageDestinationType.QUEUE, ChannelType.TCP_IP, "localhost:4711");

// message receiving daemon task:
while(true) {
	IncomingMessageReceiveTracer receiveTracer =
		oneAgentSDK.traceIncomingMessageReceive(messagingSystemInfo);
	receiveTracer.start();
	try {
		// blocking call - until message is being available:
		Message queryMessage = theQueue.receive("client queries");
		IncomingMessageProcessTracer processTracer = oneAgentSDK
			.traceIncomingMessageProcess(messagingSystemInfo);
		// retrieve Dynatrace tag created using the outgoing message tracer to link both sides together
		processTracer.setDynatraceStringTag(
			queryMessage.getHeaderField(OneAgentSDK.DYNATRACE_MESSAGE_PROPERTYNAME));
		processTracer.setVendorMessageId(queryMessage.getMessageId()); // optional
		processTracer.setCorrelationId(queryMessage.getCorrelationId()); // optional
		processTracer.start();
		try {
			// do the work ...
		} catch (Exception e) {
			processTracer.error(e.getMessage());
			Logger.logError(e);
		} finally {
			processTracer.end();
		}
	} catch (Exception e) {
		receiveTracer.error(e.getMessage());
		Logger.logError(e);
	} finally {
		receiveTracer.end();
	}
}
```

In case of a non-blocking receive operation (e.g. via an event handler), there is no need to
use `IncomingMessageReceiveTracer` - just trace the processing of the message by using `IncomingMessageProcessTracer`:

**Example:**

```Java
MessagingSystemInfo messagingSystemInfo = oneAgentSDK.createMessagingSystemInfo("myMessagingSystem",
	"requestQueue", MessageDestinationType.QUEUE, ChannelType.TCP_IP, "localhost:4711");

public void onMessage(Message message) {
	IncomingMessageProcessTracer processTracer = oneAgentSDK
		.traceIncomingMessageProcess(messagingSystemInfo);
	// retrieve Dynatrace tag created using the outgoing message tracer to link both sides together
	processTracer.setDynatraceStringTag((String)
		message.getObjectProperty(OneAgentSDK.DYNATRACE_MESSAGE_PROPERTYNAME));
	processTracer.setVendorMessageId(queryMessage.getMessageId()); // optional
	processTracer.setCorrelationId(queryMessage.getCorrelationId()); // optional
	processTracer.start();
	try {
		// do the work ...
	} catch (Exception e) {
		processTracer.error(e.getMessage());
		Logger.logError(e);
	} finally {
		processTracer.end();
	}
}
```

<a name="customservice"></a>

### Trace custom services

You can use the SDK to trace custom service methods. A custom service method is a meaningful part of your code that you want to trace but that does not fit any other tracer. An example could be the callback of a periodic timer.

```Java
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
```

<a name="scav"></a>

### Add custom request attributes

You can use the SDK to add custom request attributes to the currently traced service. Custom request attributes allow you to do easier/better filtering of your requests in Dynatrace.

Adding custom request attributes to the currently traced service call is pretty simple. Just call one of the ``addCustomRequestAttribute`` methods with your key and value:

```Java
oneAgentSDK.addCustomRequestAttribute("region", "EMEA");
oneAgentSDK.addCustomRequestAttribute("salesAmount", 2500);
```

When no service call is being traced, the custom request attributes are dropped.

## Limits

### String length

There are different length limits for string parameters:

* SQL statements, web request URI and query: 4.096 characters
* All others: 250 characters

Longer strings will be silently truncated.

<a name="troubleshooting"></a>

## Troubleshooting

### Logging callback

The SDK provides a logging-callback to give information back to the calling application in case of an error. The user application has to provide a callback like the following:

```Java
public interface LoggingCallback {

	void warn(String message);

	void error(String message);
}
```

It is set using the `setLoggingCallback` method. In general it is a good idea to forward these logging events to your application specific logging framework.

### Agent log

In case of issues, where the logging callback doesn't report any errors and the UI doesn't help: check the [agent log](https://www.dynatrace.com/support/help/installation/setup-tips/where-can-i-find-oneagent-files-and-logs/).

<a name="help"></a>

## Help & Support

### Support policy

The Dynatrace OneAgent SDK has GA status. The features are fully supported by Dynatrace.

Deprecations of APIs will be announced in the release notes of the specific OneAgent SDK (e.g. OneAgent SDK for Java release notes). Deprecated APIs can be removed from newer OneAgent SDK versions after having been deprecated for at least 1 year.

End of support announcements for a specific OneAgent SDK version will be announced in the release notes of the specific OneAgent SDK (e.g. OneAgent SDK for Java release notes) at least six months in advance, in the OneAgent release notes as well as on the list of end of support announcements.

### Get Help

* Ask a question in the <a href="https://answers.dynatrace.com/spaces/482/view.html" target="_blank">product forums</a>
* Read the <a href="https://www.dynatrace.com/support/help/" target="_blank">product documentation</a>

**Open a GitHub issue to:**

* Report minor defects, minor items or typos
* Ask for improvements or changes in the SDK API
* Ask any questions related to the community effort

SLAs don't apply for GitHub issues.

**Customers can open a ticket on the <a href="https://support.dynatrace.com/supportportal/" target="_blank">Dynatrace support portal</a> to:**

* Get support from the Dynatrace technical support engineering team
* Manage and resolve product related technical issues

SLAs apply according to the customer's support level.

<a name="furtherreading"></a>

## Further reading

* <a href="https://www.dynatrace.com/support/help/extend-dynatrace/oneagent-sdk/what-is-oneagent-sdk/" target="_blank">What is the OneAgent SDK?</a> in the Dynatrace documentation
* <a href="https://answers.dynatrace.com/spaces/483/dynatrace-product-ideas/idea/198106/planned-features-for-oneagent-sdk.html" target="_blank">Feedback & Roadmap thread in AnswerHub</a>
* <a href="https://www.dynatrace.com/news/blog/dynatrace-oneagent-sdk-for-java-end-to-end-monitoring-for-proprietary-java-frameworks/" target="_blank">Blog: Dynatrace OneAgent SDK for Java: End-to-end monitoring for proprietary Java frameworks</a>
* <a href="https://www.dynatrace.com/news/blog/dynatrace-oneagent-sdk-c-service-transaction-monitoring-c-native-applications/" target="_blank">Blog: Dynatrace OneAgent SDK for C: Service and transaction monitoring for C++ and other native applications</a>
* <a href="https://www.dynatrace.com/news/blog/dynatrace-oneagent-sdk-for-node-js-extend-end-to-end-visibility/" target="_blank">Blog: Dynatrace OneAgent SDK for Node.js: Extend end-to-end visibility</a>
