package net.sf.relish.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import net.sf.relish.RelishException;
import cucumber.api.java.After;
import cucumber.api.java.en.Given;

/**
 * FIXME [jim] - needs javadoc
 */
public final class JdbcStepDefs {

	private final Map<String, Connection> connectionsByName = new HashMap<String, Connection>();

	@After
	public void after() throws SQLException {

		for (Connection conn : connectionsByName.values()) {
			conn.close();
		}
	}

	@Given("^JDBC connection \"(\\S.*)\" connects to \"(\\S+)\" using driver \"(\\S+)\"$")
	public void jdbcConnectionConnectsUsingDriver(String connectionName, String jdbcUrl, String driverClass) throws Exception {

		if (connectionsByName.containsKey(connectionName)) {
			throw new RelishException("You may not configure connection %s because it has already been configured", connectionName);
		}

		Class.forName(driverClass);
		Connection connection = DriverManager.getConnection(jdbcUrl);
		connectionsByName.put(connectionName, connection);
	}

	private Connection getConnection(String connectionName) {

		Connection conn = connectionsByName.get(connectionName);
		if (conn == null) {
			throw new RelishException("JDBC connection %s does not exist", connectionName);
		}

		return conn;
	}

	private static final class ConnectionInfo {

		Connection connection;
	}
}
