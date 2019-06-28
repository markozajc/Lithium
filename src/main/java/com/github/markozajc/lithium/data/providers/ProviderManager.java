package com.github.markozajc.lithium.data.providers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.markozajc.lithium.Lithium;
import com.github.markozajc.lithium.data.properties.PropertyManager;

import net.dv8tion.jda.core.JDA;

public class ProviderManager {

	private static final Logger LOG = LoggerFactory.getLogger(ProviderManager.class);

	@SuppressWarnings("rawtypes")
	private final Map<Class<? extends Provider>, Provider<?>> providers = new ConcurrentHashMap<>();

	public ProviderManager(List<Provider<?>> providers, Lithium lithium) {
		for (Provider<?> provider : providers)
			this.providers.put(provider.getClass(), provider);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {

			LOG.debug("Running ProviderManager's shutdown hook.");
			LOG.debug("Signalling shutdown to all registered providers.");

			this.providers.values().forEach(p -> {
				try {
					p.onShutdown();
				} catch (Exception e) {
					LOG.error("Caught an exception while shutting down a provider.", e);
				}
			});

			LOG.debug("Storing all providers.");
			this.storeAll(lithium.getPropertyManager());
		}));
	}

	@SuppressWarnings("unchecked")
	public <T extends Provider<?>> T getProvider(Class<T> clazz) {
		return (T) this.providers.get(clazz);
	}

	/**
	 * Loads data from a {@link PropertyManager} into all {@link Provider}s at once,
	 * utilizing {@link PropertyManager#getPropertyBatch(Collection)}. This will
	 * automatically call {@link Provider#onLoadFail(Throwable)} if an exception occurs.
	 *
	 * @param pm
	 *            The {@link PropertyManager} to use.
	 */
	public void loadAll(PropertyManager pm) {
		Collection<Provider<?>> providersValues = this.providers.values();
		try {

			long start = System.currentTimeMillis();

			List<String> keys = providersValues.stream().map(Provider::getDataKey).collect(Collectors.toList());
			Map<String, String> properties = pm.getPropertyBatch(keys).get();
			providersValues.forEach(p -> p.load(properties));

			LOG.debug("Loaded {} providers in {} milliseconds.", providersValues.size(),
				System.currentTimeMillis() - start);

		} catch (ExecutionException t) {
			LOG.debug("Failed to fetch data from the PropertyManager", t);

			for (Provider<? extends Object> provider : providersValues) {
				provider.setDefaultData();
				provider.onLoadFail(t);
			}

		} catch (InterruptedException t) {
			LOG.debug("Got interrupted while loading the providers.");
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Stores data from all registered {@link Provider}s into the given
	 * {@link PropertyManager}s. automatically call
	 * {@link Provider#onStoreFail(Throwable)} if an exception occurs.
	 *
	 * @param pm
	 *            The {@link PropertyManager} to use.
	 */
	public void storeAll(PropertyManager pm) {
		Collection<Provider<?>> providersValues = this.providers.values();
		long start = System.currentTimeMillis();

		providersValues.forEach(p -> {
			try {
				p.store(pm).get();
				LOG.debug("Stored {}.", p.getDataKey());
			} catch (ExecutionException e) {
				p.onStoreFail(e);

			} catch (InterruptedException e) {
				LOG.info("Got interrupted while storing providers.");
				Thread.currentThread().interrupt();
			}
		});

		LOG.debug("Stored {} providers in {} milliseconds.", providersValues.size(),
			System.currentTimeMillis() - start);
	}

	/**
	 * Cleans obsolete data from all providers at once. This is the same as calling
	 * {@link Provider#clean(Lithium, JDA)} on all {@link Provider}s.
	 *
	 * @param lithium
	 *            The {@link Lithium} instance.
	 * @param jda
	 *            The {@link JDA} instance.
	 * @return Total cleaned keys.
	 *
	 */
	public int cleanAll(Lithium lithium, JDA jda) {
		int objects = 0;
		for (Provider<?> provider : this.providers.values())
			objects += provider.clean(lithium, jda);

		return objects;
	}

}
