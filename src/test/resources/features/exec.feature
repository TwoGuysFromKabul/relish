Feature: Application Executor 
	Executes external applications and provides access to their output
	
Scenario: Execute an application 
	When application "foo" is executed with command line "foo a b\\ c d" 
	When application "foo" is executed with command line "foo a b c" with input that is this text: 
		"""
	input line 1
	input line 2
	"""
	When application "bar" is executed with command line "bar" with input that is the output of the "foo" application 
	
Scenario: Validate application output 
	Then application "foo" exit code should be 0
	Then application "foo" output should match this text: 
		"""
a regex to match to the foo app's output
"""