package com.github.markozajc.lithium.data.properties.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.github.markozajc.lithium.data.properties.PropertyManager;

/**
 * A PropertyManager implementation that stores properties into a file when writing
 * and loads them when reading (also loads them when writing to them as it is
 * required to load them in order to create modifications). This implementation only
 * stores Properties into RAM when accessing the properties.
 *
 * @author Marko Zajc
 */
public class FilePropertyManager extends PropertyManager {

	private static final String EXTENSION = "lni";
	protected final File propertiesDirectory;

	public FilePropertyManager(File propertiesDirectory, ExecutorService executor) throws IOException {
		super(executor);
		this.propertiesDirectory = propertiesDirectory;

		if (!propertiesDirectory.exists() && !propertiesDirectory.mkdirs())
			throw new IOException("Couldn't create the properties directory.");

		if (!propertiesDirectory.isDirectory())
			throw new IOException("The given path is not a directory.");

		if (!propertiesDirectory.canWrite() || !propertiesDirectory.canRead())
			throw new IOException("The properties directory is not readable/writable.");

	}

	@Override
	public Future<Void> setProperty(String key, String value) {
		return this.executor.submit(() -> {
			Files.write(new File(this.propertiesDirectory, key + "." + EXTENSION).toPath(),
				value.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
			return null;
		});
	}

	@Override
	public Future<Void> removeProperty(String key) {
		return this.executor.submit(() -> {
			File file = new File(this.propertiesDirectory, key + "." + EXTENSION);
			if (file.exists())
				Files.delete(file.toPath());

			return null;
		});
	}

	@Override
	public Future<String> getProperty(String key) {
		return this.executor.submit(() -> {
			File file = new File(this.propertiesDirectory, key + "." + EXTENSION);
			if (!file.exists())
				return null;

			return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		});
	}

}
