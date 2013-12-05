Feature: MQTT Broker

Scenario: Configure broker default anonymous access
    Given MQTT broker is running on port 1883
    Given MQTT broker allows user "bob" access with password "abc123"

Scenario: Configure broker with anonymous access
    Given MQTT broker is running on port 1883 with anonymous access allowed

Scenario: Configure broker without anonymous access
    Given MQTT broker is running on port 1883 with anonymous access not allowed

Scenario: Publish message

    When MQTT broker publishes a message at QoS 0 to "/my/topic1" as text:
    """
    message body text
    """
    When MQTT broker publishes a retained message at QoS 1 to "/my/topic2" as XML:
    """
    <myxml>abc</myxml>
    """
	When MQTT broker publishes a message at QoS 0 to "/my/topic1" from file "test.dat"
    
Scenario: Verify published message by client

    Then MQTT broker message 2 published by "foo" to "/my/topic1" should not be retained
    Then MQTT broker message 2 published by "foo" to "/my/topic1" should be retained
    Then MQTT broker message 2 published by "foo" to "/my/topic1" should have a QoS of 0
    
    Then MQTT broker messages 3 thru 5 published by "foo" to "/my/topic1" should not be retained
    Then MQTT broker messages 3 thru 5 published by "foo" to "/my/topic1" should be retained
    Then MQTT broker messages 3 thru 5 published by "foo" to "/my/topic1" should have a QoS of 1
    Then MQTT broker messages 4 thru 9 published by "foo" to "/my/topic1" should match this JSON:
    """
    {"field1":123}
    """
    Then MQTT broker message 5 published by "foo" to "/my/topic1" should match this text:
    """
    this is (?:my|our) text
    """
    Then MQTT broker should have at least 5 messages published by "foo" to "/my/topic1"
    Then MQTT broker should have at least 5 messages published by "foo" to "/my/topic1" within 2 milliseconds
    Then MQTT broker should have at least 5 messages published by "foo" to "/my/topic1" within 2 seconds
    Then MQTT broker should have at most 5 messages published by "foo" to "/my/topic1"
    Then MQTT broker should have exactly 5 messages published by "foo" to "/my/topic1"
    
Scenario: Verify published message across all clients

    Then MQTT broker message 2 published to "/my/topic1" should not be retained
    Then MQTT broker message 2 published to "/my/topic1" should be retained
    Then MQTT broker messages 3 thru 9 published to "/my/topic1" should not be retained
    Then MQTT broker messages 3 thru 9 published to "/my/topic1" should be retained
    
    Then MQTT broker message 2 published to "/my/topic1" should have a QoS of 1
    Then MQTT broker messages 3 thru 9 published to "/my/topic1" should have a QoS of 0
    
    Then MQTT broker messages 4 thru 6 published to "/my/topic1" should match this JSON:
    """
    {"field1":123}
    """
    Then MQTT broker message 5 published to "/my/topic1" should match this text:
    """
    this is (?:my|our) text
    """
    
    Then MQTT broker should have at most 5 messages published to "/my/topic1"
    Then MQTT broker should have at least 5 messages published to "/my/topic1"
    Then MQTT broker should have at least 5 messages published to "/my/topic1" within 2 milliseconds
    Then MQTT broker should have exactly 5 messages published to "/my/topic1"
    
Scenario: Verify connect
    Then MQTT broker client "foo" should be connected
    Then MQTT broker client "foo" should be connected within 1 seconds
    Then MQTT broker client "foo" keep alive interval should be 30 seconds
    Then MQTT broker client "foo" username should be "bob"
    Then MQTT broker client "foo" password should be "abc123"
    Then MQTT broker client "foo" should have a clean session
    Then MQTT broker client "foo" should not have a clean session
    Then MQTT broker client "foo" will message should be retained at QoS 1 on "/my/topic1" matching this text:
    """
    other text
    """
    Then MQTT broker client "foo" will message should match this text:
    """
    some text
    """
    Then MQTT broker client "foo" will message should be retained at QoS 0 on "/my/topic1"
    Then MQTT broker client "foo" will message should be at QoS 0
    Then MQTT broker client "foo" will message should be on "/my/topic1" matching this JSON:
    """
    {"field1":123}
    """
    Then MQTT broker client "foo" will message should be not retained
    Then MQTT broker client "foo" will message should be at QoS 1
    Then MQTT broker client "foo" will message should match this text:
    """
    this is (?:my|our) text
    """

Scenario: Verify subscribe
    Then MQTT broker client "foo" should be subscribed to "/my/topic2"
    Then MQTT broker client "foo" should be subscribed to "/my/topic1" at QoS 1
    Then MQTT broker client "foo" should be subscribed to "/my/topic1" within 500 milliseconds
    Then MQTT broker client "foo" should be subscribed to "/my/topic1" at QoS 1 within 500 milliseconds
    
Scenario: Verify unsubscribe
    Then MQTT broker client "foo" should be unsubscribed from "/my/topic1"
    Then MQTT broker client "foo" should be unsubscribed from "/my/topic1" within 500 milliseconds
    
Scenario: Verify disconnect
    Then MQTT broker client "foo" should be disconnected
    Then MQTT broker client "foo" should be disconnected within 5 seconds
