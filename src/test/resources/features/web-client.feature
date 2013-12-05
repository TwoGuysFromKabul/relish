Feature: Web Client

Scenario: Configure web client requests
    Given web client request 1 header "abc" is "123"
    Given web client requests 1 thru 5 header "abc" is "123"
    Given web client request 2 uses these headers:
    | Name | Value |
    | header1 | value1 |
    | header2 | value2 |
    Given web client requests 2 thru 21 use these headers:
    | Name | Value |
    | header1 | value1 |
    | header2 | value2 |
    Given web client requests 3 thru 22 body is this text:
    """
    the request text
    """
    Given web client request 4 body is this binary:
    """
    01 fa c3
    """

Scenario: Send web client responses
	Then web client request 1 is sent to "http://localhost:8080/pump" using method "GET"
	Then web client requests 1 thru 10 are sent to "http://localhost:8080/pump" using method "PUT"
	
Scenario: Validate web client responses
	Steps that use "should be" compare equality directly.
	Steps that use "should match" compare using a regex.
	
    Then web client response 1 header "abc" should match "123"
    Then web client responses 1 thru 5 header "def" should match "(?:456|789)"
    Then web client response 2 headers should include:
    | Name | Value |
    | header1 | value1 |
    | header2 | value2 |
    Then web client responses 3 thru 88 headers should be:
    | Name | Value |
    | header3 | value3 |
    | header4 | value4 |
    Then web client response 4 body should match this XML:
    """
    <myxml>foo</myxml>
    """
    Then web client responses 5 thru 9 body should match this text:
    """
    ^this is (?:my|our) regular expression$
    """
    Then web client response 6 status code should be 200
    Then web client responses 5 thru 24 status code should be 200
    Then web client response 7 status code should be 200
    Then web client should have at least 5 responses
    Then web client should have at most 6 responses
    Then web client should have exactly 7 responses
    