package com.github.markozajc.lithium.data.source;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import com.github.markozajc.lithium.data.properties.PropertyManager;

public interface DataSource {

	/**
	 * Creates a new {@link PropertyManager} attached to this {@link DataSource}.
	 *
	 * @param executor
	 *            The {@link ExecutorService} the {@link PropertyManager} should use.
	 *
	 * @return a new {@link PropertyManager}
	 * @throws IOException
	 *             thrown when something goes wrong
	 */
	public PropertyManager createPropertyManager(ExecutorService executor) throws IOException;

}
