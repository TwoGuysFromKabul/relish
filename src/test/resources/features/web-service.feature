Feature: Web Service
	Tests the mock web service steps
	what to do when the response queue is empty: block or send the always stuff?
	
Scenario: Start/stop web service
    Given web service "pump" is running at "http://localhost:8080/pump"
    Given web service "pump" is stopped

Scenario: Configure web service responses
    Given web service "pump" response 1 header "abc" is "123"
    Given web service "pump" responses 1 thru 5 header "abc" is "123"
    Given web service "pump" response 2 uses these headers:
    | Name | Value |
    | header1 | value1 |
    | header2 | value2 |
    Given web service "pump" responses 2 thru 21 use these headers:
    | Name | Value |
    | header1 | value1 |
    | header2 | value2 |
    Given web service "pump" responses 3 thru 22 body is this text:
    """
    the response text
    """
    Given web service "pump" response 4 body is this binary:
    """
    01 fa c3
    """
    Given web service "pump" responses 5 thru 24 use status code 200
    Given web service "pump" response 7 uses status code 200
    
Scenario: Validate web service requests
	Steps that use "should be" compare equality directly.
	Steps that use "should match" compare using a regex.
	
    Then web service "pump" request 1 method should be "GET"
    Then web service "pump" request 1 thru 3 method should be "GET"
    Then web service "pump" request 1 header "abc" should match "123"
    Then web service "pump" requests 1 thru 5 header "def" should match "(?:456|789)"
    Then web service "pump" request 2 headers should include:
    | Name | Value |
    | header1 | value1 |
    | header2 | value2 |
    Then web service "pump" requests 3 thru 88 headers should be:
    | Name | Value |
    | header3 | value3 |
    | header4 | value4 |
    Then web service "pump" request 4 body should match this XML:
    """
    <myxml>foo</myxml>
    """
    Then web service "pump" requests 5 thru 9 body should match this text:
    """
    ^this is (?:my|our) regular expression$
    """
    Then web service "pump" should have at least 5 requests
    Then web service "pump" should have at least 5 requests within 2 seconds
    Then web service "pump" should have at least 1 request within 2 seconds
    Then web service "pump" should have at most 6 requests
    Then web service "pump" should have exactly 7 requests

