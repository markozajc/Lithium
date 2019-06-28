package com.github.markozajc.lithium.data.properties;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * A class used to represent a property manager that can read/write properties.
 *
 * @author Marko Zajc
 */
public abstract class PropertyManager {

	protected final ExecutorService executor;

	public PropertyManager(ExecutorService executor) {
		this.executor = executor;
	}

	/**
	 * Sets a property.
	 *
	 * @param key
	 *            key of the property
	 * @param value
	 *            value of the property
	 * @return {@link Future} set operation
	 */
	public abstract Future<Void> setProperty(String key, String value);

	/**
	 * Removes a property.
	 *
	 * @param key
	 *            key of the property
	 * @return {@link Future} removal operation
	 */
	public abstract Future<Void> removeProperty(String key);

	/**
	 * Retrieves a property.
	 *
	 * @param key
	 *            key of the property to retrieve
	 * @return {@link Future} property's value or {@code null} if that property does not
	 *         exist
	 */
	public abstract Future<String> getProperty(String key);

	/**
	 * Gets a batch of properties. This behaves the same as {@link #getProperty(String)},
	 * except it returns a batch of results, which may be faster to fetch than one by
	 * one, depending on the implementation.
	 *
	 * @param keys
	 *            A {@link List} of keys to fetch.
	 * @return A {@link Map} of keys and values.
	 */
	public Future<Map<String, String>> getPropertyBatch(Collection<String> keys) {
		return this.executor.submit(() -> {

			Map<String, String> result = new HashMap<>();
			for (String key : keys)
				result.put(key, getProperty(key).get());

			return result;
		});
	}

	/**
	 * Retrieves a property with a default (null) value.
	 *
	 * @param key
	 *            key of the property to retrieve
	 * @param defaultVal
	 *            value that will be used if key does not exist
	 * @return property's value or provided default if that property does not exist
	 */
	public final Future<String> getProperty(String key, String defaultVal) {
		return this.executor.submit(() -> {
			String property = getProperty(key).get();
			if (property == null) {
				return defaultVal;
			}
			return property;
		});
	}

}
