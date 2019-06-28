package com.github.markozajc.lithium.data.providers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.markozajc.lithium.Lithium;

import net.dv8tion.jda.core.JDA;

public abstract class MapProvider<K, V> extends Provider<Map<K, V>> {

	@Override
	public Map<K, V> getDefaultData() {
		return new ConcurrentHashMap<>();
	}

	/**
	 * @return an unmodifiable map
	 */
	@Override
	public Map<K, V> getData() {
		return Collections.unmodifiableMap(super.getData());
	}

	@Override
	protected Map<K, V> constructData(String json) {
		return new ConcurrentHashMap<>(super.constructData(json));
	}

	@Override
	public int clean(Lithium lithium, JDA jda) {
		Predicate<? super K> filter = getObsoleteFilter();
		if (filter == null)
			return 0;
		// In case the provided filter is null

		List<K> obsolete = this.data.keySet().stream().filter(filter).collect(Collectors.toList());
		obsolete.forEach(this.data::remove);
		// Finds & removes all obsolete elements from the data

		store(lithium.getPropertyManager());
		// Stores new provider data into the default property manager

		return obsolete.size();
	}

	@Nullable
	protected abstract Predicate<K> getObsoleteFilter();

}
