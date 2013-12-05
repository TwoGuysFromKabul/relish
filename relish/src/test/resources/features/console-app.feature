Feature: Console App

Scenario: Start/stop console app in project no args
	Given console app "net.sf.relish.TestMainClass" is running
	When console app "net.sf.relish.TestMainClass" is stopped

Scenario: Start/stop console app in project with args
	Given console app "net.sf.relish.TestMainClass" is running with args: 1 "a b c" foo
	When console app "net.sf.relish.TestMainClass" is stopped

Scenario: Start/stop console app in jar no args
	Given console app "net.sf.relish.TestMainClass" is running from JAR "testapp.jar"
	When console app "net.sf.relish.TestMainClass" is stopped

Scenario: Start/stop console app in jar with args
	Given console app "net.sf.relish.TestMainClass" is running from JAR "testapp.jar" with args: 1 "a b c" foo
	When console app "net.sf.relish.TestMainClass" is stopped
