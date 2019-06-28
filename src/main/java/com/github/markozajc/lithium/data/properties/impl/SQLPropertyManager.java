package com.github.markozajc.lithium.data.properties.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.markozajc.lithium.data.properties.PropertyManager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
public class SQLPropertyManager extends PropertyManager {

	private static final Logger LOG = LoggerFactory.getLogger(SQLPropertyManager.class);

	private static final String TABLE_NAME = "data";
	private static final String KEY_COLUMN = "key";
	private static final String VALUE_COLUMN = "json";

	private final String dbUrl;
	private final String username;
	private final String password;

	private Connection activeConnection;

	private static final String SELECT_TABLE_STATEMENT = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE table_name='"
			+ TABLE_NAME + "';";
	private static final String CREATE_TABLE_STATEMENT = "CREATE TABLE " + TABLE_NAME + " (" + KEY_COLUMN + " varchar, "
			+ VALUE_COLUMN + " varchar);";

	private static final String DELETE_VALUE_STATEMENT = "DELETE FROM " + TABLE_NAME + " WHERE " + KEY_COLUMN
			+ " = %s;"; // NOSONAR
	private static final String SELECT_VALUE_STATEMENT = "SELECT " + VALUE_COLUMN + " FROM " + TABLE_NAME + " WHERE "
			+ KEY_COLUMN + " = %s;";
	private static final String SELECT_ALL_VALUES_STATEMENT = "SELECT * FROM " + TABLE_NAME + ";";
	private static final String INSERT_VALUE_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" + VALUE_COLUMN + ", "
			+ KEY_COLUMN + ") VALUES (%s, %s);";
	private static final String UPDATE_VALUE_STATEMENT = "UPDATE " + TABLE_NAME + " SET " + VALUE_COLUMN
			+ " = %s WHERE " + KEY_COLUMN + " = %s;";

	private Connection createConnection() throws SQLException {
		if (this.username == null && this.password == null)
			return DriverManager.getConnection(this.dbUrl);

		LOG.debug("Created a new SQL connection");
		return DriverManager.getConnection(this.dbUrl, this.username, this.password);
	}

	private Connection getConnection() throws SQLException {
		if (this.activeConnection == null) {
			this.activeConnection = createConnection();

		} else if (!this.activeConnection.isValid(0)) {
			LOG.debug("Invalidating an obsolete SQL connection");
			this.activeConnection.close();
			this.activeConnection = createConnection();
		}

		return this.activeConnection;
	}

	private static String neutralize(String str) {
		return neutralize(str, true);
	}

	private static String neutralize(String str, boolean startAndEnd) {
		return (startAndEnd ? "'" : "") + str.replace("'", "''") + (startAndEnd ? "'" : "");
	}

	private void init() throws SQLException {
		LOG.debug("Initializing SQL property manager");

		try (Statement select = getConnection().createStatement()) {
			// Opens a connections
			try (ResultSet results = select.executeQuery(SELECT_TABLE_STATEMENT)) {
				// Requests the table
				if (!results.next()) {
					// Checks if a table exists
					try (Statement createTable = getConnection().createStatement()) {
						// Creates a table
						createTable.execute(CREATE_TABLE_STATEMENT);
					}
				}
			}
		}
	}

	public SQLPropertyManager(String dbUrl, ExecutorService executor) throws SQLException {
		this(dbUrl, null, null, executor);
	}

	public SQLPropertyManager(String dbUrl, String username, String password, ExecutorService executor)
			throws SQLException {
		super(executor);
		this.dbUrl = dbUrl;
		this.username = username;
		this.password = password;

		init();
	}

	@Override
	public Future<Void> setProperty(String key, String value) {
		return this.executor.submit(() -> {
			try (Statement select = getConnection().createStatement()) {
				try (Statement update = getConnection().createStatement()) {
					update.executeUpdate(
						String.format(select.executeQuery(String.format(SELECT_VALUE_STATEMENT, neutralize(key))).next()
								? UPDATE_VALUE_STATEMENT
								: INSERT_VALUE_STATEMENT,
							neutralize(value), neutralize(key)));
				}

			} catch (SQLException e) {
				throw new IOException(e);
			}

			return null;
		});
	}

	@Override
	public Future<Void> removeProperty(String key) {
		return this.executor.submit(() -> {
			try (Statement s = getConnection().createStatement()) {
				s.executeUpdate(String.format(DELETE_VALUE_STATEMENT, neutralize(key)));

			} catch (SQLException e) {
				throw new IOException(e);
			}

			return null;
		});
	}

	@Override
	public Future<String> getProperty(String key) {
		return this.executor.submit(() -> {
			try (Statement s = getConnection().createStatement()) {
				try (ResultSet rs = s.executeQuery(String.format(SELECT_VALUE_STATEMENT, neutralize(key)))) {
					if (rs.next())
						return rs.getString(VALUE_COLUMN);

					return null;
				}
			} catch (SQLException e) {
				throw new IOException(e);
			}
		});
	}

	@Override
	public Future<Map<String, String>> getPropertyBatch(Collection<String> keys) {
		return this.executor.submit(() -> {

			Map<String, String> result = new HashMap<>();
			try (Statement s = getConnection().createStatement();
					ResultSet rs = s.executeQuery(SELECT_ALL_VALUES_STATEMENT);) {
				while (rs.next()) {
					if (keys.contains(rs.getString(KEY_COLUMN)))
						result.put(rs.getString(KEY_COLUMN), rs.getString(VALUE_COLUMN));
				}
			} catch (SQLException e) {
				throw new IOException(e);
			}

			return result;
		});
	}

}
