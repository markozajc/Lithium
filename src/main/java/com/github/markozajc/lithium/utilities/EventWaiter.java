package com.github.markozajc.lithium.utilities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.github.markozajc.lithium.Lithium;
import com.github.markozajc.lithium.processes.LithiumProcess;
import com.github.markozajc.lithium.processes.context.ProcessContext;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;

public class EventWaiter implements EventListener {

	@SuppressWarnings("rawtypes")
	final Map<Class<? extends Event>, Set<Waiter>> waiters = new ConcurrentHashMap<>();
	private final Lithium lithium;

	public EventWaiter(Lithium lithium) {
		this.lithium = lithium;
	}

	@SuppressWarnings("rawtypes")
	public <T extends Event> void submitWaiter(Class<T> eventClass, Waiter<T> waiter) {
		Set<Waiter> waiterSet = this.waiters.computeIfAbsent(eventClass,
			c -> Collections.synchronizedSet(new HashSet<>()));
		waiterSet.add(waiter);
	}

	public void handleThrowable(ProcessContext context, Throwable throwable) {
		this.lithium.getHandlers().getException().handleThrowable(context, throwable);
	}

	@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
	@SuppressWarnings("unchecked")
	@Override
	public void onEvent(Event event) {
		for (Class<?> clazz = event.getClass(); Event.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
			@SuppressWarnings("rawtypes")
			Set<Waiter> set = this.waiters.get(clazz);
			if (set != null) {
				Class<?> clazzCopy = clazz;
				set.stream()
						.filter(w -> w.canAccept(event))
						.forEach(w -> this.lithium.getProcessManager()
								.submitProcess(w.createProcess(event, this, clazzCopy)));
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public Map<Class<? extends Event>, Set<Waiter>> getWaiters() {
		return Collections.unmodifiableMap(this.waiters);
	}

	public void cleanWaiters() {
		this.waiters.values().stream().forEach(sw -> sw.removeIf(Waiter::canCleanup));
	}

	public static class Waiter<T extends Event> {

		final ProcessContext parentContext;
		private final Predicate<T> isRight;
		private final Predicate<Void> canCleanup;
		private final ThrowableConsumer<T, Throwable> action;

		public Waiter(Predicate<T> isRight, Predicate<Void> canCleanup, ThrowableConsumer<T, Throwable> action,
				ProcessContext parentContext) {
			this.isRight = isRight;
			this.canCleanup = canCleanup;
			this.action = action;
			this.parentContext = parentContext;
		}

		public ProcessContext getParentContext() {
			return this.parentContext;
		}

		public boolean canCleanup() {
			return this.canCleanup.test(null);
		}

		public boolean canAccept(T event) {
			return this.isRight.test(event);
		}

		void accept(T event) throws Throwable { // NOSONAR
			this.action.accept(event);
		}

		public LithiumProcess<T> createProcess(T event, EventWaiter eventWaiter, Class<T> clazz) {
			return new LithiumProcess<T>(this.parentContext) {

				@Override
				public T call() {
					try {
						eventWaiter.waiters.get(clazz).remove(Waiter.this);
						accept(event);
					} catch (Throwable t) {
						eventWaiter.handleThrowable(Waiter.this.parentContext, t);
					}

					return event;
				}
			};
		}

	}
}
