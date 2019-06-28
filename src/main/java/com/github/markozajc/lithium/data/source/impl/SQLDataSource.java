package com.github.markozajc.lithium.data.source.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.markozajc.lithium.data.properties.PropertyManager;
import com.github.markozajc.lithium.data.properties.impl.SQLPropertyManager;
import com.github.markozajc.lithium.data.source.DataSource;

/**
 * A {@link DataSource} implementation used to connect to a SQL database.
 *
 * @author Marko Zajc
 */
public class SQLDataSource implements DataSource {

	private static final Logger LOG = LoggerFactory.getLogger(SQLDataSource.class);

	public static final int MAX_RETRY = 5;

	private final String databaseUrl;

	/**
	 * Creates a new {@link SQLDataSource}.
	 *
	 * @param databaseUrl
	 *            the URL used to connect to the database
	 */
	public SQLDataSource(String databaseUrl) {
		this.databaseUrl = databaseUrl;
	}

	@Override
	public PropertyManager createPropertyManager(ExecutorService executor) throws IOException {
		SQLException e = null;
		for (int i = 0; i < MAX_RETRY; i++) {
			try {
				return new SQLPropertyManager(this.databaseUrl, executor);
			} catch (SQLException e1) {
				LOG.info("Couldn't connect to the SQL database, retrying [{}/{}].", i + 1, MAX_RETRY);
				e = e1;
			}
		}

		throw new IOException("Failed to connect to the SQL database " + MAX_RETRY + " times.", e);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SQLDataSource))
			return false;

		return this.hashCode() == obj.hashCode();
	}

	@Override
	public int hashCode() {
		return this.databaseUrl.hashCode();
	}

}
