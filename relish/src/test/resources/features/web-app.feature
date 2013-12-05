Feature: Web App

Scenario: Start/stop web app in project
	Given web app "test-web-app" is running on port 13847
	When web app "test-web-app" is stopped

Scenario: Start/stop web app in war file
	Given web app "test-web-app.war" is running on port 13847
	When web app "test-web-app.war" is stopped