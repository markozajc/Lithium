package com.github.markozajc.lithium.data.providers;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.Lithium;
import com.github.markozajc.lithium.data.properties.PropertyManager;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import net.dv8tion.jda.core.JDA;

/**
 * The {@link Provider} framework provides a way to store persistent data. Data is
 * automatically deserialized and loaded to all registered providers using
 * {@link Gson} on every boot and serialized and stored on every shutdown (make sure
 * you shut down JVM gracefully to let the shutdown hooks run!)
 *
 * @author Marko Zajc
 * @param <T>
 *            The type of the data used in this provider.
 */
public abstract class Provider<T> {

	private static final Logger LOG = LoggerFactory.getLogger(Provider.class);

	protected T data;

	/**
	 * @return this provider's data
	 */
	public T getData() {
		return this.data;
	}

	/**
	 * @return the data key that will be used when loading & storing values
	 */
	public abstract String getDataKey();

	public final void setDefaultData() {
		this.data = getDefaultData();
	}

	/**
	 * Loads the {@link Provider}'s data from JSON. The {@code json} argument can also be
	 * {@code null}, in which case {@link #setDefaultData()} will be called.
	 *
	 * @param json
	 *            the JSON to load for this {@link Provider} or {@code null} to load the
	 *            default data.
	 * @throws JsonParseException
	 *             if the given JSON is invalid
	 */
	private final void loadFromJson(@Nullable String json) {
		if (json == null) {
			setDefaultData();

			// In case the data itself is null (there is no data attached to that property
			// in the given PropertyManager)
		} else {
			T newData = constructData(json);

			if (newData == null) {
				setDefaultData();

				// In case the constructData(String) returns null
			} else {
				this.data = newData;
			}
		}

		onDataLoaded();
	}

	/**
	 * Loads the provider's data from a {@link Map} of keys and values. Loading
	 * exceptions ({@link JsonParseException}) are handled automatically and are passed
	 * to this {@link Provider}'s load failure handler. The default data will be used if
	 * the given {@link Map} doesn't have this {@link Provider}'s data.
	 *
	 * @param properties
	 *            the key-value {@link Map} to load from
	 */
	public final void load(Map<String, String> properties) {
		try {
			loadFromJson(properties.get(this.getDataKey()));
		} catch (JsonParseException t) {
			setDefaultData();
			onLoadFail(t);
		}
	}

	/**
	 * Loads the provider's data from a {@link PropertyManager}. Loading exceptions are
	 * handled automatically and are passed to this {@link Provider}'s load failure
	 * handler. The default data will be used if the {@link PropertyManager} doesn't have
	 * this {@link Provider}'s data.
	 *
	 * @param pm
	 *            the {@link PropertyManager} to load from
	 */
	public final void load(PropertyManager pm) {
		try {
			long start = System.currentTimeMillis();

			loadFromJson(pm.getProperty(this.getDataKey()).get());

			LOG.debug("Loaded {} in {} milliseconds.", this.getClass().getSimpleName(),
				System.currentTimeMillis() - start);
		} catch (JsonParseException | ExecutionException t) {
			setDefaultData();
			onLoadFail(t);

		} catch (InterruptedException e) {
			LOG.debug("Got interrupted while loading data.");
			Thread.currentThread().interrupt();
		}
	}

	public final Future<Void> store(PropertyManager pm) {
		return pm.setProperty(getDataKey(), constructJson());
	}

	/**
	 * Constructs the {@link Provider}'s data from JSON.
	 *
	 * @param json
	 *            JSON to parse
	 * @return data constructed out of the given JSON
	 * @throws JsonParseException
	 *             if there's something wrong with the provided JSON
	 */
	protected T constructData(String json) {
		return getGson().fromJson(json, new TypeToken<T>() {}.getType());
	}

	/**
	 * @return JSON constructed out of current data
	 */
	protected String constructJson() {
		return getGson().toJson(this.data);
	}

	/**
	 * @return returns the default (empty) data in case an exception is thrown when
	 *         trying to load the actual data
	 */
	protected T getDefaultData() {
		try {
			return constructData("{}");
		} catch (JsonParseException e) {
			return null;
		}
	}

	/**
	 * Will be called in case storing the data to the given PropertyManager fails in
	 * {@link #store(PropertyManager)}
	 *
	 * @param t
	 */
	public void onStoreFail(Throwable t) {
		LOG.error("Failed to store {}", this.getClass().getSimpleName());
		LOG.error("", t);
	}

	/**
	 * Will be called in case loading of the data from the PropertyManager fails in
	 * {@link #load(PropertyManager)}.
	 *
	 * @param t
	 */
	public void onLoadFail(Throwable t) {
		LOG.error("Failed to load {}", this.getClass().getSimpleName());
		LOG.error("", t);
	}

	/**
	 * Will be called at the end of {@link #loadFromJson(String)} and
	 * {@link #load(PropertyManager)}.
	 */
	protected void onDataLoaded() {}

	/**
	 * Cleans unused data of the provider. If the provider does not explicitly implement
	 * it, this won't do anything at all.
	 *
	 * @param lithium
	 *            The {@link Lithium} instance.
	 * @param jda
	 *            The {@link JDA} instance.
	 * @return A number of unused elements that were removed, by convention returns
	 *         {@code 0} if provider does not store data in separate elements, if the
	 *         provider does not support cleaning or if no obsolete elements were found.
	 */
	@SuppressWarnings("unused")
	public int clean(Lithium lithium, JDA jda) {
		return 0;
	}

	public Gson getGson() {
		return Constants.GSON;
	}

	public void onShutdown() {}
}