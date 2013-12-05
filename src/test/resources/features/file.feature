Feature: File Feature 
	DSL for interacting with files and directories

Scenario: Directory exists ops 
	Then directory "/pfm/abc" exists within 5 milliseconds 
	Then directory "/pfm/abc" exists
	Then directory "/pfm/abc" does not exist within 55 seconds 
	Then directory "/pfm/abc" does not exist
	
Scenario: Directory content ops 
	Then directory "/pfm/abc" contains at least 2 files within 2 seconds 
	Then directory "/pfm/abc" contains at least 2 files
	Then directory "/pfm/abc" contains exactly 3 files 
	Then directory "/pfm/abc" contains at most 4 files
	
	Then directory "/pfm/abc" contains at least 2 files with names that match "\S+[\d]+" within 2 seconds 
	Then directory "/pfm/abc" contains at least 2 files with names that match "\S+[\d]+" 
	Then directory "/pfm/abc" contains exactly 3 files with names that match "\S+[\d]+" 
	Then directory "/pfm/abc" contains at most 4 files with names that match "\S+[\d]+" 
	Then directory "/pfm/abc" contains exactly 1 file with a name that matches "\S+[\d]+"
	
Scenario: File exists ops 
	Then file "/pfm/abc/foo.txt" exists within 5 seconds
	Then file "/pfm/abc/foo.txt" exists 
	Then file "/pfm/abc/foo.txt" does not exist within 65 seconds 
	Then file "/pfm/abc/foo.txt" does not exist 
	
Scenario: File content ops 
	Then file "/pfm/abc/foo.txt" contains at least 1 line that matches "\S+[\d]+" within 2 seconds 
	Then file "/pfm/abc/foo.txt" contains at least 2 lines that match "\S+[\d]+" within 2 seconds 
	Then file "/pfm/abc/foo.txt" contains at least 2 lines within 2 seconds 
	Then file "/pfm/abc/foo.txt" contains at least 2 lines that match "\S+[\d]+" 
	Then file "/pfm/abc/foo.txt" contains at least 2 lines
	Then file "/pfm/abc/foo.txt" contains exactly 3 lines that match "\S+[\d]+" 
	Then file "/pfm/abc/foo.txt" contains exactly 3 lines 
	Then file "/pfm/abc/foo.txt" contains at most 4 lines that match "\S+[\d]+" 
	Then file "/pfm/abc/foo.txt" contains at most 4 lines 
	Then file "/pfm/abc/foo.txt" contains at least 1 line within 2 seconds 
	Then file "/pfm/abc/foo.txt" contains exactly 1 line 
	Then file "/pfm/abc/foo.txt" contains at least 2 lines that match "\S+[\d]+" 
	
	Then file "/pfm/abc/foo.dat" contains binary that matches: 
		"""
	0c ae 32 [0-9a-fA-F]{1,2}
	"""
		
	Then file "/pfm/abc/foo.json" contains JSON that matches: 
		"""
	{"field1":"\S+"}
	"""
		
	Then file "/pfm/abc/foo.xml" contains XML that matches: 
		"""
	<foo><bar>\S+</bar></foo>
	"""
		
	Then file "/pfm/abc/foo.xml" contains text that matches: 
		"""
	hello\s+world
	"""
		
Scenario: File modifiation ops 
	Given file "/pfm/abc/foo.txt" is created with this text: 
		"""
	hello world
	"""
		
	Given file "/pfm/abc/foo.dat" is created with this binary: 
		"""
	0c ae 32
	"""
		
	When file "/pfm/abc/foo.txt" is deleted 
	