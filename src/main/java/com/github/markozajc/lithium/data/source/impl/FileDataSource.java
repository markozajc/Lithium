package com.github.markozajc.lithium.data.source.impl;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import com.github.markozajc.lithium.data.properties.PropertyManager;
import com.github.markozajc.lithium.data.properties.impl.FilePropertyManager;
import com.github.markozajc.lithium.data.source.DataSource;

public class FileDataSource implements DataSource {

	private final File directory;

	public FileDataSource(File directory) {
		this.directory = directory;
	}

	@Override
	public PropertyManager createPropertyManager(ExecutorService executor) throws IOException {
		return new FilePropertyManager(this.directory, executor);
	}

}
