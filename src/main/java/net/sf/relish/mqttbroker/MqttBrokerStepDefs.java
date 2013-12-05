package net.sf.relish.mqttbroker;

import static net.sf.relish.RelishUtil.*;
import static net.sf.relish.matcher.RelishMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import net.sf.relish.CountQuantifier;
import net.sf.relish.DataFormat;
import net.sf.relish.ExpandingArrayList;
import net.sf.relish.RelishUtil;
import net.sf.relish.RelishException;
import net.sf.relish.transformer.CountQuantifierTransformer;
import net.sf.relish.transformer.IsNullTransformer;
import net.sf.relish.transformer.NullSafeIntegerTransformer;
import net.sf.relish.transformer.StringToBooleanTransformer;
import net.sf.xenqtt.client.MqttClient;
import net.sf.xenqtt.client.MqttClientListener;
import net.sf.xenqtt.client.PublishMessage;
import net.sf.xenqtt.client.SyncMqttClient;
import net.sf.xenqtt.message.ConnectMessage;
import net.sf.xenqtt.message.ConnectReturnCode;
import net.sf.xenqtt.message.DisconnectMessage;
import net.sf.xenqtt.message.PubMessage;
import net.sf.xenqtt.message.QoS;
import net.sf.xenqtt.message.SubscribeMessage;
import net.sf.xenqtt.message.UnsubscribeMessage;
import net.sf.xenqtt.mockbroker.Client;
import net.sf.xenqtt.mockbroker.MockBroker;
import net.sf.xenqtt.mockbroker.MockBrokerHandler;
import cucumber.api.Transform;
import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Cucumber steps for mocking an MQTT broker
 */
public final class MqttBrokerStepDefs {

	private MockBroker broker;
	private MqttClient client;

	private final ConcurrentHashMap<String, ClientInfo> clientInfoByClientId = new ConcurrentHashMap<String, ClientInfo>();
	private final ConcurrentHashMap<String, List<PubMessage>> pubMessagesByTopic = new ConcurrentHashMap<String, List<PubMessage>>();

	@After
	public void after() {
		if (client != null) {
			try {
				client.close();
			} catch (Exception ignore) {
			}
		}
		if (broker != null) {
			broker.shutdown(5000);
		}
	}

	/**
	 * Adds a username/password the mock broker will accept.
	 * 
	 * @param username
	 *            The username to allow
	 * @param password
	 *            The password for the specified username.
	 */
	@Given("^MQTT broker allows user \"(\\S.*)\" access with password \"(\\S.*)\"$")
	public void mqttBrokerAllowsUserAccessWithPassword(String username, String password) {

		getRequiredBroker().addCredentials(username, password);
	}

	/**
	 * Starts the mock MQTT broker on the specified port and optionally allows anonymous access. If anonymous access is allowed then the broker will accept any
	 * username/password combination. By default anonymous access is allowed.
	 * 
	 * @param port
	 *            The port to start the broker on
	 * @param allowAnonymousAccess
	 *            Whether anonymous access is allowed
	 */
	@Given("^MQTT broker is running on port (\\d+)(?: with anonymous access (allowed|not allowed))?$")
	public void mqttBrokerIsRunningOnPort(int port, @Transform(AllowedTransformer.class) boolean allowAnonymousAccess) {

		if (broker != null) {
			throw new RelishException("You may not start the MQTT broker because it is already running at %s", broker.getURI());
		}

		broker = new MockBroker(new BrokerHandler(), 15, port, allowAnonymousAccess, true, 50);
		broker.init();
		String id = "MQT" + System.identityHashCode(broker);
		broker.addCredentials(id, id);
		client = new SyncMqttClient(broker.getURI(), new ClientListener(), 1);
		ConnectReturnCode returnCode = client.connect("MqttBrokerStepsClient", false, id, id);
		if (returnCode != ConnectReturnCode.ACCEPTED) {
			throw new RelishException("Unable to start internal Relish client to mock broker. Connection return code: " + returnCode);
		}
	}

	/**
	 * Makes the broker publish a message with a payload to the client
	 * 
	 * @param retained
	 *            If included the retained flag will be set in the message header
	 * @param qos
	 *            The QoS to publish the message at (0 or 1)
	 * @param topicName
	 *            The topic the message was published to
	 * @param format
	 *            The format of the message body
	 * @param body
	 *            The body of the message
	 */
	@When("^MQTT broker publishes a( retained)? message at QoS ([01]) to \"(\\S.*)\" as (XML|JSON|text|binary):$")
	public void mqttBrokerPublishesAMessage(@Transform(RetainedTransformer.class) boolean retained, int qos, String topicName, DataFormat format, String body) {

		QoS theQos = QoS.lookup(qos);
		byte[] payload = format.textToBytes(body);
		PublishMessage message = new PublishMessage(topicName, theQos, payload, retained);
		getRequiredClient().publish(message);
	}

	/**
	 * Makes the broker publish a message with a payload to the client. The payload is read from a file
	 * 
	 * @param retained
	 *            If included the retained flag will be set in the message header
	 * @param qos
	 *            The QoS to publish the message at (0 or 1)
	 * @param topicName
	 *            The topic the message was published to
	 * @param filename
	 *            The name of the file to read the payload from
	 */
	@When("^MQTT broker publishes a( retained)? message at QoS ([01]) to \"(\\S.*)\" from file \"(\\S.*)\"$")
	public void mqttBrokerPublishesAMessageFromFile(@Transform(RetainedTransformer.class) boolean retained, int qos, String topicName, String filename) {

		QoS theQos = QoS.lookup(qos);
		File file = new File(filename);
		if (!file.exists()) {
			throw new RelishException("File %s does not exist", filename);
		}
		byte[] payload = RelishUtil.getFileContents(file);
		PublishMessage message = new PublishMessage(topicName, theQos, payload, retained);
		getRequiredClient().publish(message);
	}

	/**
	 * Validates whether the retained flag is set on messages published by the client to the broker.
	 * 
	 * @param startIndex
	 *            The index of the first message in the range to validate. The first message s index 1, the second message is index 2, etc. If the client ID is
	 *            specified then the index is per client: the first message published to the topic by the client is index 1, the second message published to the
	 *            topic by the client is 2, etc. If no client ID is specified then the index is across all clients: the first message published to the topic by
	 *            any client is index 1, the second message published to the topic by any client is index 2, etc.
	 * @param endIndex
	 *            The index of the last message in the range to validate: 1 is the first message, 2 is the seconds message, etc. If null then startIndex is
	 *            used.
	 * @param clientId
	 *            The ID of the client that published the message
	 * @param topic
	 *            The topic the message was published to
	 * @param retained
	 *            Whether or not the retained flag is set in the message's header
	 */
	@Then("^MQTT broker messages? (\\d+)(?: thru (\\d+))? published(?: by \"(\\S.*)\")? to \"(\\S.*)\" should( not)? be retained$")
	public void mqttBrokerMessagePublishedShouldBeRetained(int startIndex, Integer endIndex, String clientId, String topic,
			@Transform(IsNullTransformer.class) boolean retained) {

		List<PubMessage> messages = clientId != null ? getClientInfo(clientId).getPubMessages(topic) : getPubMessages(topic);
		for (int i = startIndex; i <= getEndIndex(startIndex, endIndex); i++) {

			PubMessage message = getRequiredPubMessage(messages, i);
			assertThat(message.isRetain(), equalTo(retained), "MQTT published message %d retain flag does not match", i);
		}
	}

	/**
	 * Validates the QoS of messages published by the client to the broker.
	 * 
	 * @param startIndex
	 *            The index of the first message in the range to validate. The first message s index 1, the second message is index 2, etc. If the client ID is
	 *            specified then the index is per client: the first message published to the topic by the client is index 1, the second message published to the
	 *            topic by the client is 2, etc. If no client ID is specified then the index is across all clients: the first message published to the topic by
	 *            any client is index 1, the second message published to the topic by any client is index 2, etc.
	 * @param endIndex
	 *            The index of the last message in the range to validate: 1 is the first message, 2 is the seconds message, etc. If null then startIndex is
	 *            used.
	 * @param clientId
	 *            The ID of the client that published the message
	 * @param topic
	 *            The topic the message was published to
	 * @param qos
	 *            The QoS the message was published at
	 */
	@Then("^MQTT broker messages? (\\d+)(?: thru (\\d+))? published(?: by \"(\\S.*)\")? to \"(\\S.*)\" should have a QoS of ([01])$")
	public void mqttBrokerMessagePublishedShouldHaveAQosOf(int startIndex, Integer endIndex, String clientId, String topic, int qos) {

		List<PubMessage> messages = clientId != null ? getClientInfo(clientId).getPubMessages(topic) : getPubMessages(topic);
		for (int i = startIndex; i <= getEndIndex(startIndex, endIndex); i++) {

			PubMessage message = getRequiredPubMessage(messages, i);
			assertThat(message.getQoSLevel(), equalTo(qos), "MQTT published message %d QoS does not match", i);
		}
	}

	/**
	 * Validates the payload of messages published by the client to the broker.
	 * 
	 * @param startIndex
	 *            The index of the first message in the range to validate. The first message s index 1, the second message is index 2, etc. If the client ID is
	 *            specified then the index is per client: the first message published to the topic by the client is index 1, the second message published to the
	 *            topic by the client is 2, etc. If no client ID is specified then the index is across all clients: the first message published to the topic by
	 *            any client is index 1, the second message published to the topic by any client is index 2, etc.
	 * @param endIndex
	 *            The index of the last message in the range to validate: 1 is the first message, 2 is the seconds message, etc. If null then startIndex is
	 *            used.
	 * @param clientId
	 *            The ID of the client that published the message
	 * @param topic
	 *            The topic the message was published to
	 * @param format
	 *            The format the body is expected to be in
	 * @param bodyRegex
	 *            A regular expression which the message's body must match. If the format is "binary" then the body of the message is hexadecimal text. If the
	 *            body contains multiple lines each line will have all leading and trailing whitespace removed then all lines will be concatenated into a single
	 *            line.
	 */
	@Then("^MQTT broker messages? (\\d+)(?: thru (\\d+))? published(?: by \"(\\S.*)\")? to \"(\\S.*)\" should match this (XML|JSON|text|binary):$")
	public void mqttBrokerMessagePublishedShouldMatch(int startIndex, Integer endIndex, String clientId, String topic, DataFormat format, String bodyRegex) {

		List<PubMessage> messages = clientId != null ? getClientInfo(clientId).getPubMessages(topic) : getPubMessages(topic);
		for (int i = startIndex; i <= getEndIndex(startIndex, endIndex); i++) {

			PubMessage message = getRequiredPubMessage(messages, i);
			String bodyText = format.bytesToText(message.getPayload());
			bodyRegex = format.normalizeRegex(bodyRegex);
			assertThat(bodyText, matches(bodyRegex), "MQTT published message %d payload does not match", i);
		}
	}

	/**
	 * Validates the number of messages published to a topic. If client ID is specified then the number of messages published to the topic by that client is
	 * validated; otherwise, the number of messages published to the topic by all clients is validated.
	 * 
	 * @param countQualifier
	 *            How the count will be evaluated
	 * @param count
	 *            The number of messages that should have been published
	 * @param clientId
	 *            The ID of the client that published the messages
	 * @param topic
	 *            The topic the messages were published to
	 */
	@Then("^MQTT broker should have (exactly|at least|at most) (\\d+) messages? published(?: by \"(\\S.*)\")? to \"(\\S.*)\"$")
	public void mqttBrokerShouldHaveMessagePublishedCount(@Transform(CountQuantifierTransformer.class) CountQuantifier countQuantifier, int count,
			String clientId, String topic) {

		final List<PubMessage> messages = clientId != null ? getClientInfo(clientId).getPubMessages(topic) : getPubMessages(topic);

		assertThat(messages.size(), countQuantifier.newMatcher(count), "MQTT published message count does not match");
	}

	/**
	 * Waits for a specified number of messages to be published to a topic. If client ID is specified then the number of messages published to the topic by that
	 * client is counted; otherwise, the number of messages published to the topic by all clients is counted.
	 * 
	 * @param count
	 *            The number of messages to wait for
	 * @param clientId
	 *            The ID of the client that published the messages
	 * @param topic
	 *            The topic the messages are published to
	 * @param timeout
	 *            The value of the max time to wait for the messages
	 * @param timeoutUnit
	 *            The unit of measure for the timeout value (seconds or milliseconds)
	 */
	@Then("^MQTT broker should have at least (\\d+) messages? published(?: by \"(\\S.*)\")? to \"(\\S.*)\" within (\\d+) (seconds|milliseconds)$")
	public void mqttBrokerShouldHaveMessagePublishedCountWithin(int count, String clientId, String topic, int timeout, TimeUnit timeoutUnit) {

		final List<PubMessage> messages = clientId != null ? getClientInfo(clientId).getPubMessages(topic) : getPubMessages(topic);

		Callable<Integer> currentMessageCount = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return messages.size();
			}
		};
		assertThatWithin(timeout, timeoutUnit, currentMessageCount, gte(count), "MQTT client did not receive enough messages");
	}

	/**
	 * Validates the specified client is connected. This step will wait up to the specified timeout for the client to be connected.
	 * 
	 * @param clientId
	 *            The ID of the client that is connected
	 * @param timeout
	 *            The value of the max time to wait for the client to be connected
	 * @param timeoutUnit
	 *            The unit of measure for the timeout value (seconds or milliseconds)
	 */
	@Then("^MQTT broker client \"(\\S.*)\" should be connected(?: within (\\d+) (seconds|milliseconds))?$")
	public void mqttBrokerClientShouldBeConnected(String clientId, @Transform(NullSafeIntegerTransformer.class) int timeout, TimeUnit timeoutUnit) {

		final ClientInfo clientInfo = getClientInfo(clientId);
		Callable<Boolean> clientConnected = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return clientInfo.connectMessage != null;
			}
		};
		assertThatWithin(timeout, timeoutUnit, clientConnected, equalTo(Boolean.TRUE), "MQTT client %s never connected", clientId);
	}

	/**
	 * Validates the keep alive interval for the specified client
	 * 
	 * @param clientId
	 *            The ID of the client to validate the keep alive interval for
	 * @param keepAliveInterval
	 *            The expected keep alive interval, in seconds
	 */
	@Then("^MQTT broker client \"(\\S.*)\" keep alive interval should be (\\d+) seconds$")
	public void mqttBrokerClientKeepAliveIntervalShouldBe(String clientId, int keepAliveInterval) {
		assertThat(getConnectMessage(clientId).getKeepAliveSeconds(), equalTo(keepAliveInterval), "MQTT client %s keep alive interval does not match", clientId);
	}

	/**
	 * Validates the username for the specified client
	 * 
	 * @param clientId
	 *            The ID of the client to validate the username for
	 * @param username
	 *            The expected username
	 */
	@Then("^MQTT broker client \"(\\S.*)\" username should be \"(\\S.*)\"$")
	public void mqttBrokerClientUserNameShouldBe(String clientId, String username) {
		assertThat(getConnectMessage(clientId).getUserName(), equalTo(username), "MQTT client username does not match");
	}

	/**
	 * Validates the password for the specified client
	 * 
	 * @param clientId
	 *            The ID of the client to validate the password for
	 * @param password
	 *            The expected password
	 */
	@Then("^MQTT broker client \"(\\S.*)\" password should be \"(\\S.*)\"$")
	public void mqttBrokerClientPasswordShouldBe(String clientId, String password) {
		assertThat(getConnectMessage(clientId).getPassword(), equalTo(password), "MQTT client password does not match");
	}

	/**
	 * Validates whether the specified client connected with a clean session
	 * 
	 * @param clientId
	 *            The ID of the client to validate the clean session status of
	 * @param should
	 *            <ul>
	 *            <li>should: if the client should have a clean session</li>
	 *            <li>should not: if the client should not have a clean session</li>
	 *            </ul>
	 */
	@Then("^MQTT broker client \"(\\S.*)\" (should|should not) have a clean session$")
	public void mqttBrokerClientCleanSession(String clientId, @Transform(ShouldTransformer.class) boolean should) {
		assertThat(getConnectMessage(clientId).isCleanSession(), equalTo(should), "MQTT client clean session flag does not match");
	}

	/**
	 * Validates the Will Message configured for the specified client. This step does not validate the message body.
	 * 
	 * @param clientId
	 *            The ID of the client to validate the Will Message for
	 * @param retained
	 *            Whether or not the retained flag is set in the message's header
	 * @param qos
	 *            The QoS the message will be published at
	 * @param topic
	 *            The topic the message will be published to
	 */
	@Then("^MQTT broker client \"(\\S.*)\" will message should be(?!(?:\\s*$))( retained| not retained)?(?: at QoS ([01]))?(?: on \"(\\S.*)\")?$")
	public void mqttBrokerClientWillMessageShouldBe(String clientId, @Transform(OptionalRetainedTransformer.class) Boolean retained, Integer qos, String topic) {
		mqttBrokerClientWillMessageWithBodyShouldBe(clientId, retained, qos, topic, null, null);
	}

	/**
	 * Validates the Will Message configured for the specified client. This step does not validate the message body.
	 * 
	 * @param clientId
	 *            The ID of the client to validate the Will Message for
	 * @param retained
	 *            Whether or not the retained flag is set in the message's header
	 * @param qos
	 *            The QoS the message will be published at
	 * @param topic
	 *            The topic the message will be published to
	 * @param comparison
	 *            The type of comparison to make when validating the body:
	 *            <ul>
	 *            <li>as: The body will be compared for equality</li>
	 *            <li>matching: The body specified in the DSL is a regular expression the body of the message must match</li>
	 *            </ul>
	 * @param format
	 *            The format the body is expected to be in
	 * @param bodyRegex
	 *            A regular expression which the message's body must match. If the format is "binary" then the body of the message is hexadecimal text. If the
	 *            body contains multiple lines each line will have all leading and trailing whitespace removed then all lines will be concatenated into a single
	 *            line.
	 */
	@Then("^MQTT broker client \"(\\S.*)\" will message should(?: be)?( retained| not retained)?(?: at QoS ([01]))?(?: on \"(\\S.*)\")? match(?:ing)? this (XML|JSON|text):$")
	public void mqttBrokerClientWillMessageWithBodyShouldBe(String clientId, @Transform(OptionalRetainedTransformer.class) Boolean retained, Integer qos,
			String topic, DataFormat format, String bodyRegex) {
		ConnectMessage message = getConnectMessage(clientId);
		if (retained != null) {
			assertThat(message.isWillRetain(), equalTo(retained), "MQTT client %s Will Message retain flag does not match", clientId);
		}

		if (qos != null) {
			assertThat(message.getWillQoSLevel(), equalTo(qos), "MQTT client %s Will Message QoS does not match", clientId);
		}

		if (format != null) {
			String bodyText = format.bytesToText(message.getWillMessage().getBytes(DataFormat.ASCII));
			bodyRegex = format.normalizeRegex(bodyRegex);
			assertThat(bodyText, matches(bodyRegex), "MQTT client %s Will Message payload does not match", clientId);
		}
	}

	/**
	 * Validates the specified client is subscribed to the topic
	 * 
	 * @param clientId
	 *            The ID of the client that should be subscribed
	 * @param topic
	 *            The topic the client is subscribed to
	 * @param qos
	 *            The QoS the client is subscribed to the topic at
	 * @param timeout
	 *            The value of the max time to wait for the client to be subscribed to the topic
	 * @param timeoutUnit
	 *            The unit of measure for the timeout value (seconds or milliseconds)
	 */
	@Then("^MQTT broker client \"(\\S.*)\" should be subscribed to \"(\\S.*)\"(?: at QoS ([01]))?(?: within (\\d+) (seconds|milliseconds))?$")
	public void mqttBrokerClientShouldBeSubscribedTo(String clientId, final String topic, Integer qos,
			@Transform(NullSafeIntegerTransformer.class) int timeout, TimeUnit timeoutUnit) {

		final ClientInfo clientInfo = getConnectedClientInfo(clientId);
		Callable<Boolean> hasSubscribed = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return clientInfo.subscribedQosByTopic.containsKey(topic);
			}
		};
		assertThatWithin(timeout, timeoutUnit, hasSubscribed, equalTo(Boolean.TRUE), "MQTT client %s is has never subscribed to topic %s", clientId, topic);
		if (qos != null) {
			QoS theQos = QoS.lookup(qos);
			assertThat(clientInfo.subscribedQosByTopic.get(topic), equalTo(theQos), "MQTT client %s requested subscription QoS does  not match", clientId);
		}
	}

	/**
	 * Validates the client is unsubscribed from the topic
	 * 
	 * @param clientId
	 *            The ID of the client that should be unsubscribed
	 * @param topic
	 *            The topic the client unsubscribed from
	 * @param timeout
	 *            The value of the max time to wait for the client to be unsubscribed from the topic
	 * @param timeoutUnit
	 *            The unit of measure for the timeout value (seconds or milliseconds)
	 */
	@Then("^MQTT broker client \"(\\S.*)\" should be unsubscribed from \"(\\S.*)\"(?: within (\\d+) (seconds|milliseconds))?$")
	public void mqttBrokerClientShouldBeUnsubscribedFrom(String clientId, final String topic, @Transform(NullSafeIntegerTransformer.class) int timeout,
			TimeUnit timeoutUnit) {

		final ClientInfo clientInfo = getConnectedClientInfo(clientId);
		Callable<Boolean> hasUnubscribed = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return clientInfo.unsubscribedTopics.contains(topic);
			}
		};
		assertThatWithin(timeout, timeoutUnit, hasUnubscribed, equalTo(Boolean.TRUE), "MQTT client %s has never unsubscribed from topic %s", clientId, topic);
	}

	/**
	 * Validate the client is disconnected from the broker
	 * 
	 * @param clientId
	 *            The ID of the client that should be disconnected
	 * @param timeout
	 *            The value of the max time to wait for the client to be disconnected
	 * @param timeoutUnit
	 *            The unit of measure for the timeout value (seconds or milliseconds)
	 */
	@Then("^MQTT broker client \"(\\S.*)\" should be disconnected(?: within (\\d+) (seconds|milliseconds))?$")
	public void mqttBrokerClientShouldBeDisconnected(String clientId, @Transform(NullSafeIntegerTransformer.class) int timeout, TimeUnit timeoutUnit) {

		final ClientInfo clientInfo = getConnectedClientInfo(clientId);
		Callable<Boolean> hasDisconnected = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return clientInfo.disconnected;
			}
		};
		assertThatWithin(timeout, timeoutUnit, hasDisconnected, equalTo(Boolean.TRUE), "MQTT client %s has never disconnected", clientId);
	}

	private MockBroker getRequiredBroker() {
		if (broker == null) {
			throw new RelishException("The MQTT broker is not running");
		}
		return broker;
	}

	private MqttClient getRequiredClient() {
		if (client == null) {
			throw new RelishException("The MQTT broker is not running");
		}
		return client;
	}

	private PubMessage getRequiredPubMessage(List<PubMessage> messages, int i) {

		PubMessage message = messages.get(i - 1);
		if (message == null) {
			throw new RelishException("MQTT published message %d does not exist", i);
		}
		return message;
	}

	private ConnectMessage getConnectMessage(String clientId) {

		ClientInfo clientInfo = clientInfoByClientId.get(clientId);
		if (clientInfo == null || clientInfo.connectMessage == null) {
			throw new RelishException("Client %s never connected", clientId);
		}

		return clientInfo.connectMessage;
	}

	private ClientInfo getConnectedClientInfo(String clientId) {

		ClientInfo clientInfo = clientInfoByClientId.get(clientId);
		if (clientInfo == null || clientInfo.connectMessage == null) {
			throw new RelishException("Client %s is not connected", clientId);
		}

		return clientInfo;
	}

	private ClientInfo getClientInfo(String clientId) {

		ClientInfo clientInfo = clientInfoByClientId.get(clientId);
		if (clientInfo == null) {
			clientInfo = new ClientInfo();
			ClientInfo currentInfo = clientInfoByClientId.putIfAbsent(clientId, clientInfo);
			if (currentInfo != null) {
				clientInfo = currentInfo;
			}
		}

		return clientInfo;
	}

	private List<PubMessage> getPubMessages(String topic) {
		List<PubMessage> messages = pubMessagesByTopic.get(topic);
		if (messages == null) {
			messages = Collections.synchronizedList(new ExpandingArrayList<PubMessage>());
			List<PubMessage> current = pubMessagesByTopic.putIfAbsent(topic, messages);
			if (current != null) {
				messages = current;
			}
		}
		return messages;
	}

	private int getEndIndex(int startIndex, Integer endIndex) {
		return endIndex == null ? startIndex : endIndex;
	}

	private final static class ClientListener implements MqttClientListener {

		/**
		 * @see net.sf.relish.client.MqttClientListener#publishReceived(net.sf.relish.client.MqttClient, net.sf.relish.client.PublishMessage)
		 */
		@Override
		public void publishReceived(MqttClient client, PublishMessage message) {
		}

		/**
		 * @see net.sf.relish.client.MqttClientListener#disconnected(net.sf.relish.client.MqttClient, java.lang.Throwable, boolean)
		 */
		@Override
		public void disconnected(MqttClient client, Throwable cause, boolean reconnecting) {
		}
	}

	private final class BrokerHandler extends MockBrokerHandler {

		/**
		 * @see net.sf.relish.mockbroker.MockBrokerHandler#connect(net.sf.relish.mockbroker.Client, net.sf.relish.message.ConnectMessage)
		 */
		@Override
		public boolean connect(Client client, ConnectMessage message) throws Exception {
			getClientInfo(client.getClientId()).connectMessage = message;
			return false;
		}

		/**
		 * @see net.sf.relish.mockbroker.MockBrokerHandler#publish(net.sf.relish.mockbroker.Client, net.sf.relish.message.PubMessage)
		 */
		@Override
		public boolean publish(Client client, PubMessage message) throws Exception {
			getClientInfo(client.getClientId()).getPubMessages(message.getTopicName()).add(message);
			getPubMessages(message.getTopicName()).add(message);
			return false;
		}

		/**
		 * @see net.sf.relish.mockbroker.MockBrokerHandler#subscribe(net.sf.relish.mockbroker.Client, net.sf.relish.message.SubscribeMessage)
		 */
		@Override
		public boolean subscribe(Client client, SubscribeMessage message) throws Exception {

			Map<String, QoS> subscribedQosByTopic = getClientInfo(client.getClientId()).subscribedQosByTopic;
			String[] topics = message.getTopics();
			QoS[] qoses = message.getRequestedQoSes();
			for (int i = 0; i < topics.length; i++) {
				subscribedQosByTopic.put(topics[i], qoses[i]);
			}
			return false;
		}

		/**
		 * @see net.sf.relish.mockbroker.MockBrokerHandler#unsubscribe(net.sf.relish.mockbroker.Client, net.sf.relish.message.UnsubscribeMessage)
		 */
		@Override
		public boolean unsubscribe(Client client, UnsubscribeMessage message) throws Exception {

			Set<String> unsubscribedTopics = getClientInfo(client.getClientId()).unsubscribedTopics;
			for (String topic : message.getTopics()) {
				unsubscribedTopics.add(topic);
			}
			return false;
		}

		/**
		 * @see net.sf.relish.mockbroker.MockBrokerHandler#disconnect(net.sf.relish.mockbroker.Client, net.sf.relish.message.DisconnectMessage)
		 */
		@Override
		public void disconnect(Client client, DisconnectMessage message) throws Exception {

			getClientInfo(client.getClientId()).disconnected = true;
		}
	}

	private final static class ClientInfo {

		final Map<String, QoS> subscribedQosByTopic = new ConcurrentHashMap<String, QoS>();
		final Set<String> unsubscribedTopics = new CopyOnWriteArraySet<String>();
		final ConcurrentHashMap<String, List<PubMessage>> pubMessagesByTopic = new ConcurrentHashMap<String, List<PubMessage>>();
		volatile ConnectMessage connectMessage;
		volatile boolean disconnected;

		List<PubMessage> getPubMessages(String topic) {
			List<PubMessage> messages = pubMessagesByTopic.get(topic);
			if (messages == null) {
				messages = Collections.synchronizedList(new ExpandingArrayList<PubMessage>());
				List<PubMessage> current = pubMessagesByTopic.putIfAbsent(topic, messages);
				if (current != null) {
					messages = current;
				}
			}
			return messages;
		}
	}

	public final static class ShouldTransformer extends StringToBooleanTransformer<Boolean> {

		public ShouldTransformer() {
			super("should", "should not", true);
		}
	}

	public final static class AllowedTransformer extends StringToBooleanTransformer<Boolean> {

		public AllowedTransformer() {
			super("allowed", "not allowed", true);
		}
	}

	public final static class RetainedTransformer extends StringToBooleanTransformer<Boolean> {

		public RetainedTransformer() {
			super("retained", "not retained", true);
		}
	}

	public final static class OptionalRetainedTransformer extends StringToBooleanTransformer<Boolean> {

		public OptionalRetainedTransformer() {
			super("retained", "not retained", false);
		}
	}
}
