Feature: MQTT Client 

Scenario: Connect 
	Given MQTT client "foo" will message is retained at QoS 1 on "/my/topic1" with this text: 
		"""
    other text
    """
	Given MQTT client "foo" uses a keep alive interval of 30 seconds 
	Given MQTT client "foo" is connected to "tcp://m2m.io:1883"
	Given MQTT client "foo" is connected with a clean session to "tcp://m2m.io:1883" 
	Given MQTT client "foo" is connected to "tcp://m2m.io:1883" as user "abc" 
	Given MQTT client "foo" is connected to "tcp://m2m.io:1883" as user "abc" with password "123" 
	Given MQTT client "foo" is connected with a clean session to "tcp://m2m.io:1883" as user "abc" with password "123" 
	
Scenario: Disconnect 
	When MQTT client "foo" is disconnected 
	
Scenario: Subscribe 
	Given MQTT client "foo" is subscribed to topic "/my/topic1" at QoS 1 
	
Scenario: Unsubscribe 
	When MQTT client "foo" is unsubscribed from topic "/my/topic1" 
	
Scenario: Publish message 
	When MQTT client "foo" publishes a message at QoS 0 to "/my/topic1" as text: 
		"""
    message body text
    """
	When MQTT client "foo" publishes a retained message at QoS 1 to "/my/topic2" as XML: 
		"""
    <myxml>abc</myxml>
    """
	When MQTT client "foo" publishes a message at QoS 0 to "/my/topic1" from file "test.dat"
		
Scenario: Verify published message
    
    Then MQTT client "foo" message 2 from "/my/topic1" should not be retained
    Then MQTT client "foo" message 2 from "/my/topic1" should be retained
    Then MQTT client "foo" messages 3 thru 5 from "/my/topic1" should have a QoS of 0
    Then MQTT client "foo" messages 4 thru 9 from "/my/topic1" should match this JSON:
    """
    {"field1":123}
    """
    Then MQTT client "foo" message 5 from "/my/topic1" should match this text:
    """
    this is (?:my|our) text
    """
    Then MQTT client "foo" should have at least 5 messages from "/my/topic1"
    Then MQTT client "foo" should have at least 5 messages from "/my/topic1" within 3 seconds
    Then MQTT client "foo" should have at most 5 messages from "/my/topic1"
    Then MQTT client "foo" should have exactly 5 messages from "/my/topic1"
