package com.github.markozajc.lithium.utilities;

@FunctionalInterface
public interface ThrowableConsumer<T, E extends Throwable> {

	void accept(T t) throws E;

}
