package net.sf.relish.mqttbroker;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.ConnectException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import net.sf.relish.CountQuantifier;
import net.sf.relish.DataFormat;
import net.sf.relish.RelishException;
import net.sf.relish.RelishUtil;
import net.sf.relish.rule.ElapsedTime;
import net.xenqtt.MqttInvocationException;
import net.xenqtt.client.MqttClient;
import net.xenqtt.client.MqttClientConfig;
import net.xenqtt.client.MqttClientListener;
import net.xenqtt.client.PublishMessage;
import net.xenqtt.client.Subscription;
import net.xenqtt.client.SyncMqttClient;
import net.xenqtt.message.ConnectReturnCode;
import net.xenqtt.message.QoS;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MqttBrokerStepDefsTest {

	@Rule public final ElapsedTime elapsedTime = new ElapsedTime();

	@Mock MqttClientListener listener;
	@Captor ArgumentCaptor<PublishMessage> messageCaptor;

	File testFile = new File("test.txt");
	MqttClient client;
	MqttBrokerStepDefs steps = new MqttBrokerStepDefs();

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void after() {

		if (client != null) {
			try {
				client.close();
			} catch (Exception ignore) {
			}
		}

		testFile.delete();
		steps.after();
	}

	@Test
	public void testAfter_NotRunning() {

		if (client != null) {
			client.close();
		}
		steps.after();
	}

	@Test
	public void testAfter_Running() {

		steps.mqttBrokerIsRunningOnPort(12473, false);
		assertTrue(isRunning());
		steps.after();
		assertFalse(isRunning());
	}

	@Test
	public void testMqttBrokerAllowsUserAccessWithPassword() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, false);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		assertEquals(ConnectReturnCode.BAD_CREDENTIALS, client.connect("client", false, "foo", "bar"));

		steps.mqttBrokerAllowsUserAccessWithPassword("foo", "bar");
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		assertEquals(ConnectReturnCode.ACCEPTED, client.connect("client", false, "foo", "bar"));
	}

	@Test(expected = RelishException.class)
	public void testMqttBrokerAllowsUserAccessWithPassword_BrokerNotRunning() throws Exception {

		steps.mqttBrokerAllowsUserAccessWithPassword("foo", "bar");
	}

	@Test
	public void testMqttBrokerIsRunningOnPort_AnonymousAccessAllowed() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		assertEquals(ConnectReturnCode.ACCEPTED, client.connect("client", false));
	}

	@Test
	public void testMqttBrokerIsRunningOnPort_AnonymousAccessNotAllowed() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, false);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		assertEquals(ConnectReturnCode.NOT_AUTHORIZED, client.connect("client", false));
	}

	@Test
	public void testMqttBrokerPublishesAMessage_Retained() throws Exception {

		startBrokerAndClient();
		client.subscribe(new Subscription[] { new Subscription("foo", QoS.AT_LEAST_ONCE) });

		steps.mqttBrokerPublishesAMessage(true, 1, "foo", DataFormat.TEXT, "abc");

		verify(listener, timeout(1000)).publishReceived(same(client), messageCaptor.capture());
		PublishMessage message = messageCaptor.getValue();
		assertTrue(message.isRetain());
		assertEquals("abc", message.getPayloadString());
		assertEquals(QoS.AT_LEAST_ONCE, message.getQoS());
		assertEquals("foo", message.getTopic());
	}

	@Test
	public void testMqttBrokerPublishesAMessage_NotRetained() throws Exception {

		startBrokerAndClient();
		client.subscribe(new Subscription[] { new Subscription("foo", QoS.AT_LEAST_ONCE) });

		steps.mqttBrokerPublishesAMessage(false, 1, "foo", DataFormat.TEXT, "abc");

		verify(listener, timeout(1000)).publishReceived(same(client), messageCaptor.capture());
		PublishMessage message = messageCaptor.getValue();
		assertFalse(message.isRetain());
		assertEquals("abc", message.getPayloadString());
		assertEquals(QoS.AT_LEAST_ONCE, message.getQoS());
		assertEquals("foo", message.getTopic());
	}

	@Test(expected = RelishException.class)
	public void testMqttBrokerPublishesAMessage_BrokerNotRunning() throws Exception {

		steps.mqttBrokerPublishesAMessage(false, 1, "foo", DataFormat.TEXT, "abc");
	}

	@Test
	public void testMqttBrokerPublishesAMessageFromFile_Retained() throws Exception {

		RelishUtil.writeToFile(testFile, new ByteArrayInputStream("hello world".getBytes(Charset.forName("UTF8"))));
		startBrokerAndClient();
		client.subscribe(new Subscription[] { new Subscription("foo", QoS.AT_LEAST_ONCE) });

		steps.mqttBrokerPublishesAMessageFromFile(true, 1, "foo", testFile.getName());

		verify(listener, timeout(1000)).publishReceived(same(client), messageCaptor.capture());
		PublishMessage message = messageCaptor.getValue();
		assertTrue(message.isRetain());
		assertEquals("hello world", message.getPayloadString());
		assertEquals(QoS.AT_LEAST_ONCE, message.getQoS());
		assertEquals("foo", message.getTopic());
	}

	@Test
	public void testMqttBrokerPublishesAMessageFromFile_NotRetained() throws Exception {

		RelishUtil.writeToFile(testFile, new ByteArrayInputStream("hello world".getBytes(Charset.forName("UTF8"))));
		startBrokerAndClient();
		client.subscribe(new Subscription[] { new Subscription("foo", QoS.AT_LEAST_ONCE) });

		steps.mqttBrokerPublishesAMessageFromFile(false, 1, "foo", testFile.getName());

		verify(listener, timeout(1000)).publishReceived(same(client), messageCaptor.capture());
		PublishMessage message = messageCaptor.getValue();
		assertFalse(message.isRetain());
		assertEquals("hello world", message.getPayloadString());
		assertEquals(QoS.AT_LEAST_ONCE, message.getQoS());
		assertEquals("foo", message.getTopic());
	}

	@Test(expected = RelishException.class)
	public void testMqttBrokerPublishesAMessageFromFile_FileDoesNotExist() throws Exception {

		steps.mqttBrokerPublishesAMessageFromFile(false, 1, "foo", "not a file");
	}

	@Test(expected = RelishException.class)
	public void testMqttBrokerPublishesAMessageFromFile_BrokerNotRunning() throws Exception {

		steps.mqttBrokerPublishesAMessageFromFile(false, 1, "foo", testFile.getName());
	}

	@Test
	public void testMqttBrokerMessagePublishedShouldBeRetained_Retained_Matches() throws Exception {

		startBrokerAndClient();
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "", true));
		steps.mqttBrokerMessagePublishedShouldBeRetained(1, 1, "client", "foo", true);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerMessagePublishedShouldBeRetained_Retained_DoesNotMatch() throws Exception {

		startBrokerAndClient();
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "", true));
		steps.mqttBrokerMessagePublishedShouldBeRetained(1, 1, "client", "foo", false);
	}

	@Test
	public void testMqttBrokerMessagePublishedShouldBeRetained_NotRetained_Matches() throws Exception {

		startBrokerAndClient();
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "", false));
		steps.mqttBrokerMessagePublishedShouldBeRetained(1, 1, "client", "foo", false);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerMessagePublishedShouldBeRetained_NotRetained_DoesNotMatch() throws Exception {

		startBrokerAndClient();
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "", true));
		steps.mqttBrokerMessagePublishedShouldBeRetained(1, 1, "client", "foo", false);
	}

	@Test
	public void testMqttBrokerMessagePublishedShouldHaveAQosOf_Mathes() throws Exception {

		startBrokerAndClient();
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "", false));
		steps.mqttBrokerMessagePublishedShouldHaveAQosOf(1, 1, "client", "foo", 1);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerMessagePublishedShouldHaveAQosOf_DoesNotMath() throws Exception {

		startBrokerAndClient();
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "", false));
		steps.mqttBrokerMessagePublishedShouldHaveAQosOf(1, 1, "client", "foo", 0);
	}

	@Test
	public void testMqttBrokerMessagePublishedPayloadShouldMatch_Matches() throws Exception {

		startBrokerAndClient();
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		steps.mqttBrokerMessagePublishedShouldMatch(1, 1, "client", "foo", DataFormat.TEXT, "hello world");
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerMessagePublishedPayloadShouldMatch_DoesNotMatch() throws Exception {

		startBrokerAndClient();
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		steps.mqttBrokerMessagePublishedShouldMatch(1, 1, "client", "foo", DataFormat.TEXT, "crapola");
	}

	@Test
	public void testMqttBrokerShouldHaveMessagePublishedCount_ClientSpecified() throws Exception {

		startBrokerAndClient();
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		steps.mqttBrokerShouldHaveMessagePublishedCount(CountQuantifier.EXACTLY, 3, "client", "foo");
	}

	@Test
	public void testMqttBrokerShouldHaveMessagePublishedCount_ClientNotSpecified() throws Exception {

		startBrokerAndClient();
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		steps.mqttBrokerShouldHaveMessagePublishedCount(CountQuantifier.EXACTLY, 3, null, "foo");
	}

	@Test
	public void testMqttBrokerShouldHaveMessagePublishedCountWithin_ClientNotSpecified_Success() throws Exception {

		startBrokerAndClient();
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		steps.mqttBrokerShouldHaveMessagePublishedCountWithin(3, null, "foo", 1, TimeUnit.SECONDS);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerShouldHaveMessagePublishedCountWithin_ClientNotSpecified_TimesOut() throws Exception {

		startBrokerAndClient();
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		steps.mqttBrokerShouldHaveMessagePublishedCountWithin(3, null, "foo", 1, TimeUnit.SECONDS);
	}

	@Test
	public void testMqttBrokerShouldHaveMessagePublishedCountWithin_ClientSpecified_Success() throws Exception {

		startBrokerAndClient();
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		steps.mqttBrokerShouldHaveMessagePublishedCountWithin(3, "client", "foo", 1, TimeUnit.SECONDS);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerShouldHaveMessagePublishedCountWithin_ClientSpecified_TimesOut() throws Exception {

		startBrokerAndClient();
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		client.publish(new PublishMessage("foo", QoS.AT_LEAST_ONCE, "hello world", true));
		steps.mqttBrokerShouldHaveMessagePublishedCountWithin(3, "client", "foo", 1, TimeUnit.SECONDS);
	}

	@Test
	public void testMqttBrokerClientShouldBeConnected_NoTimeout_Success() throws Exception {

		startBrokerAndClient();
		steps.mqttBrokerClientShouldBeConnected("client", 0, null);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientShouldBeConnected_NoTimeout_Fail() throws Exception {

		steps.mqttBrokerClientShouldBeConnected("client", 0, null);
	}

	@Test
	public void testMqttBrokerClientShouldBeConnected_WithTimeout_Success() throws Exception {

		startBrokerAndClient();
		steps.mqttBrokerClientShouldBeConnected("client", 1, TimeUnit.SECONDS);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientShouldBeConnected_WithTimeout_TimesOut() throws Exception {

		elapsedTime.expectMinMillis(500);
		elapsedTime.expectMaxMillis(1500);
		steps.mqttBrokerClientShouldBeConnected("client", 1, TimeUnit.SECONDS);
	}

	@Test
	public void testMqttBrokerClientKeepAliveIntervalShouldBe_Success() throws Exception {

		MqttClientConfig config = new MqttClientConfig();
		config.setKeepAliveSeconds(100);
		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1, config);
		assertEquals(ConnectReturnCode.ACCEPTED, client.connect("client", false));

		steps.mqttBrokerClientKeepAliveIntervalShouldBe("client", 100);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientKeepAliveIntervalShouldBe_Fail() throws Exception {

		MqttClientConfig config = new MqttClientConfig();
		config.setKeepAliveSeconds(100);
		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1, config);
		assertEquals(ConnectReturnCode.ACCEPTED, client.connect("client", false));

		steps.mqttBrokerClientKeepAliveIntervalShouldBe("client", 1000);
	}

	@Test
	public void testMqttBrokerClientUserNameShouldBe_Success() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		steps.mqttBrokerAllowsUserAccessWithPassword("abc", "123");
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		assertEquals(ConnectReturnCode.ACCEPTED, client.connect("client", false, "abc", "123"));

		steps.mqttBrokerClientUserNameShouldBe("client", "abc");
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientUserNameShouldBe_Fail() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		steps.mqttBrokerAllowsUserAccessWithPassword("abc", "123");
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		assertEquals(ConnectReturnCode.ACCEPTED, client.connect("client", false, "abc", "123"));

		steps.mqttBrokerClientUserNameShouldBe("client", "def");
	}

	@Test
	public void testMqttBrokerClientPasswordShouldBe_Success() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		steps.mqttBrokerAllowsUserAccessWithPassword("abc", "123");
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		assertEquals(ConnectReturnCode.ACCEPTED, client.connect("client", false, "abc", "123"));

		steps.mqttBrokerClientPasswordShouldBe("client", "123");
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientPasswordShouldBe_Fail() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		steps.mqttBrokerAllowsUserAccessWithPassword("abc", "123");
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		assertEquals(ConnectReturnCode.ACCEPTED, client.connect("client", false, "abc", "123"));

		steps.mqttBrokerClientPasswordShouldBe("client", "456");
	}

	@Test
	public void testMqttBrokerClientCleanSession_Success() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		assertEquals(ConnectReturnCode.ACCEPTED, client.connect("client", true));

		steps.mqttBrokerClientCleanSession("client", true);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientCleanSession_Fail() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		assertEquals(ConnectReturnCode.ACCEPTED, client.connect("client", false));

		steps.mqttBrokerClientCleanSession("client", true);
	}

	@Test
	public void testMqttBrokerClientWillMessageShouldBe_Success() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		assertEquals(ConnectReturnCode.ACCEPTED, client.connect("client", false, "willTopic", "willMessage", QoS.AT_LEAST_ONCE, true));

		steps.mqttBrokerClientWillMessageShouldBe("client", true, 1, "willTopic");
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientWillMessageShouldBe_Fail() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		assertEquals(ConnectReturnCode.ACCEPTED, client.connect("client", false, "willTopic", "willMessage", QoS.AT_LEAST_ONCE, true));

		steps.mqttBrokerClientWillMessageShouldBe("client", false, 1, "willTopic");
	}

	@Test
	public void testMqttBrokerClientWillMessageWithBodyShouldBe_Success() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		assertEquals(ConnectReturnCode.ACCEPTED, client.connect("client", false, "willTopic", "willMessage", QoS.AT_LEAST_ONCE, true));

		steps.mqttBrokerClientWillMessageWithBodyShouldBe("client", true, 1, "willTopic", DataFormat.TEXT, "willMessage");
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientWillMessageWithBodyShouldBe_Fail() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		assertEquals(ConnectReturnCode.ACCEPTED, client.connect("client", false, "willTopic", "willMessage", QoS.AT_LEAST_ONCE, true));

		steps.mqttBrokerClientWillMessageWithBodyShouldBe("client", true, 1, "willTopic", DataFormat.TEXT, "notTheWillMessage");
	}

	@Test
	public void testMqttBrokerClientShouldBeSubscribedTo_NoTimeout_Success() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		client.connect("client", false);
		client.subscribe(new Subscription[] { new Subscription("abc", QoS.AT_LEAST_ONCE) });

		steps.mqttBrokerClientShouldBeSubscribedTo("client", "abc", 1, 0, null);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientShouldBeSubscribedTo_NoTimeout_NotSubscribed() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		client.connect("client", false);
		client.subscribe(new Subscription[] { new Subscription("abc", QoS.AT_LEAST_ONCE) });

		steps.mqttBrokerClientShouldBeSubscribedTo("client", "def", 1, 0, null);
		fail();
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientShouldBeSubscribedTo_NoTimeout_Subscribed_QosDoesNotMatch() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		client.connect("client", false);
		client.subscribe(new Subscription[] { new Subscription("abc", QoS.AT_LEAST_ONCE) });

		steps.mqttBrokerClientShouldBeSubscribedTo("client", "abc", 0, 0, null);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientShouldBeSubscribedTo_NoTimeout_NotSubscribed_QosDoesNotMatch() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		client.connect("client", false);
		client.subscribe(new Subscription[] { new Subscription("def", QoS.AT_LEAST_ONCE) });

		steps.mqttBrokerClientShouldBeSubscribedTo("client", "abc", 0, 0, null);
	}

	@Test
	public void testMqttBrokerClientShouldBeSubscribedTo_Timeout_Success() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		client.connect("client", false);
		client.subscribe(new Subscription[] { new Subscription("abc", QoS.AT_LEAST_ONCE) });

		steps.mqttBrokerClientShouldBeSubscribedTo("client", "abc", 1, 1, TimeUnit.SECONDS);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientShouldBeSubscribedTo_Timeout_TimesOut() throws Exception {

		elapsedTime.expectMinMillis(500);
		elapsedTime.expectMaxMillis(1500);

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		client.connect("client", false);

		steps.mqttBrokerClientShouldBeSubscribedTo("client", "abc", 1, 1, TimeUnit.SECONDS);
	}

	@Test
	public void testMqttBrokerClientShouldBeUnsubscribedFrom_NoTimeout_Success() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		client.connect("client", false);
		client.subscribe(new Subscription[] { new Subscription("abc", QoS.AT_LEAST_ONCE) });
		client.unsubscribe(new String[] { "abc" });

		steps.mqttBrokerClientShouldBeUnsubscribedFrom("client", "abc", 0, null);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientShouldBeUnsubscribedFrom_NoTimeout_NeverSubscribed() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		client.connect("client", false);

		steps.mqttBrokerClientShouldBeUnsubscribedFrom("client", "abc", 0, null);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientShouldBeUnsubscribedFrom_NoTimeout_SubscribedButNotUnsubscribed() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		client.connect("client", false);
		client.subscribe(new Subscription[] { new Subscription("abc", QoS.AT_LEAST_ONCE) });

		steps.mqttBrokerClientShouldBeUnsubscribedFrom("client", "abc", 0, null);
	}

	@Test
	public void testMqttBrokerClientShouldBeUnsubscribedFrom_Timeout_Success() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		client.connect("client", false);
		client.subscribe(new Subscription[] { new Subscription("abc", QoS.AT_LEAST_ONCE) });
		client.unsubscribe(new String[] { "abc" });

		steps.mqttBrokerClientShouldBeUnsubscribedFrom("client", "abc", 1, TimeUnit.SECONDS);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientShouldBeUnsubscribedFrom_Timeout_TimesOut() throws Exception {

		elapsedTime.expectMinMillis(500);
		elapsedTime.expectMaxMillis(1500);

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		client.connect("client", false);

		steps.mqttBrokerClientShouldBeUnsubscribedFrom("client", "abc", 1, TimeUnit.SECONDS);
	}

	@Test
	public void testMqttBrokerClientShouldBeDisconnected_NoTimeout_Success() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		client.connect("client", false);
		client.disconnect();

		Thread.sleep(500);

		steps.mqttBrokerClientShouldBeDisconnected("client", 0, null);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientShouldBeDisconnected_NoTimeout_ConnectedButNotDisconnected() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		client.connect("client", false);

		steps.mqttBrokerClientShouldBeDisconnected("client", 0, null);
	}

	@Test
	public void testMqttBrokerClientShouldBeDisconnected_Timeout_Success() throws Exception {

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		client.connect("client", false);
		client.disconnect();

		steps.mqttBrokerClientShouldBeDisconnected("client", 1, TimeUnit.SECONDS);
	}

	@Test(expected = AssertionError.class)
	public void testMqttBrokerClientShouldBeDisconnected_Timeout_TimesOut() throws Exception {

		elapsedTime.expectMinMillis(500);
		elapsedTime.expectMaxMillis(1500);

		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		client.connect("client", false);

		steps.mqttBrokerClientShouldBeDisconnected("client", 1, TimeUnit.SECONDS);
	}

	private void startBrokerAndClient() {
		steps.mqttBrokerIsRunningOnPort(12473, true);
		client = new SyncMqttClient("tcp://localhost:12473", listener, 1);
		assertEquals(ConnectReturnCode.ACCEPTED, client.connect("client", false));
	}

	private boolean isRunning() {
		try {
			MqttClientListener mockListener = mock(MqttClientListener.class);
			new SyncMqttClient("tcp://localhost:12473", mockListener, 1).close();
			return true;
		} catch (MqttInvocationException e) {
			if (e.getRootCause().getClass() != ConnectException.class) {
				throw e;
			}
			return false;
		}
	}
}
