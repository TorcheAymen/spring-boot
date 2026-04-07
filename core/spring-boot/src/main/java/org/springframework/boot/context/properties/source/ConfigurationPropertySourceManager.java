package org.springframework.boot.context.properties.source;

import org.springframework.core.env.Environment;

public interface ConfigurationPropertySourcesManager {
	void attach(Environment environment);
	Iterable<ConfigurationPropertySource> get(Environment environment);
}
