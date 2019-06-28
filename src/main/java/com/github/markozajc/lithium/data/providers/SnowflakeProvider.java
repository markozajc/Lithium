package com.github.markozajc.lithium.data.providers;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.gson.reflect.TypeToken;

public abstract class SnowflakeProvider<V> extends MapProvider<Long, V> {

	private Type type;

	public SnowflakeProvider() {
		this.type = getTypeToken().getType();
	}

	@Override
	public Map<Long, V> constructData(String json) {
		if (json == null)
			return new ConcurrentHashMap<>();

		Map<? extends Long, ? extends V> deserialized = getGson().fromJson(json, this.type);
		if (deserialized == null)
			return new ConcurrentHashMap<>();

		return new ConcurrentHashMap<>(new HashMap<>(deserialized));
	}

	public abstract TypeToken<Map<Long, V>> getTypeToken();

	@SuppressWarnings("null")
	@Override
	protected final Predicate<Long> getObsoleteFilter() {
		LongPredicate obsoleteFilter = getSnowflakeObsoleteFilter();
		if (obsoleteFilter == null)
			return null;
		// Passes the null(ability)

		return obsoleteFilter::test;
		// Returns the LongPredicate, wrapped into a Predicate<Long>
	}

	@Nullable
	protected abstract LongPredicate getSnowflakeObsoleteFilter();

}
