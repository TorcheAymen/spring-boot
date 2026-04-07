package org.springframework.boot.system;

import org.jspecify.annotations.Nullable;

/**
 * Modern strategy to extract the PID using Java ProcessHandle API.
 */
public class Java9PidStrategy implements PidExtractionStrategy {

	@Override
	public @Nullable Long extractPid() throws Exception {
		return ProcessHandle.current().pid();
	}

}
