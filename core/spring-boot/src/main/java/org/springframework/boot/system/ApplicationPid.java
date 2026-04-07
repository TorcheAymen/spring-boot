/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * An application process ID.
 *
 * @author Phillip Webb
 * @since 2.0.0
 */
public class ApplicationPid {

	private static final PosixFilePermission[] WRITE_PERMISSIONS = { PosixFilePermission.OWNER_WRITE,
			PosixFilePermission.GROUP_WRITE, PosixFilePermission.OTHERS_WRITE };

	private final @Nullable Long pid;

	private final java.util.List<PidExtractionStrategy> strategies;

	public ApplicationPid() {
		this.strategies = java.util.List.of(new Java9PidStrategy(), new JmxPidStrategy());
		this.pid = currentProcessPid();
	}

	public ApplicationPid(java.util.List<PidExtractionStrategy> strategies) {
		this.strategies = strategies;
		this.pid = currentProcessPid();
	}

	protected ApplicationPid(@Nullable Long pid) {
		this.strategies = java.util.Collections.emptyList();
		this.pid = pid;
	}

	private @Nullable Long currentProcessPid() throws ApplicationPidException {
		for (PidExtractionStrategy strategy : this.strategies) {
			try {
				Long extractedPid = strategy.extractPid();
				if (extractedPid != null) {
					return extractedPid;
				}
			}
			catch (Exception ex) {
				// Ignore and try the next strategy
			}
		}
		throw new ApplicationPidException("Failed to get current process PID from " + this.strategies.size() + " strategies.");
	}

	/**
	 * Return if the application PID is available.
	 * @return {@code true} if the PID is available
	 * @since 3.4.0
	 */
	public boolean isAvailable() {
		return this.pid != null;
	}

	/**
	 * Return the application PID as a {@link Long}.
	 * @return the application PID or {@code null}
	 * @since 3.4.0
	 */
	public @Nullable Long toLong() {
		return this.pid;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ApplicationPid other) {
			return ObjectUtils.nullSafeEquals(this.pid, other.pid);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(this.pid);
	}

	@Override
	public String toString() {
		return (this.pid != null) ? String.valueOf(this.pid) : "???";
	}

	/**
	 * Write the PID to the specified file.
	 * @param file the PID file
	 * @throws IllegalStateException if no PID is available.
	 * @throws IOException if the file cannot be written
	 */
	public void write(File file) throws IOException {
		Assert.state(this.pid != null, "No PID available");
		createParentDirectory(file);
		if (file.exists()) {
			assertCanOverwrite(file);
		}
		try (FileWriter writer = new FileWriter(file)) {
			writer.append(String.valueOf(this.pid));
		}
	}

	private void createParentDirectory(File file) {
		File parent = file.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
	}

	private void assertCanOverwrite(File file) throws IOException {
		if (!file.canWrite() || !canWritePosixFile(file)) {
			throw new FileNotFoundException(file + " (permission denied)");
		}
	}

	private boolean canWritePosixFile(File file) throws IOException {
		try {
			Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(file.toPath());
			for (PosixFilePermission permission : WRITE_PERMISSIONS) {
				if (permissions.contains(permission)) {
					return true;
				}
			}
			return false;
		}
		catch (UnsupportedOperationException ex) {
			// Assume that we can
			return true;
		}
	}

}
