package com.github.markozajc.lithium;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.markozajc.lithium.commands.Command;
import com.github.markozajc.lithium.commands.CommandList;
import com.github.markozajc.lithium.commands.CommandListBuilder;
import com.github.markozajc.lithium.data.properties.PropertyManager;
import com.github.markozajc.lithium.data.providers.Provider;
import com.github.markozajc.lithium.data.providers.ProviderManager;
import com.github.markozajc.lithium.data.source.DataSource;
import com.github.markozajc.lithium.handlers.CommandHandler;
import com.github.markozajc.lithium.handlers.ExceptionHandler;
import com.github.markozajc.lithium.listeners.ExceptionListener;
import com.github.markozajc.lithium.listeners.MessageListener;
import com.github.markozajc.lithium.processes.ProcessManager;
import com.github.markozajc.lithium.tasks.Task;
import com.github.markozajc.lithium.tasks.TaskChain;
import com.github.markozajc.lithium.utilities.EventWaiter;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Lithium {

	static final Logger LOG = LoggerFactory.getLogger(Lithium.class);

	private final BotConfiguration configuration;
	private PropertyManager propertyManager;
	private final ProviderManager providerManager;
	private final Handlers handlers;
	private final ProcessManager processManager;
	private CommandList commands;
	private final EventWaiter eventWaiter;

	public Lithium(BotConfiguration configuration, PersistentDataConfiguration dataConfiguration, Handlers handlers,
			JDABuilder jdaBuilder, List<BootTask> bootTasks, List<Function<Lithium, EventListener>> listeners,
			ProcessManager processManager) throws LoginException {
		this.configuration = configuration;
		this.handlers = handlers;
		this.processManager = processManager;
		this.eventWaiter = new EventWaiter(this);

		LOG.debug("Creating a ProviderManager..");
		this.providerManager = new ProviderManager(dataConfiguration.getProviders(), this);

		LOG.debug("Creating the bootstrap task chain..");
		List<Task> bootstrapTasks = new ArrayList<>();
		bootstrapTasks.add(new Task(() -> {

			CommandListBuilder cb = new CommandListBuilder();
			cb.registerCommands(configuration.getCommands());
			this.commands = cb.build();

		}, "load-commands"));

		bootstrapTasks.add(new Task(
				() -> this.propertyManager = dataConfiguration.getDataSource()
						.createPropertyManager(this.processManager.getExecutorService()),
				"create-property-manager",
				new Task(() -> this.providerManager.loadAll(this.propertyManager), "load-providers")));

		LOG.debug("Creating and registering event listeners..");
		jdaBuilder.addEventListener(listeners.stream().map(f -> f.apply(this)).toArray())
				.addEventListener(new Bootstrap(this, new TaskChain(bootstrapTasks, "lithium-boot"), bootTasks),
					new MessageListener(this), new ExceptionListener(this), this.eventWaiter)
				.build();

	}

	public static Logger getLog() {
		return LOG;
	}

	public PropertyManager getPropertyManager() {
		return this.propertyManager;
	}

	public ProviderManager getProviderManager() {
		return this.providerManager;
	}

	public Handlers getHandlers() {
		return this.handlers;
	}

	public CommandList getCommands() {
		return this.commands;
	}

	public BotConfiguration getConfiguration() {
		return this.configuration;
	}

	public ProcessManager getProcessManager() {
		return this.processManager;
	}

	public EventWaiter getEventWaiter() {
		return this.eventWaiter;
	}

	private static class Bootstrap extends ListenerAdapter {

		private final Lithium lithium;
		private final TaskChain tasks;
		private final List<BootTask> bootTasks;

		public Bootstrap(Lithium lithium, TaskChain tasks, List<BootTask> bootTasks) {
			this.lithium = lithium;
			this.tasks = tasks;
			this.bootTasks = bootTasks;
		}

		@Override
		public void onReady(ReadyEvent event) {
			try {
				this.tasks.executeAll().get();

				new TaskChain(this.bootTasks.stream()
						.map(bt -> new Task(() -> bt.run(event, this.lithium), "boot-task"))
						.collect(Collectors.toList()), "lithium-post-boot-chain").executeAll().get();
			} catch (ExecutionException e) {
				LOG.error("An exception was thrown while bootstrapping, shutting down JDA", e);
				event.getJDA().shutdown();

			} catch (InterruptedException e) {
				LOG.debug("Got interrupted while bootstrapping.");
				Thread.currentThread().interrupt();
			}

			LOG.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			LOG.info("\tLithium v{}", Constants.LITHIUM_VERSION);
			LOG.info("\t-as {}", event.getJDA().getSelfUser().getName());
			LOG.info("\tLaunch successful!");
			LOG.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		}
	}

	@FunctionalInterface
	public static interface BootTask {

		public void run(ReadyEvent event, Lithium lithium) throws Exception; // NOSONAR

	}

	public static class PersistentDataConfiguration {

		private final DataSource dataSource;
		private final List<Provider<?>> providers;

		public PersistentDataConfiguration(@Nonnull DataSource dataSource, @Nonnull List<Provider<?>> providers) {
			this.dataSource = dataSource;
			this.providers = providers;
		}

		public DataSource getDataSource() {
			return this.dataSource;
		}

		public List<Provider<?>> getProviders() { // NOSONAR
			return this.providers;
		}

	}

	public static class BotConfiguration {

		private final long ownerId;
		private final String defaultPrefix;
		private final List<Command> commands;
		private final String name;

		public BotConfiguration(long ownerId, String defaultPrefix, List<Command> commands, String name) {
			this.ownerId = ownerId;
			this.defaultPrefix = defaultPrefix;
			this.commands = commands;
			this.name = name;
		}

		public long getOwnerId() {
			return this.ownerId;
		}

		public String getDefaultPrefix() {
			return this.defaultPrefix;
		}

		public List<Command> getCommands() {
			return this.commands;
		}

		public String getName() {
			return this.name;
		}

		@Nonnull
		public User getOwner(JDA jda) {
			User owner = jda.getUserById(this.getOwnerId());
			if (owner == null)
				throw new IllegalStateException(
						"JDA couldn't find the bot's owner by their ID. Are you sure you have configured Lithium correctly?");
			return owner;
		}

	}

	public static class Handlers {

		private final CommandHandler command;
		private final ExceptionHandler exception;

		public Handlers(CommandHandler command, ExceptionHandler exception) {
			this.command = command;
			this.exception = exception;
		}

		public CommandHandler getCommand() {
			return this.command;
		}

		public ExceptionHandler getException() {
			return this.exception;
		}

	}

}
