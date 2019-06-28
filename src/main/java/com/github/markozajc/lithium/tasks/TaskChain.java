package com.github.markozajc.lithium.tasks;

import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.markozajc.lithium.utilities.Counter;

public class TaskChain {

	private static final Logger LOG = LoggerFactory.getLogger(TaskChain.class);

	private final List<Task> tasks;
	private final String name;

	public TaskChain(List<Task> tasks, String name) {
		this.tasks = tasks;
		this.name = name;
	}

	public Future<Void> executeAll() {
		return new Task(() -> {
			List<Future<Void>> futures = this.tasks.stream().map(Task::execute).collect(Collectors.toList());

			Counter counter = new Counter();
			for (Future<Void> future : futures) {
				try {
					LOG.info("[{}%] Executing {}", (int) ((float) counter.getCount() / (float) futures.size() * 100f),
						this.name);

					future.get();
					counter.count();
				} catch (InterruptedException e) {
					LOG.warn("Got interrupted while executing the task chain.");
					Thread.currentThread().interrupt();
				}
			}

			LOG.info("[100%] Executing {}", this.name);
		}, this.name).execute();
	}

}
