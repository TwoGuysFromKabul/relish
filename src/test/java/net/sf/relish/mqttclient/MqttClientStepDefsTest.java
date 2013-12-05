package net.sf.relish.mqttclient;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import net.sf.relish.CountQuantifier;
import net.sf.relish.DataFormat;
import net.sf.relish.RelishUtil;
import net.sf.relish.RelishException;
import net.sf.xenqtt.message.ConnectMessage;
import net.sf.xenqtt.message.DisconnectMessage;
import net.sf.xenqtt.message.MqttMessage;
import net.sf.xenqtt.message.PubAckMessage;
import net.sf.xenqtt.message.PubMessage;
import net.sf.xenqtt.message.QoS;
import net.sf.xenqtt.message.SubscribeMessage;
import net.sf.xenqtt.message.UnsubscribeMessage;
import net.sf.xenqtt.mockbroker.Client;
import net.sf.xenqtt.mockbroker.MockBroker;
import net.sf.xenqtt.mockbroker.MockBrokerHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MqttClientStepDefsTest {

	File testFile = new File("test.txt");
	@Mock MockBrokerHandler handler;
	@Captor ArgumentCaptor<MqttMessage> captor;
	MockBroker broker;
	MqttClientStepDefs steps = new MqttClientStepDefs();

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		broker = new MockBroker(handler);
		broker.init();
	}

	@After
	public void after() {

		testFile.delete();
		steps.after();
	}

	@Test
	public void testAfter_NoConnections() throws Exception {

		steps.after();
	}

	@Test
	public void testAfter_WithConnections() throws Exception {

		connect();

		steps.after();

		verify(handler, timeout(1000)).channelClosed(any(Client.class), isNull(Throwable.class));
	}

	@Test
	public void testMqttClientWillMessage_NotConnected() throws Exception {

		steps.mqttClientWillMessage("foo", true, 1, "my/topic", DataFormat.TEXT, "hello world");
		steps.mqttClientIsConnected("foo", false, broker.getURI(), null, null);

		verify(handler, timeout(1000)).connect(any(Client.class), (ConnectMessage) captor.capture());
		ConnectMessage msg = (ConnectMessage) captor.getValue();

		assertTrue(msg.isWillMessageFlag());
		assertTrue(msg.isWillRetain());
		assertEquals(1, msg.getWillQoSLevel());
		assertEquals("my/topic", msg.getWillTopic());
		assertEquals("hello world", msg.getWillMessage());
	}

	@Test(expected = RelishException.class)
	public void testMqttClientWillMessage_AlreadyConnected() throws Exception {

		steps.mqttClientIsConnected("foo", false, broker.getURI(), null, null);

		verify(handler, timeout(1000)).connect(any(Client.class), any(ConnectMessage.class));

		steps.mqttClientWillMessage("foo", true, 1, "my/topic", DataFormat.TEXT, "hello world");
	}

	@Test
	public void testMqttClientUsesAKeepAliveIntervalOf_NotConnected() throws Exception {

		steps.mqttClientUsesAKeepAliveIntervalOf("foo", 1234);
		steps.mqttClientIsConnected("foo", false, broker.getURI(), null, null);

		verify(handler, timeout(1000)).connect(any(Client.class), (ConnectMessage) captor.capture());
		ConnectMessage msg = (ConnectMessage) captor.getValue();
		assertEquals(1234, msg.getKeepAliveSeconds());
	}

	@Test(expected = RelishException.class)
	public void testMqttClientUsesAKeepAliveIntervalOf_AlreadyConnected() throws Exception {

		steps.mqttClientIsConnected("foo", false, broker.getURI(), null, null);

		verify(handler, timeout(1000)).connect(any(Client.class), any(ConnectMessage.class));

		steps.mqttClientUsesAKeepAliveIntervalOf("foo", 1234);
	}

	@Test
	public void testMqttClientIsConnected_Success_NoCleanSession_NoCredentials() throws Exception {

		steps.mqttClientIsConnected("foo", false, broker.getURI(), null, null);

		verify(handler, timeout(1000)).connect(any(Client.class), (ConnectMessage) captor.capture());
		ConnectMessage msg = (ConnectMessage) captor.getValue();
		assertEquals("foo", msg.getClientId());
		assertFalse(msg.isCleanSession());
		assertFalse(msg.isUserNameFlag());
		assertFalse(msg.isPasswordFlag());
	}

	@Test
	public void testMqttClientIsConnected_Success_CleanSession_Credentials() throws Exception {

		broker.addCredentials("myuser", "mypass");
		steps.mqttClientIsConnected("foo", true, broker.getURI(), "myuser", "mypass");

		verify(handler, timeout(1000)).connect(any(Client.class), (ConnectMessage) captor.capture());
		ConnectMessage msg = (ConnectMessage) captor.getValue();
		assertEquals("foo", msg.getClientId());
		assertTrue(msg.isCleanSession());
		assertTrue(msg.isUserNameFlag());
		assertEquals("myuser", msg.getUserName());
		assertTrue(msg.isPasswordFlag());
		assertEquals("mypass", msg.getPassword());
	}

	@Test(expected = RelishException.class)
	public void testMqttClientIsConnected_ClientAlreadyConnected() throws Exception {

		steps.mqttClientIsConnected("foo", false, broker.getURI(), null, null);
		verify(handler, timeout(1000)).connect(any(Client.class), any(ConnectMessage.class));

		steps.mqttClientIsConnected("foo", false, broker.getURI(), null, null);
	}

	@Test
	public void testMqttClientIsConnected_MultipleClients() throws Exception {

		steps.mqttClientIsConnected("foo", false, broker.getURI(), null, null);
		verify(handler, timeout(1000)).connect(any(Client.class), (ConnectMessage) captor.capture());
		assertEquals("foo", ((ConnectMessage) captor.getValue()).getClientId());

		captor = ArgumentCaptor.forClass(MqttMessage.class);
		reset(handler);
		steps.mqttClientIsConnected("bar", false, broker.getURI(), null, null);
		verify(handler, timeout(1000)).connect(any(Client.class), (ConnectMessage) captor.capture());
		assertEquals("bar", ((ConnectMessage) captor.getValue()).getClientId());
	}

	@Test(expected = RelishException.class)
	public void testMqttClientIsDisconnected_ClientNotConnected() throws Exception {

		steps.mqttClientIsDisconnected("foo");
	}

	@Test
	public void testMqttClientIsDisconnected_Success() throws Exception {

		connect();
		steps.mqttClientIsDisconnected("foo");
		verify(handler, timeout(1000)).disconnect(any(Client.class), any(DisconnectMessage.class));
	}

	@Test(expected = RelishException.class)
	public void testMqttClientIsSubscribedToTopic_ClientNotConnected() throws Exception {

		steps.mqttClientIsSubscribedToTopic("foo", "my/topic", 1);
	}

	@Test
	public void testMqttClientIsSubscribedToTopic_Success() throws Exception {

		connect();
		steps.mqttClientIsSubscribedToTopic("foo", "my/topic", 1);

		verify(handler, timeout(1000)).subscribe(any(Client.class), (SubscribeMessage) captor.capture());
		SubscribeMessage msg = (SubscribeMessage) captor.getValue();
		assertArrayEquals(new QoS[] { QoS.AT_LEAST_ONCE }, msg.getRequestedQoSes());
		assertArrayEquals(new String[] { "my/topic" }, msg.getTopics());
	}

	@Test(expected = RelishException.class)
	public void testMqttClientIsUnsubscribedFromTopic_NotConnected() throws Exception {

		steps.mqttClientIsUnubscribedFromTopic("foo", "my/topic");
	}

	@Test
	public void testMqttClientIsUnsubscribedFromTopic_Success() throws Exception {

		connect();
		steps.mqttClientIsUnubscribedFromTopic("foo", "my/topic");
		verify(handler, timeout(1000)).unsubscribe(any(Client.class), (UnsubscribeMessage) captor.capture());
		UnsubscribeMessage msg = (UnsubscribeMessage) captor.getValue();
		assertArrayEquals(new String[] { "my/topic" }, msg.getTopics());
	}

	@Test(expected = RelishException.class)
	public void testMqttClientPublishesAMessage_NotConnected() throws Exception {

		steps.mqttClientPublishesAMessage("foo", false, 1, "my/topic", DataFormat.TEXT, "hello world");
	}

	@Test
	public void testMqttClientPublishesAMessage_Success_NotRetained() throws Exception {

		connect();
		steps.mqttClientPublishesAMessage("foo", false, 1, "my/topic", DataFormat.TEXT, "hello world");
		verify(handler, timeout(1000)).publish(any(Client.class), (PubMessage) captor.capture());
		PubMessage msg = (PubMessage) captor.getValue();
		assertFalse(msg.isRetain());
		assertEquals(1, msg.getQoSLevel());
		assertEquals("my/topic", msg.getTopicName());
		assertEquals("hello world", new String(msg.getPayload(), Charset.forName("UTF8")));
	}

	@Test
	public void testMqttClientPublishesAMessage_Success_Retained() throws Exception {

		connect();
		steps.mqttClientPublishesAMessage("foo", true, 0, "my/topic", DataFormat.TEXT, "hello world");
		verify(handler, timeout(1000)).publish(any(Client.class), (PubMessage) captor.capture());
		PubMessage msg = (PubMessage) captor.getValue();
		assertTrue(msg.isRetain());
		assertEquals(0, msg.getQoSLevel());
		assertEquals("my/topic", msg.getTopicName());
		assertEquals("hello world", new String(msg.getPayload(), Charset.forName("UTF8")));
	}

	@Test(expected = RelishException.class)
	public void testMqttClientPublishesAMessageFromFile_NotConnected() throws Exception {

		steps.mqttClientPublishesAMessageFromFile("foo", false, 1, "my/topic", testFile.getName());
	}

	@Test(expected = RelishException.class)
	public void testMqttClientPublishesAMessageFromFile_FileDoesNotExist() throws Exception {

		steps.mqttClientPublishesAMessageFromFile("foo", false, 1, "my/topic", "not a file");
	}

	@Test
	public void testMqttClientPublishesAMessageFromFile_Success_NotRetained() throws Exception {

		RelishUtil.writeToFile(testFile, new ByteArrayInputStream("hello world".getBytes(Charset.forName("UTF8"))));
		connect();
		steps.mqttClientPublishesAMessageFromFile("foo", false, 1, "my/topic", testFile.getName());
		verify(handler, timeout(1000)).publish(any(Client.class), (PubMessage) captor.capture());
		PubMessage msg = (PubMessage) captor.getValue();
		assertFalse(msg.isRetain());
		assertEquals(1, msg.getQoSLevel());
		assertEquals("my/topic", msg.getTopicName());
		assertEquals("hello world", new String(msg.getPayload(), Charset.forName("UTF8")));
	}

	@Test
	public void testMqttClientPublishesAMessageFromFile_Success_Retained() throws Exception {

		RelishUtil.writeToFile(testFile, new ByteArrayInputStream("hello world".getBytes(Charset.forName("UTF8"))));
		connect();
		steps.mqttClientPublishesAMessageFromFile("foo", true, 0, "my/topic", testFile.getName());
		verify(handler, timeout(1000)).publish(any(Client.class), (PubMessage) captor.capture());
		PubMessage msg = (PubMessage) captor.getValue();
		assertTrue(msg.isRetain());
		assertEquals(0, msg.getQoSLevel());
		assertEquals("my/topic", msg.getTopicName());
		assertEquals("hello world", new String(msg.getPayload(), Charset.forName("UTF8")));
	}

	@Test(expected = RelishException.class)
	public void testMqttClientMessageFromShouldBeRetained_MessageNotFound() throws Exception {

		steps.mqttClientMessageFromShouldBeRetained("foo", 1, 1, "my/topic", false);
	}

	@Test
	public void testMqttClientMessageFromShouldBeRetained_Retained_Matches() throws Exception {

		Client client = connect();
		publish(client, QoS.AT_LEAST_ONCE, true);
		steps.mqttClientMessageFromShouldBeRetained("foo", 1, 1, "my/topic", true);
	}

	@Test(expected = AssertionError.class)
	public void testMqttClientMessageFromShouldBeRetained_Retained_DoesNotMatch() throws Exception {

		Client client = connect();
		publish(client, QoS.AT_LEAST_ONCE, false);
		steps.mqttClientMessageFromShouldBeRetained("foo", 1, 1, "my/topic", true);
	}

	@Test
	public void testMqttClientMessageFromShouldBeRetained_NotRetained_Matches() throws Exception {

		Client client = connect();
		publish(client, QoS.AT_LEAST_ONCE, false);
		steps.mqttClientMessageFromShouldBeRetained("foo", 1, 1, "my/topic", false);
	}

	@Test(expected = AssertionError.class)
	public void testMqttClientMessageFromShouldBeRetained_NotRetained_DoesNotMatch() throws Exception {

		Client client = connect();
		publish(client, QoS.AT_LEAST_ONCE, true);
		steps.mqttClientMessageFromShouldBeRetained("foo", 1, 1, "my/topic", false);
	}

	@Test(expected = RelishException.class)
	public void testMqttClientMessageFromShouldHaveAQosOf_MessageNotFound() throws Exception {

		steps.mqttClientMessageFromShouldHaveAQosOf("foo", 1, 1, "my/topic", 1);
	}

	@Test
	public void testMqttClientMessageFromShouldHaveAQosOf_Matches() throws Exception {

		Client client = connect();
		publish(client, QoS.AT_LEAST_ONCE, false);
		steps.mqttClientMessageFromShouldHaveAQosOf("foo", 1, 1, "my/topic", 1);
	}

	@Test(expected = AssertionError.class)
	public void testMqttClientMessageFromShouldHaveAQosOf_DoesNotMatch() throws Exception {

		Client client = connect();
		publish(client, QoS.AT_MOST_ONCE, false);
		steps.mqttClientMessageFromShouldHaveAQosOf("foo", 1, 1, "my/topic", 1);
	}

	@Test(expected = RelishException.class)
	public void testMqttClientMessageFromShouldMatch_MessageNotFound() throws Exception {

		steps.mqttClientMessageFromShouldMatch("foo", 1, 1, "my/topic", DataFormat.TEXT, "hello world");
	}

	@Test
	public void testMqttClientMessageFromShouldMatch_Matches() throws Exception {

		Client client = connect();
		publish(client, QoS.AT_LEAST_ONCE, false);
		steps.mqttClientMessageFromShouldMatch("foo", 1, 1, "my/topic", DataFormat.TEXT, "hello world");
	}

	@Test(expected = AssertionError.class)
	public void testMqttClientMessageFromShouldMatch_DoesNotMatch() throws Exception {

		Client client = connect();
		publish(client, QoS.AT_LEAST_ONCE, false);
		steps.mqttClientMessageFromShouldMatch("foo", 1, 1, "my/topic", DataFormat.TEXT, "crapola");
	}

	@Test
	public void testMqttClientShouldHaveMessageFromCount_NoMessages_Matches() throws Exception {

		steps.mqttClientShouldHaveMessageFromCount("foo", CountQuantifier.EXACTLY, 0, "my/topic");
	}

	@Test
	public void testMqttClientShouldHaveMessageFromCount_WithMessages_Matches() throws Exception {

		Client client = connect();
		publish(client, QoS.AT_LEAST_ONCE, false);
		steps.mqttClientShouldHaveMessageFromCount("foo", CountQuantifier.EXACTLY, 1, "my/topic");
	}

	@Test(expected = AssertionError.class)
	public void testMqttClientShouldHaveMessageFromCount_DoesNotMatch() throws Exception {

		Client client = connect();
		publish(client, QoS.AT_LEAST_ONCE, false);
		steps.mqttClientShouldHaveMessageFromCount("foo", CountQuantifier.EXACTLY, 2, "my/topic");
	}

	@Test
	public void testMqttClientShouldHaveMessageFromCountWithin_Matches() throws Exception {

		Client client = connect();
		publish(client, QoS.AT_LEAST_ONCE, false);
		steps.mqttClientShouldHaveMessageFromCountWithin("foo", 1, "my/topic", 1, TimeUnit.SECONDS);
	}

	@Test(expected = AssertionError.class)
	public void testMqttClientShouldHaveMessageFromCountWithin_TimesOut() throws Exception {

		steps.mqttClientShouldHaveMessageFromCountWithin("foo", 1, "my/topic", 1, TimeUnit.SECONDS);
	}

	private Client connect() throws Exception {
		steps.mqttClientIsConnected("foo", false, broker.getURI(), null, null);
		ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
		verify(handler, timeout(1000)).connect(clientCaptor.capture(), any(ConnectMessage.class));
		return clientCaptor.getValue();
	}

	private void publish(Client client, QoS qos, boolean retain) throws Exception {

		client.send(new PubMessage(qos, retain, "my/topic", 0, "hello world".getBytes(Charset.forName("UTF8"))));
		verify(handler, timeout(1000)).pubAck(same(client), (PubAckMessage) captor.capture());
	}
}
