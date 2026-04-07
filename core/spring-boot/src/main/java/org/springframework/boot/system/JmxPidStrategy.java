package org.springframework.boot.system;

import java.lang.management.ManagementFactory;

import org.jspecify.annotations.Nullable;

/**
 * Historical fallback strategy to extract the PID using JMX RuntimeMXBean name.
 */
public class JmxPidStrategy implements PidExtractionStrategy {

	@Override
	public @Nullable Long extractPid() throws Exception {
		String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		if (jvmName != null) {
			int index = jvmName.indexOf('@');
			if (index > 0) {
				return Long.parseLong(jvmName.substring(0, index));
			}
		}
		return null;
	}

}
