Feature: Relish Utilities

Scenario: Print table 
	When print table: 
		| name  | value  |
		| name1 | value1 |
		| name2 | value2 |
		
Scenario: Print doc text 
	When print: 
		"""
	A bunch of text
	to print
	"""
		
Scenario: Print string
	When print "abc 123"
	
Scenario: Sleep
	When sleep for 10 milliseconds