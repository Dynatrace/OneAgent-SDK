package com.dynatrace.oneagent.sdk.samples;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.InProcessLink;
import com.dynatrace.oneagent.sdk.api.InProcessLinkTracer;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import com.dynatrace.oneagent.sdk.api.OutgoingMessageTracer;
import com.dynatrace.oneagent.sdk.api.IncomingMessageProcessTracer;
import com.dynatrace.oneagent.sdk.api.IncomingMessageReceiveTracer;
import com.dynatrace.oneagent.sdk.api.enums.ChannelType;
import com.dynatrace.oneagent.sdk.api.enums.MessageDestinationType;
import com.dynatrace.oneagent.sdk.api.infos.MessagingSystemInfo;

/**
 * This sample shows the usage of the SDK for tracing asynchronous messages.
 */
public class MessagingSample {

	private static OneAgentSDK oneAgentSDK;
	private static MessageQueue theQueue = new MessageQueue();

	private static ExecutorService executor = Executors.newSingleThreadExecutor();

	private static class MessageQueue {

		public void send(Message msg) {

		}

		public Message receive(String correlationId) {
			return new Message("receivedMessage", correlationId);
		}

	}

	private static class Message {
		private final String msgId;
		private final String correlationId;

		private final Map<String, String> fields = new HashMap<>();

		public Message(String msgText, String correlationId) {
			this.correlationId = correlationId;
			this.msgId = String.format("%X", System.identityHashCode(this));
		}

		public void setHeaderField(String field, String value) {
			fields.put(field, value);
		}

		public String getHeaderField(String field) {
			return fields.get(field);
		}

		public String getMessageId() {
			return msgId;
		}
	}

	public static void main(String[] args) {
		oneAgentSDK = OneAgentSDKFactory.createInstance();
		doClientMessageSend();
		doMessageReceiveWithSyncProcessing();
		// or 
		doMessageReceiveWithAsyncProcessing();
	}

	/**
	 * simulate sync call via async messaging
	 */
	public static void doClientMessageSend() {
		final String correlationId = "ABC123";
		Message toSend = new Message("query message to server", correlationId);

		MessagingSystemInfo messagingSystemInfo = oneAgentSDK.createMessagingSystemInfo("myCreativeMessagingSystem",
				"theOnlyQueue", MessageDestinationType.QUEUE, ChannelType.TCP_IP, "localhost:4711");

		// sending the request:
		{
			OutgoingMessageTracer outgoingMessageTracer = oneAgentSDK.traceOutgoingMessage(messagingSystemInfo);
			outgoingMessageTracer.start();
			try {
				toSend.setHeaderField(OneAgentSDK.DYNATRACE_MESSAGE_PROPERTYNAME, outgoingMessageTracer.getDynatraceStringTag());
				theQueue.send(toSend);
				outgoingMessageTracer.setVendorMessageId(toSend.getMessageId()); // optional payload
				outgoingMessageTracer.setCorrelationId(toSend.correlationId);
			} catch (Exception e) {
				outgoingMessageTracer.error(e.getMessage());
			} finally {
				outgoingMessageTracer.end();
			}
		}

		// waiting for server response message:
		{
			IncomingMessageReceiveTracer receivingMessageTracer = oneAgentSDK.traceIncomingMessageReceive(messagingSystemInfo);
			receivingMessageTracer.start();
			try {
				Message answer = theQueue.receive(correlationId);
				
				IncomingMessageProcessTracer processMessageTracer = oneAgentSDK.traceIncomingMessageProcess(messagingSystemInfo);
				processMessageTracer.setDynatraceStringTag(answer.getHeaderField(OneAgentSDK.DYNATRACE_MESSAGE_PROPERTYNAME));
				processMessageTracer.setVendorMessageId(answer.msgId);
				processMessageTracer.setCorrelationId(answer.correlationId);
				processMessageTracer.start();
				try {
					// handle answer message in sync way ...
				} catch (Exception e) {
					processMessageTracer.error(e.getMessage());
				} finally {
					processMessageTracer.end();
				}
			} catch (Exception e) {
				receivingMessageTracer.error(e.getMessage());
			} finally {
				receivingMessageTracer.end();
			}
		}
	}

	public static void doMessageReceiveWithSyncProcessing() {
		MessagingSystemInfo messagingSystemInfo = oneAgentSDK.createMessagingSystemInfo("myCreativeMessagingSystem",
				"theOnlyQueue", MessageDestinationType.QUEUE, ChannelType.TCP_IP, "localhost:4711");

		
		IncomingMessageReceiveTracer receivingMessageTracer = oneAgentSDK.traceIncomingMessageReceive(messagingSystemInfo);
		receivingMessageTracer.start();
		try {
			Message queryMessage = theQueue.receive("client queries");
			IncomingMessageProcessTracer processingMessageTracer = oneAgentSDK.traceIncomingMessageProcess(messagingSystemInfo);
			processingMessageTracer.setDynatraceStringTag(queryMessage.getHeaderField(OneAgentSDK.DYNATRACE_MESSAGE_PROPERTYNAME));
			processingMessageTracer.setVendorMessageId(queryMessage.msgId);
			processingMessageTracer.setCorrelationId(queryMessage.correlationId);
			processingMessageTracer.start();
			try {
				doServerSideMessageProcessing(queryMessage);
			} catch (Exception e) {
				processingMessageTracer.error(e.getMessage());
			} finally {
				processingMessageTracer.end();
			}
			
		} catch (Exception e) {
			receivingMessageTracer.error(e.getMessage());
		} finally {
			receivingMessageTracer.end();
		}
	}
	
	public static void doMessageReceiveWithAsyncProcessing() {
		MessagingSystemInfo messagingSystemInfo = oneAgentSDK.createMessagingSystemInfo("myCreativeMessagingSystem",
				"theOnlyQueue", MessageDestinationType.QUEUE, ChannelType.TCP_IP, "localhost:4711");

		IncomingMessageReceiveTracer receivingMessageTracer = oneAgentSDK.traceIncomingMessageReceive(messagingSystemInfo);
		receivingMessageTracer.start();
		try {
			final Message queryMessage = theQueue.receive("client queries");

			final InProcessLink inProcessLink = oneAgentSDK.createInProcessLink();
			executor.submit(new Runnable() {
				@Override
				public void run() {

					InProcessLinkTracer inProcessLinkTracer = oneAgentSDK.traceInProcessLink(inProcessLink);
					inProcessLinkTracer.start();
					try {
						IncomingMessageProcessTracer processingMessageTracer = oneAgentSDK.traceIncomingMessageProcess(messagingSystemInfo);
						processingMessageTracer.setDynatraceStringTag(queryMessage.getHeaderField(OneAgentSDK.DYNATRACE_MESSAGE_PROPERTYNAME));
						processingMessageTracer.setVendorMessageId(queryMessage.msgId);
						processingMessageTracer.setCorrelationId(queryMessage.correlationId);
						processingMessageTracer.start();
						try {
							doServerSideMessageProcessing(queryMessage);
						} catch (Exception e) {
							processingMessageTracer.error(e.getMessage());
						} finally {
							processingMessageTracer.end();
						}
					} catch (Exception e) {
						inProcessLinkTracer.error(e.getMessage());
					} finally {
						inProcessLinkTracer.end();
					}
				}
			});
			
		} catch (Exception e) {
			receivingMessageTracer.error(e.getMessage());
		} finally {
			receivingMessageTracer.end();
		}
	}

	public static void doServerSideMessageProcessing(Message queryMessage) {
		MessagingSystemInfo messagingSystemInfo = oneAgentSDK.createMessagingSystemInfo("myCreativeMessagingSystem",
				"theOnlyQueue", MessageDestinationType.QUEUE, ChannelType.TCP_IP, "localhost:4711");
		// do the work ...
		// and: send answer message to client:
		Message answerMessage = new Message("answerMessage", queryMessage.correlationId);
		OutgoingMessageTracer outgoingMessageTracer = oneAgentSDK.traceOutgoingMessage(messagingSystemInfo);
		outgoingMessageTracer.start();
		try {
			theQueue.send(answerMessage);
			outgoingMessageTracer.setVendorMessageId(answerMessage.getMessageId());
		} catch (Exception e) {
			outgoingMessageTracer.error(e.getMessage());
		} finally {
			outgoingMessageTracer.end();
		}
	}
}
