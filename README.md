# How to use this repository

This repository contains the specification of the Dynatrace OneAgent SDK. All interfaces are specified in Java as the language of choice. These interfaces are translated and implemented in different languages. We try to stay as close to the specification as possible, while still ensuring that language specific concepts are used where this makes the interface more usable.

This repository therefore can be considered a language independent documentation of the SDK's APIs and concepts.


#### Table of Contents

* [Dynatrace OneAgent SDK](#oneagentsdk)  
	* [Requirements](#requirements)
* [Language specific SDKs](#languagesdks)
* [API Concepts](#apiconcepts)
	* [OneAgentSDK object](#oneagentsdkobject)
	* [Tracers](#tracers)
	* [Service endpoints and communication endpoints](#endpoints)
* [Features](#features)
 	* [Trace incoming and outgoing remote calls](#remoting)
 	* [Trace database requests](#database)
 	* [Trace incoming web requests](#webrequests)
* [Further reading](#furtherreading)


<a name="oneagentsdk"/>

# Dynatrace OneAgent SDK

The Dynatrace OneAgent SDK can be used to add custom instrumentation for proprietary frameworks or programming languages that are not supported out-of-the-box by Dynatrace. The primary purpose of this SDK is to facilitate end-to-end tracing of transactions.
While other tracing frameworks are rather generic, the Dynatrace OneAgent SDK has more semantics to explicitly model remote calls, database requests, web requests, message passing, in-process context passing and more.

In order to use the Dynatrace OneAgent SDK you need to have access to the source code of the application in question. In languages like Java and Node.js you might have other possibilities to use the SDK even if you do not want or cannot modify the original code (aspects and monkey patching). For most languages the SDK does not contain much actual implementation as the real work is done by the Dynatrace OneAgent itself. The SDK just acts as an API to the OneAgent.

<a name="requirements"/>

## Requirements

* Dynatrace OneAgent v1.33 or higher installed on the host running the application/service to be monitored
* Ability to edit the source code of the application/service to be monitored
* OneAgent automatically detects that your application is instrumented with the OneAgent SDK and immediately begins monitoring it. A restart of the application is required following OneAgent installation on the host.

<a name="languagesdks"/>

# Language specific SDKs

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

## Common concepts: service endpoints and communication endpoints

Dynatrace supports the idea that the same service is deployed in different environments or just multiple times with different configs. One such logical deployment is usually identified some sort of endpoint string, most of times an URL. Therefore you can have the same service (same name) with multiple service endpoints (deployed instances of the service).

Additionally you can supply the actual communication endpoint. This might sound strange until you think about clustered and highly available servies. In such a case the same logic endpoint might have multiple communication endpoints. These might even change over time. The communication endpoint allows Dynatrace to understand which process, device or cloud service will receive the request. Thus even if you cannot install a OneAgent on that receiving end, the Dynatrace AI can reason about its impact on your system.

Imagine making a call to a cloud based clustered service. You can trace this call with the SDK, but you cannot install a OneAgent on that cloud based service. Due to the distinction of service endpoint and communication endpoint Dynatrace will understand that you are making calls to this service, it will understand that there are multiple instances, and if one of those starts to fail, the Dynatrace AI will be able to tell you about this and the impact this has.

If you can trace the call end-to-end though, the extra information about the communication endpoint will enable Dynatrace to understand the extra hops in between due to load balancers and proxies, which its AI can use again to better determine the root cause of any issue.

<a name="features"/>

# Features

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

<a name="furtherreading"/>

# Further reading

* <a href="https://www.dynatrace.com/support/help/extend-dynatrace/oneagent-sdk/what-is-oneagent-sdk/" target="_blank">What is the OneAgent SDK?</a>
* <a href="https://answers.dynatrace.com/spaces/483/dynatrace-product-ideas/idea/198106/planned-features-for-oneagent-sdk.html" target="_blank">Feedback & Roadmap thread in AnswerHub</a>
* <a href="https://www.dynatrace.com/news/blog/dynatrace-oneagent-sdk-for-java-end-to-end-monitoring-for-proprietary-java-frameworks/" target="_blank">Blog: Dynatrace OneAgent SDK for Java: End-to-end monitoring for proprietary Java frameworks</a>
* <a href="https://www.dynatrace.com/news/blog/dynatrace-oneagent-sdk-c-service-transaction-monitoring-c-native-applications/" target="_blank">Blog: Dynatrace OneAgent SDK for C: Service and transaction monitoring for C++ and other native applications</a>
* <a href="https://www.dynatrace.com/news/blog/dynatrace-oneagent-sdk-for-node-js-extend-end-to-end-visibility/" target="_blank">Blog: Dynatrace OneAgent SDK for Node.js: Extend end-to-end visibility</a>
