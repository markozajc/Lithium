package com.github.markozajc.lithium.data.properties.impl;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.github.markozajc.lithium.data.properties.PropertyManager;

/**
 * The simplest {@link PropertyManager} implementation you can imagine. This
 * {@link PropertyManager} will keep the {@link Properties} object in RAM and access
 * it when needed. It will get wiped when JVM is shut down.
 *
 * @author Marko Zajc
 */
public class SimplePropertyManager extends PropertyManager {

	private final Properties properties;

	public SimplePropertyManager(ExecutorService executor) {
		super(executor);
		this.properties = new Properties();
	}

	@Override
	public Future<Void> setProperty(String key, String value) {
		return this.executor.submit(() -> {
			this.properties.setProperty(key, value);
			return null;
		});
	}

	@Override
	public Future<Void> removeProperty(String key) {
		return this.executor.submit(() -> {
			this.properties.remove(key);
			return null;
		});
	}

	@Override
	public Future<String> getProperty(String key) {
		return this.executor.submit(() -> this.properties.getProperty(key));
	}

}
