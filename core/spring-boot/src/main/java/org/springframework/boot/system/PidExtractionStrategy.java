package org.springframework.boot.system;

import org.jspecify.annotations.Nullable;

/**
 * Strategy interface to extract the application PID.
 */
public interface PidExtractionStrategy {

	/**
	 * Extract the PID using this strategy.
	 * @return the extracted PID or {@code null} if it cannot be extracted
	 * @throws Exception if an error occurs during extraction
	 */
	@Nullable
	Long extractPid() throws Exception;

}
