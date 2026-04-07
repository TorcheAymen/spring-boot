package org.springframework.boot.env;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

/**
 * Super-classe abstraite pour les implementations de {@link PropertySourceLoader}.
 * Factorise la logique répétitive d'instanciation des PropertySources (avec track d'origine).
 */
public abstract class AbstractPropertySourceLoader implements PropertySourceLoader {

	@Override
	public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
		List<Map<String, ?>> propertiesList = loadProperties(name, resource);
		if (propertiesList.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<PropertySource<?>> propertySources = new ArrayList<>(propertiesList.size());
		for (int i = 0; i < propertiesList.size(); i++) {
			String documentNumber = (propertiesList.size() != 1) ? " (document #" + i + ")" : "";
			propertySources.add(new OriginTrackedMapPropertySource(name + documentNumber,
					Collections.unmodifiableMap(propertiesList.get(i)), true));
		}
		
		return propertySources;
	}

	/**
	 * Parse resource logic to be implemented by child classes (e.g., Yaml or Properties parsers).
	 *
	 * @param name the root property source name
	 * @param resource the resource to load
	 * @return a list of maps representing the parsed properties
	 * @throws IOException in case of I/O errors or parsing failures
	 */
	protected abstract List<Map<String, ?>> loadProperties(String name, Resource resource) throws IOException;

}
