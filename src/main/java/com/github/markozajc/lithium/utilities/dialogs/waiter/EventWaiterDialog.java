package com.github.markozajc.lithium.utilities.dialogs.waiter;

import java.util.function.Predicate;

import com.github.markozajc.lithium.processes.context.ProcessContext;
import com.github.markozajc.lithium.utilities.ThrowableConsumer;
import com.github.markozajc.lithium.utilities.EventWaiter.Waiter;
import com.github.markozajc.lithium.utilities.dialogs.Dialog;
import com.github.markozajc.lithium.utilities.dialogs.Dialogs;
import com.github.markozajc.lithium.utilities.dialogs.message.MessageDialog;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.Event;

public abstract class EventWaiterDialog<T extends Event> implements Dialog {

	private final ProcessContext context;
	private final Class<T> eventClass;
	private final MessageDialog messageDialog;
	private final ThrowableConsumer<T, Throwable> action;

	public EventWaiterDialog(ProcessContext context, Class<T> eventClass, MessageDialog messageDialog,
			ThrowableConsumer<T, Throwable> action) {
		this.context = context;
		this.eventClass = eventClass;
		this.messageDialog = messageDialog;
		this.action = action;
	}

	public final MessageDialog getMessageDialog() {
		return this.messageDialog;
	}

	public ProcessContext getContext() {
		return this.context;
	}

	public final void registerWaiter(Waiter<T> waiter) {
		this.context.getLithium().getEventWaiter().submitWaiter(this.eventClass, waiter);
	}

	public final Waiter<T> createWaiter(Predicate<T> isRight, MessageChannel channel, long userId) {
		return this.createWaiter(isRight, Dialogs.getDefaultCleanupPredicate(channel, userId));
	}

	public final Waiter<T> createWaiter(Predicate<T> isRight, Predicate<Void> cleanupPredicate) {
		return new Waiter<>(isRight, cleanupPredicate, this.action, this.context);
	}

}
