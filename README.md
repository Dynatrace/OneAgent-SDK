# How to use this repository

This repository contains the specification of the Dynatrace OneAgent SDK. All interfaces are specified in Java as the language of choice. These interfaces are translated and implemented in different languages. We try to stay as close to the specification as possible, while still ensuring that language specific concepts are used where this makes the interface more usable.

This repository therefore can be considered a language independent documentation of the SDK's APIs and concepts.


#### Table of Contents

* [Dynatrace OneAgent SDK](#oneagentsdk)  
* [Language specific SDKs](#languagesdks)
* [API Concepts](#apiconcepts)
	* [OneAgentSDK object](#oneagentsdkobject)
	* [Tracers](#tracers)
	* [Service endpoints and communication endpoints](#endpoints)
* [Features](#features)
 	* [Trace incoming and outgoing remote calls](#remoting)
 	* [Trace database requests](#database)
 	* [Trace incoming web requests](#webrequests)
 	* [Trace in-process asynchronous execution](#in-process-linking)
 	* [Trace messages](#messaging)
 	* [Add custom request attributes](#scav)
* [Limits](#Limits)
* [Troubleshooting](#troubleshooting)
* [Help & Support](#help)
* [Further reading](#furtherreading)


<a name="oneagentsdk"/>

# Dynatrace OneAgent SDK

The Dynatrace OneAgent SDK can be used to add custom instrumentation for proprietary frameworks or programming languages that are not supported out-of-the-box by Dynatrace. The primary purpose of this SDK is to facilitate end-to-end tracing of transactions.
While other tracing frameworks are rather generic, the Dynatrace OneAgent SDK has more semantics to explicitly model remote calls, database requests, web requests, message passing, in-process context passing and more.

In order to use the Dynatrace OneAgent SDK you need to have access to the source code of the application in question. In languages like Java and Node.js you might have other possibilities to use the SDK even if you do not want or cannot modify the original code (aspects and monkey patching). 

OneAgent automatically detects that your application is instrumented with the OneAgent SDK and immediately begins monitoring it. A restart of the application is required following OneAgent installation on the host. For most languages the SDK does not contain much actual implementation as the real work is done by the Dynatrace OneAgent itself. The SDK just acts as an API to the OneAgent.

<a name="languagesdks"/>

# Language specific SDKs

The language specific SDKs are open source and published directly to GitHub, together with technical documentation and sample codes.

* [OneAgent SDK for Node.js](https://github.com/Dynatrace/OneAgent-SDK-for-NodeJs)
* [OneAgent SDK for Java](https://github.com/Dynatrace/OneAgent-SDK-for-Java)
* [OneAgent SDK for C/C++](https://github.com/Dynatrace/OneAgent-SDK-for-C)

<a name="apiconcepts"/>

# API Concepts

<a name="oneagentsdkobject"/>

## OneAgentSDK object

All interactions with the OneAgentSDK are done via a central interface. You can create an instance of this interface via a Factory.

```Java
OneAgentSDK OneAgentSDK = OneAgentSDKFactory.createInstance();
```

You can create more than one object of this in your application. This ensures that you do not need to coordinate a singleton behavior across the whole applications and that different frameworks can use the SDK independently from each other. The OneAgentSDK object enables you to create Tracers for different aspects of your application.

<a name="tracers"/>

## Tracers

To trace any kind of call you first need to create a Tracer. The Tracer object represents the logical and physical endpoint that you want to call. A Tracer serves two purposes. On the one hand to time the call (duration, CPU time and more) and on the other to report errors. That is why each Tracer has these three methods. The error method must be called only once, and it must be in between start and end. Actually it is not allowed to call start, end or error on the same Tracer instance from different threads.

```Java
void start();

void error(String message);

void end();
```

Therefore it is good practice to use a Tracer always with the following pattern:

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

The second purpose of a Tracer is to allow tracing across process boundaries. To achieve that these kind of traces supply so-called tags. Tags are strings or byte arrays that enable Dynatrace to trace a transaction end-to-end. As such the tag is the one information that you need to transport across these calls yourselfs.

<a name="endpoints"/>

## Service name, service endpoints and communication endpoints

Dynatrace supports the idea that the same service is deployed in different environments or just multiple times with different configs. One such logical deployment is usually identified some sort of endpoint string, most of times an URL. Therefore you can have the same *service (same name)* with multiple *service endpoints* (deployed instances of the service). 

Additionally you can supply the actual *endpoint for the communication channel*. This might sound strange until you think about clustered and highly available services. In such a case the same logical endpoint might have multiple *communication endpoints*. These might even change over time. The *communication endpoint* allows Dynatrace to understand which process, device or cloud service will receive the request. Thus even if you cannot install a OneAgent on that receiving end, the Dynatrace AI can reason about its impact on your system.

Imagine making a call to a cloud based clustered service. You can trace this call with the SDK, but you cannot install a OneAgent on that cloud based service. Due to the distinction of *service endpoint* and *communication endpoint* Dynatrace will understand that you are making calls to this service, it will understand that there are multiple instances, and if one of those starts to fail, the Dynatrace AI will be able to tell you about this and the impact this has.

If you can trace the call end-to-end though, the extra information about the *communication endpoint* will enable Dynatrace to understand the extra hops in between due to load balancers and proxies, which its AI can use again to better determine the root cause of any issue.

See <a href="https://www.dynatrace.com/support/help/server-side-services/service-analysis/how-do-i-analyze-individual-service-instances/" target="_blank">How do I analyze service instances?</a> in the Dynatrace product documentation to learn more.

<a name="features"/>

# Features

The feature sets differ slightly with each language implementation. More functionality will be added over time, see <a href="https://answers.dynatrace.com/spaces/483/dynatrace-product-ideas/idea/198106/planned-features-for-oneagent-sdk.html" target="_blank">Planned features for OneAgent SDK</a> for details on upcoming features.

<a name="remoting"/>

## Trace incoming and outgoing remote calls

You can use the SDK to trace proprietary IPC communication from one process to the other. This will enable you to see full Service Flow, PurePath and Smartscape topology for remoting technologies that Dynatrace is not aware of.

To trace any kind of remote call you first need to create a Tracer. The Tracer object represents the endpoint that you want to call, as such you need to supply the name of the remote service and remote method. In addition you need to transport the tag in your remote call to the server side if you want to trace it end-to-end.

```Java
OutgoingRemoteCallTracer outgoingRemoteCall = OneAgentSDK.traceOutgoingRemoteCall("remoteMethodToCall", "RemoteServiceName", "rmi://Endpoint/service", ChannelType.TCP_IP, "remoteHost:1234");

outgoingRemoteCall.start();
try {
	outgoingRemoteCall.setProtocolName("RMI/custom");
	String tag = outgoingRemoteCall.getDynatraceStringTag();
	//make the call and transport the tag across
} catch (Throwable e) {
	outgoingRemoteCall.error(e);
} finally {
	outgoingRemoteCall.end();
}

```

On the server side you need to wrap the handling and processing of your remote call as well. This will not only trace the server side call and everything that happens, it will also connect it to the calling side.

```Java
IncomingRemoteCallTracer incomingRemoteCall = OneAgentSDK.traceIncomingRemoteCall("remoteMethodToCall", "RemoteServiceName", "rmi://Endpoint/service");

incomingRemoteCall.setDynatraceStringTag(tag);
incomingRemoteCall.start();
try {
	incomingRemoteCall.setProtocolName("RMI/custom");
	doSomeWork();
} catch (Exception e) {
	incomingRemoteCall.error(e);
} finally {
	incomingRemoteCall.end();
}
```

<a name="database"/>

## Trace database requests

You can use the SDK to trace database requests that Dynatrace doesn't detect automatically. This will not only enable you to see single SQL statements within the traced requests, it will also extend SmartScape to include the traced database in the topology. This in turn will extend the reach of the Dynatrace AI, because it will baseline the behaviour of every single reported SQL statement and alert you on errors or slowdowns down to the single SQL statement.

To trace any kind of database request you first need to create a DatabaseInfo object. The info object represents the database itself.

```Java
DatabaseInfo databaseInfo = OneAgentSDK.createDatabaseInfo("myDB", DatabaseVendor.PROGRESS, ChannelType.TCP_IP, "dbHost:1234");
```

To trace a specific SQL statement you then need to create a Tracer object.

```Java
String sql = "Select name from User";
DatabaseRequestTracer sqlTracer = OneAgentSDK.traceSQLDatabaseRequest(databaseInfo, sql);

sqlTracer.start();
try {
	myDB.executeSQL(sql);
} catch (Exception e) {
	sqlTracer.error(e);
} finally {
	sqlTracer.end();

}
```

<a name="webrequests"/>

## Trace incoming web requests

You can use the SDK to trace incoming web requests. This might be useful if Dynatrace does not support the respective web server framework or language.

To trace any kind of incoming web request you first need to create a WebServerInfo object. The info object represents the endpoint of your web server.

```Java
WebServerInfo wsInfo = OneAgentSDK.createWebServerInfo("WebShopProduction", "CheckoutService", "/api/service/checkout");
```

To trace a specific incoming web request you then need to create a Tracer object.

```Java
IncomingWebRequestTracer tracer = OneAgentSDK.traceIncomingWebRequest(wsInfo,"https://www.oursupershop.com/api/service/checkout/save","POST")

tracer.setDynatraceStringTag(tag);
tracer.start();
try {
	int statusCodeReturnedToClient = processWebRequest();
	tracer.setStatusCode(statusCodeReturnedToClient);
} catch (Exception e) {
	tracer.error(e);
} finally {
	tracer.end();
}
```

<a name="in-process-linking"/>

## Trace in-process asynchronous execution

You can use the SDK to trace asynchronous in-process code execution. This might be useful if Dynatrace does not support the threading framework or specific asynchronous libraries. In-process-linking will only be used to link other services (Database-, Webrequests, ...) between thread or queuing boundaries, where currently no support is available.

To link asynchronous execution, you need to receive a InProcessLink, where async execution forks:

```Java
InProcessLink inProcessLink = OneAgentSDK.createInProcessLink();
```

The provided inProcessLink must not serialized and can only be used inside the process, where it has been created. It must be used to start tracing where the async execution takes place:

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

<a name="messaging"/>

## Trace messages

You can use the SDK to trace messages sent or received via messaging system. When tracing messages, we distinct between:
* sending a message
* receiving a message
* processing a received message

To trace an outgoing message, the code looks straight forward compared to other tracers:
 
```Java
MessagingSystemInfo messagingSystemInfo = oneAgentSDK.createMessagingSystemInfo("myMessagingSystem",
		"requestQueue", MessageDestinationType.QUEUE, ChannelType.TCP_IP, "localhost:4711");
OutgoingMessageTracer outgoingMessageTracer = oneAgentSDK.traceOutgoingMessage(messagingSystemInfo);
outgoingMessageTracer.start();
try {
	// transport the dynatrace tag along with the message: 	
	messageToSend.setHeaderField(
		OneAgentSDK.DYNATRACE_MESSAGING_HEADERNAME, outgoingMessageTracer.getDynatraceStringTag());
	theQueue.send(messageToSend);
	
	// optional:  add messageid provided from messaging system
	outgoingMessageTracer.setVendorMessageId(toSend.getMessageId());
	// optional:  add correlationId
	outgoingMessageTracer.setCorrelationId(toSend.correlationId);
} catch (Exception e) {
	outgoingMessageTracer.error(e.getMessage());
	Logger.logError(e);
} finally {
	outgoingMessageTracer.end();
}
```

On the incoming side, we need to differ between the blocking receiving part and processing the received message. Therefore two different tracers are being used: ReceivingMessageTracer and ProcessingMessageTracer.

```Java
MessagingSystemInfo messagingSystemInfo = oneAgentSDK.createMessagingSystemInfo("myMessagingSystem",
		"requestQueue", MessageDestinationType.QUEUE, ChannelType.TCP_IP, "localhost:4711");

// following code is probably inside a busy loop:
while(true) {
	ReceivingMessageTracer receivingMessageTracer = oneAgentSDK.traceReceivingMessage(messagingSystemInfo);
	receivingMessageTracer.start();
	try {
		// blocking call - until message is being available:
		Message queryMessage = theQueue.receive("client queries");
		ProcessingMessageTracer processingMessageTracer = oneAgentSDK
			.traceProcessingMessage(messagingSystemInfo);
		processingMessageTracer.setDynatraceStringTag(
			queryMessage.getHeaderField(OneAgentSDK.DYNATRACE_MESSAGING_HEADERNAME));
		processingMessageTracer.setVendorMessageId(queryMessage.msgId);
		processingMessageTracer.setCorrelationId(queryMessage.correlationId);
		processingMessageTracer.start();
		try {
			// do the work ... 
		} catch (Exception e) {
			processingMessageTracer.error(e.getMessage());
			Logger.logError(e);
		} finally {
			processingMessageTracer.end();
		}
	} catch (Exception e) {
		receivingMessageTracer.error(e.getMessage());
		Logger.logError(e);
	} finally {
		receivingMessageTracer.end();
	}
}
```

In case of no blocking call happens (e. g. message processing via eventhandler), there is no need to use ReceivingMessageTracer - just trace processing of the message by using the ProcessingMessageTracer:

```Java
public void onMessage(Message message) {
	MessagingSystemInfo messagingSystemInfo = oneAgentSDK.createMessagingSystemInfo("myMessagingSystem",
		"requestQueue", MessageDestinationType.QUEUE, ChannelType.TCP_IP, "localhost:4711");

	ProcessingMessageTracer processingMessageTracer = oneAgentSDK
		.traceProcessingMessage(messagingSystemInfo);
	processingMessageTracer.setDynatraceStringTag((String)
		message.getObjectProperty(OneAgentSDK.DYNATRACE_MESSAGING_HEADERNAME));
	processingMessageTracer.setVendorMessageId(queryMessage.msgId);
	processingMessageTracer.setCorrelationId(queryMessage.correlationId);
	processingMessageTracer.start();
	try {
		// do the work ... 
	} catch (Exception e) {
		processingMessageTracer.error(e.getMessage());
		Logger.logError(e);
	} finally {
		processingMessageTracer.end();
	}
}
```

<a name="scav"/>

## Add custom request attributes

You can use the SDK to add custom request attributes to current traced service. Custom request attributes allows you to do easier/better filtering of your requests in dynatrace.

Adding custom request attributes to the current traced service call is pretty simple. Just call one of the addCustomRequestAttribute methods with your key and value:

```Java
oneAgentSDK.addCustomRequestAttribute("region", "EMEA");
oneAgentSDK.addCustomRequestAttribute("salesAmount", 2500);
```

When no service call is being traced, the custom request attributes are dropped. 

<a name="limits"/>

# Limits

There are common limits, which will we applied when no other limit is explicitly mentioned.

## String length

There are two length limits for string parameters. Longer strings will be silently truncated:

* SQL statements: default 4096 characters 
* All other: 250 characters

Default might be overriden by tenant specific configuration.

<a name="troubleshooting"/>

# Troubleshooting

## Logging callback

The SDK provides a logging-callback to give information back to calling application in case of an error. User application has to provide a callback like the following:

```Java
public interface LoggingCallback {

	void warn(String message);

	void error(String message);
}
```
In general it is a good idea to forward these logging events to your application specific logging framework.

## Agent log

In case of issues, where logging callback doesn't report any errors and UI doesn't help: check the [agent log](https://www.dynatrace.com/support/help/installation/setup-tips/where-can-i-find-oneagent-files-and-logs/).

<a name="help"/>

# Help & Support

The Dynatrace OneAgent SDK is an open source project, currently in beta status. The features are fully supported by Dynatrace. 

**Get Help**
* Ask a question in the <a href="https://answers.dynatrace.com/spaces/482/view.html" target="_blank">product forums</a>
* Read the <a href="https://www.dynatrace.com/support/help/" target="_blank">product documentation</a>

**Open a GitHub issue to:**
* Report minor defects, minor items or typos
* Ask for improvements or changes in the SDK API
* Ask any questions related to the community effort

SLAs don't apply for GitHub issues

**Customers can open a ticket on the <a href="https://support.dynatrace.com/supportportal/" target="_blank">Dynatrace support portal</a> to:**
* Get support from the Dynatrace technical support engineering team
* Manage and resolve product related technical issues

SLAs apply according to the customer's support level.

<a name="furtherreading"/>

# Further reading

* <a href="https://www.dynatrace.com/support/help/extend-dynatrace/oneagent-sdk/what-is-oneagent-sdk/" target="_blank">What is the OneAgent SDK?</a> in the Dynatrace documentation
* <a href="https://answers.dynatrace.com/spaces/483/dynatrace-product-ideas/idea/198106/planned-features-for-oneagent-sdk.html" target="_blank">Feedback & Roadmap thread in AnswerHub</a>
* <a href="https://www.dynatrace.com/news/blog/dynatrace-oneagent-sdk-for-java-end-to-end-monitoring-for-proprietary-java-frameworks/" target="_blank">Blog: Dynatrace OneAgent SDK for Java: End-to-end monitoring for proprietary Java frameworks</a>
* <a href="https://www.dynatrace.com/news/blog/dynatrace-oneagent-sdk-c-service-transaction-monitoring-c-native-applications/" target="_blank">Blog: Dynatrace OneAgent SDK for C: Service and transaction monitoring for C++ and other native applications</a>
* <a href="https://www.dynatrace.com/news/blog/dynatrace-oneagent-sdk-for-node-js-extend-end-to-end-visibility/" target="_blank">Blog: Dynatrace OneAgent SDK for Node.js: Extend end-to-end visibility</a>
