package org.springframework.boot.system;

import org.jspecify.annotations.Nullable;

/**
 * Mock strategy for testing ApplicationPid.
 */
public class MockPidStrategy implements PidExtractionStrategy {

	private final Long mockPid;

	public MockPidStrategy(Long mockPid) {
		this.mockPid = mockPid;
	}

	@Override
	public @Nullable Long extractPid() throws Exception {
		return this.mockPid;
	}

}
