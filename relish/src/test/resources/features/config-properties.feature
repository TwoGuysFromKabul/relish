Feature: Config Properties

Scenario: Set Property
	Given config properties are:
	| Name  | Value  |
	| name1 | value1 |
	| name2 | value2 |
	Given config property "foo" is "bar"