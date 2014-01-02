Feature: JDBC 

Scenario: JDBC 

	Given JDBC connection "foo" connects to "jdbc:jtds:sqlserver://sql-comms:1433;DatabaseName=InTouch_Comms;socketTimeout=300;user=abc;password=123" using driver "net.sourceforge.jtds.jdbc.Driver" 
	
	Given JDBC connection "foo" inserts these records into table "my_table": 
		| Col1 | Col2 | Col3 |
		| abc  | def  | ghi  |
		| jkl  | mno  | pqr  |
		
	When JDBC connection "foo" executes query "delete * from foo" 
		
	Then JDBC connection "foo" query "select count(*) from foo" result count should be 5 
	Then JDBC connection "foo" query "select count(*) from foo" result count should be 5 within 2 seconds 
	Then JDBC connection "foo" query "select * from foo" results should be these records: 
		| Col1 | Col2 | Col3 |
		| abc  | def  | \S+[\d]+  |
		| jkl  | mno  | pqr  | 
	Then JDBC connection "foo" query "select * from foo" results should be these records within 5 seconds: 
		| Col1 | Col2 | Col3 |
		| abc  | def  | \S+[\d]+  |
		| jkl  | mno  | pqr  | 
	Then JDBC connection "foo" query "select * from foo" results should include these records: 
		| Col1 | Col2 | Col3 |
		| abc  | def  | \S+[\d]+  |
		| jkl  | mno  | pqr  | 
	Then JDBC connection "foo" query "select * from foo" results should include these records within 5 seconds: 
		| Col1 | Col2 | Col3 |
		| abc  | def  | \S+[\d]+  |
		| jkl  | mno  | pqr  | 
		
