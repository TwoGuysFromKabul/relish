package net.sf.relish.mqttclient;

import static net.sf.relish.RelishUtil.*;
import static net.sf.relish.matcher.RelishMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import net.sf.relish.CountQuantifier;
import net.sf.relish.DataFormat;
import net.sf.relish.ExpandingArrayList;
import net.sf.relish.RelishUtil;
import net.sf.relish.RelishException;
import net.sf.relish.mqttbroker.MqttBrokerStepDefs.RetainedTransformer;
import net.sf.relish.transformer.CountQuantifierTransformer;
import net.sf.relish.transformer.IsNotNullTransformer;
import net.sf.relish.transformer.IsNullTransformer;
import net.sf.xenqtt.client.MqttClient;
import net.sf.xenqtt.client.MqttClientConfig;
import net.sf.xenqtt.client.MqttClientListener;
import net.sf.xenqtt.client.NullReconnectStrategy;
import net.sf.xenqtt.client.PublishMessage;
import net.sf.xenqtt.client.Subscription;
import net.sf.xenqtt.client.SyncMqttClient;
import net.sf.xenqtt.message.ConnectMessage;
import net.sf.xenqtt.message.QoS;
import cucumber.api.Transform;
import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Cucumber step defs for controlling an MQTT client.
 */
public final class MqttClientStepDefs {

	private final Map<String, ClientInfo> clientInfoById = new HashMap<String, ClientInfo>();

	/**
	 * Cleans up resources created by the step defs
	 */
	@After
	public void after() {

		for (ClientInfo info : clientInfoById.values()) {
			if (info.client != null) {
				info.client.close();
			}
		}
	}

	/**
	 * Configures the Will Message for a client. If this is not called before {@link #mqttClientIsConnected(String, boolean, String, String, String)} no Will
	 * Message will be used.
	 * 
	 * @param clientId
	 *            The ID of the client to configure the Will Message for
	 * @param retained
	 *            True to set the retained flag on the Will Message
	 * @param qos
	 *            The QoS level of the Will Message
	 * @param topic
	 *            The topic to publish the Will Message to
	 * @param format
	 *            The {@link DataFormat format} of the Will Message body
	 * @param willPayload
	 *            The Will Message payload
	 */
	@Given("^MQTT client \"(\\S.*)\" will message is( not)? retained at QoS ([01]) on \"(\\S.*)\" with this (XML|JSON|text):$")
	public void mqttClientWillMessage(String clientId, @Transform(IsNullTransformer.class) boolean retained, int qos, String topic, DataFormat format,
			String willPayload) {

		ClientInfo info = getOrCreateClientInfo(clientId);
		info.assertNotConnected();
		info.willRetained = retained;
		info.willQos = qos;
		info.willTopic = topic;
		info.willMessage = format.normalizeText(willPayload);
	}

	/**
	 * Configures the keep alive interval for the MQTT client. If no keep alive interval is specified then the {@link MqttClientConfig#getKeepAliveSeconds()
	 * default} will be used.
	 * 
	 * @param clientId
	 *            The ID of the client to configure the keep alive interval for.
	 * @param interval
	 *            The interval to use
	 */
	@Given("^MQTT client \"(\\S.*)\" uses a keep alive interval of ([\\d]+) seconds?$")
	public void mqttClientUsesAKeepAliveIntervalOf(String clientId, int interval) {

		ClientInfo info = getOrCreateClientInfo(clientId);
		info.assertNotConnected();
		info.keepAliveInterval = interval;
	}

	/**
	 * Connects the specified MQTT client to the a broker
	 * 
	 * @param clientId
	 *            The ID of the client to connect
	 * @param cleanSession
	 *            If true then the {@link ConnectMessage#isCleanSession() clean session flag} will be st
	 * @param brokerUri
	 *            The URI of the broker to connect to
	 * @param user
	 *            The user name to use in the connect message. If null then no username is used.
	 * @param password
	 *            The password to use in the connect message. If null then no password is used. If this is not null then user may not be null
	 */
	@Given("^MQTT client \"(\\S.*)\" is connected( with a clean session)? to \"(tcp://\\S+:[0-9]{1,5})\"(?: as user \"(\\S.*)\"(?: with password \"(\\S.*)\")?)?$")
	public void mqttClientIsConnected(String clientId, @Transform(IsNotNullTransformer.class) boolean cleanSession, String brokerUri, String user,
			String password) {

		ClientInfo info = getOrCreateClientInfo(clientId);
		info.assertNotConnected();
		MqttClientConfig config = new MqttClientConfig();
		config.setReconnectionStrategy(new NullReconnectStrategy());
		if (info.keepAliveInterval != Integer.MIN_VALUE) {
			config.setKeepAliveSeconds(info.keepAliveInterval);
		}
		info.client = new SyncMqttClient(brokerUri, new ClientListener(info), 1, config);
		QoS theQos = info.willTopic == null ? null : QoS.lookup(info.willQos);
		info.client.connect(clientId, cleanSession, user, password, info.willTopic, info.willMessage, theQos, info.willRetained);
	}

	/**
	 * Disconnects the specified client from the broker
	 * 
	 * @param clientId
	 *            The ID of the client to disconnect
	 */
	@Given("^MQTT client \"(\\S.*)\" is disconnected$")
	public void mqttClientIsDisconnected(String clientId) {

		ClientInfo info = getOrCreateClientInfo(clientId);
		info.assertConnected();
		info.client.disconnect();
		info.client = null;
	}

	/**
	 * Subscribes the specified client to the topic
	 * 
	 * @param clientId
	 *            The ID of the client to subscribed
	 * @param topic
	 *            The topic for the client to subscribe to
	 * @param qos
	 *            The QoS the client requests the subscription to be at
	 */
	@Given("^MQTT client \"(\\S.*)\" is subscribed to topic \"(.*)\" at QoS ([01])$")
	public void mqttClientIsSubscribedToTopic(String clientId, String topic, int qos) {

		ClientInfo info = getOrCreateClientInfo(clientId);
		info.assertConnected();
		info.client.subscribe(new Subscription[] { new Subscription(topic, QoS.lookup(qos)) });
	}

	/**
	 * Unsubscribes the client from the topic
	 * 
	 * @param clientId
	 *            The ID of the client to unsubscribe
	 * @param topic
	 *            The topic to unsubscribe the client from
	 */
	@Given("^MQTT client \"(\\S.*)\" is unsubscribed from topic \"(.*)\"$")
	public void mqttClientIsUnubscribedFromTopic(String clientId, String topic) {

		ClientInfo info = getOrCreateClientInfo(clientId);
		info.assertConnected();
		info.client.unsubscribe(new String[] { topic });
	}

	/**
	 * Makes the client publish a message with a payload to the client
	 * 
	 * @param clientId
	 *            The ID of the client to publish the message
	 * @param retained
	 *            If included the retained flag will be set in the message header
	 * @param qos
	 *            The QoS to publish the message at (0 or 1)
	 * @param topic
	 *            The topic to publish the message to
	 * @param format
	 *            The format of the message body
	 * @param payload
	 *            The payload of the message
	 */
	@When("^MQTT client \"(\\S.*)\" publishes a( retained)? message at QoS ([01]) to \"(\\S.*)\" as (XML|JSON|text|binary):$")
	public void mqttClientPublishesAMessage(String clientId, @Transform(RetainedTransformer.class) boolean retained, int qos, String topic, DataFormat format,
			String payload) {

		ClientInfo info = getOrCreateClientInfo(clientId);
		info.assertConnected();
		info.client.publish(new PublishMessage(topic, QoS.lookup(qos), format.textToBytes(payload), retained));
	}

	/**
	 * Makes the client publish a message with a payload to the client. The payload is read from a file.
	 * 
	 * @param clientId
	 *            The ID of the client to publish the message
	 * @param retained
	 *            If included the retained flag will be set in the message header
	 * @param qos
	 *            The QoS to publish the message at (0 or 1)
	 * @param topic
	 *            The topic to publish the message to
	 * @param filename
	 *            The name of the file to read the payload from
	 */
	@When("^MQTT client \"(\\S.*)\" publishes a( retained)? message at QoS ([01]) to \"(\\S.*)\" from file \"(\\S.*)\"$")
	public void mqttClientPublishesAMessageFromFile(String clientId, @Transform(RetainedTransformer.class) boolean retained, int qos, String topic,
			String filename) {

		ClientInfo info = getOrCreateClientInfo(clientId);
		info.assertConnected();
		File file = new File(filename);
		if (!file.exists()) {
			throw new RelishException("File %s does not exist", filename);
		}
		byte[] payload = RelishUtil.getFileContents(file);
		info.client.publish(new PublishMessage(topic, QoS.lookup(qos), payload, retained));
	}

	/**
	 * Validates whether the {@link PublishMessage#isRetain() retained flag} is set on messages received by the client.
	 * 
	 * @param clientId
	 *            The ID of the client that should have received the message
	 * @param startIndex
	 *            The index of the first message in the range to validate. The first message s index 1, the second message is index 2, etc. The first message
	 *            received by the client from the topic is index 1, the second message is 2, etc.
	 * @param endIndex
	 *            The index of the last message in the range to validate: 1 is the first message, 2 is the seconds message, etc. If null then startIndex is
	 *            used.
	 * @param topic
	 *            The topic the message was received from
	 * @param retained
	 *            Whether or not the retained flag is set in the message's header
	 */
	@Then("^MQTT client \"(\\S.*)\" messages? (\\d+)(?: thru (\\d+))? from \"(\\S.*)\" should( not)? be retained$")
	public void mqttClientMessageFromShouldBeRetained(String clientId, int startIndex, Integer endIndex, String topic,
			@Transform(IsNullTransformer.class) boolean retained) {

		ClientInfo info = getOrCreateClientInfo(clientId);
		int end = getEndIndex(startIndex, endIndex);
		for (int i = startIndex; i <= end; i++) {
			PublishMessage msg = info.getRequiredMessage(topic, i);
			assertThat(msg.isRetain(), equalTo(retained), "MQTT client %s message %d from %s retained does not match", clientId, i, topic);
		}
	}

	/**
	 * Validates the QoS of messages received by the client.
	 * 
	 * @param clientId
	 *            The ID of the client that should have received the message
	 * @param startIndex
	 *            The index of the first message in the range to validate. The first message s index 1, the second message is index 2, etc. The first message
	 *            received by the client from the topic is index 1, the second message is 2, etc.
	 * @param endIndex
	 *            The index of the last message in the range to validate: 1 is the first message, 2 is the seconds message, etc. If null then startIndex is
	 *            used.
	 * @param topic
	 *            The topic the message was received from
	 * @param qos
	 *            The QoS the message was published at
	 */
	@Then("^MQTT client \"(\\S.*)\" messages? (\\d+)(?: thru (\\d+))? from \"(\\S.*)\" should have a QoS of ([01])$")
	public void mqttClientMessageFromShouldHaveAQosOf(String clientId, int startIndex, Integer endIndex, String topic, int qos) {

		ClientInfo info = getOrCreateClientInfo(clientId);
		int end = getEndIndex(startIndex, endIndex);
		for (int i = startIndex; i <= end; i++) {
			PublishMessage msg = info.getRequiredMessage(topic, i);
			assertThat(msg.getQoS(), equalTo(QoS.lookup(qos)), "MQTT client %s message %d from %s QoS does not match", clientId, i, topic);
		}
	}

	/**
	 * Validates the payload of message published to the client.
	 * 
	 * @param clientId
	 *            The ID of the client that should have received the message
	 * @param startIndex
	 *            The index of the first message in the range to validate. The first message s index 1, the second message is index 2, etc. The first message
	 *            received by the client from the topic is index 1, the second message is 2, etc.
	 * @param endIndex
	 *            The index of the last message in the range to validate: 1 is the first message, 2 is the seconds message, etc. If null then startIndex is
	 *            used.
	 * @param topic
	 *            The topic the message was received from
	 * @param format
	 *            The format the body is expected to be in
	 * @param bodyRegex
	 *            A regular expression which the message's body must match. If the format is "binary" then the body of the message is hexadecimal text. If the
	 *            body contains multiple lines each line will have all leading and trailing whitespace removed then all lines will be concatenated into a single
	 *            line.
	 */
	@Then("^MQTT client \"(\\S.*)\" messages? (\\d+)(?: thru (\\d+))? from \"(\\S.*)\" should match this (XML|JSON|text|binary):$")
	public void mqttClientMessageFromShouldMatch(String clientId, int startIndex, Integer endIndex, String topic, DataFormat format, String bodyRegex) {

		ClientInfo info = getOrCreateClientInfo(clientId);
		int end = getEndIndex(startIndex, endIndex);
		for (int i = startIndex; i <= end; i++) {
			PublishMessage msg = info.getRequiredMessage(topic, i);
			bodyRegex = format.normalizeRegex(bodyRegex);
			String payload = format.bytesToText(msg.getPayload());
			assertThat(payload, matches(bodyRegex), "MQTT client %s message %d from %s payload does not match", clientId, i, topic);
		}
	}

	/**
	 * Validates the number of messages received by a client from a topic.
	 * 
	 * @param clientId
	 *            The ID of the client to validate the received messages for
	 * @param countQualifier
	 *            How the count will be evaluated
	 * @param count
	 *            The number of messages that should have been received
	 * @param topic
	 *            The topic the messages were received from
	 */
	@Then("^MQTT client \"(\\S.*)\" should have (exactly|at least|at most) (\\d+) messages? from \"(\\S.*)\"$")
	public void mqttClientShouldHaveMessageFromCount(String clientId, @Transform(CountQuantifierTransformer.class) CountQuantifier countQuantifier, int count,
			String topic) {

		ClientInfo info = getOrCreateClientInfo(clientId);
		assertThat(info.getMessageCount(topic), countQuantifier.newMatcher(count), "MQTT client %s message count from %s does not match", clientId, topic);
	}

	/**
	 * Waits for a specified number of messages received by a client from a topic.
	 * 
	 * @param clientId
	 *            The ID of the client to validate the received messages for
	 * @param countQualifier
	 *            How the count will be evaluated
	 * @param count
	 *            The number of messages that should have been received
	 * @param topic
	 *            The topic the messages were received from
	 * @param timeout
	 *            The value of the max time to wait for the messages
	 * @param timeoutUnit
	 *            The unit of measure for the timeout value (seconds or milliseconds)
	 */
	@Then("^MQTT client \"(\\S.*)\" should have at least (\\d+) messages? from \"(\\S.*)\" within (\\d+) (seconds|milliseconds)$")
	public void mqttClientShouldHaveMessageFromCountWithin(String clientId, int count, final String topic, int timeout, TimeUnit timeoutUnit) {

		final ClientInfo info = getOrCreateClientInfo(clientId);
		Callable<Integer> callable = new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				return info.getMessageCount(topic);
			}
		};

		assertThatWithin(timeout, timeoutUnit, callable, gte(count), "MQTT client %s message count from %s does not match", clientId, topic);
	}

	private ClientInfo getOrCreateClientInfo(String clientId) {

		ClientInfo info = clientInfoById.get(clientId);
		if (info == null) {
			info = new ClientInfo(clientId);
			clientInfoById.put(clientId, info);
		}

		return info;
	}

	private int getEndIndex(int startIndex, Integer endIndex) {
		return endIndex == null ? startIndex : endIndex;
	}

	private final class ClientListener implements MqttClientListener {

		private final ClientInfo info;

		public ClientListener(ClientInfo info) {
			this.info = info;
		}

		@Override
		public void publishReceived(MqttClient client, PublishMessage message) {

			List<PublishMessage> msgs = info.receivedMessagesByTopic.get(message.getTopic());
			if (msgs == null) {
				msgs = Collections.synchronizedList(new ExpandingArrayList<PublishMessage>());
				List<PublishMessage> old = info.receivedMessagesByTopic.putIfAbsent(message.getTopic(), msgs);
				if (old != null) {
					msgs = old;
				}
			}

			msgs.add(message);
			message.ack();
		}

		@Override
		public void disconnected(MqttClient client, Throwable cause, boolean reconnecting) {
			info.client = null;
		}
	}

	private static final class ClientInfo {

		private final ConcurrentHashMap<String, List<PublishMessage>> receivedMessagesByTopic = new ConcurrentHashMap<String, List<PublishMessage>>();
		private final String clientId;
		private volatile MqttClient client;

		private boolean willRetained;
		private int willQos;
		private String willTopic;
		private String willMessage;

		private int keepAliveInterval = Integer.MIN_VALUE;

		ClientInfo(String clientId) {
			this.clientId = clientId;
		}

		void assertConnected() {
			if (client == null) {
				throw new RelishException("MQTT client %s is not connected", clientId);
			}
		}

		void assertNotConnected() {
			if (client != null) {
				throw new RelishException("MQTT client %s is already connected", clientId);
			}
		}

		PublishMessage getRequiredMessage(String topic, int index) {

			List<PublishMessage> msgs = receivedMessagesByTopic.get(topic);
			if (msgs == null) {
				throw new RelishException("MQTT client %s has not received message %d from topic %s", clientId, index, topic);
			}

			PublishMessage msg = msgs.get(index - 1);
			if (msg == null) {
				throw new RelishException("MQTT client %s has not received message %d from topic %s", clientId, index, topic);
			}

			return msg;
		}

		int getMessageCount(String topic) {

			List<PublishMessage> msgs = receivedMessagesByTopic.get(topic);
			return msgs == null ? 0 : msgs.size();
		}
	}
}
