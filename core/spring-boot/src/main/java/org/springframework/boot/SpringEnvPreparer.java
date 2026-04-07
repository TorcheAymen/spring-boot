package org.springframework.boot;

import java.util.Map;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.env.DefaultPropertiesPropertySource;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.boot.bootstrap.DefaultBootstrapContext;

/**
 * Composant extrait de SpringApplication 
 * Responsabilité : Préparation, création et configuration de l'environnement applicatif.
 */
class SpringEnvPreparer {

	private final ApplicationContextFactory applicationContextFactory;
	private final ApplicationProperties properties;
	private final boolean addConversionService;
	private final boolean addCommandLineProperties;
	private final boolean isCustomEnvironment;
	private final Class<?> mainApplicationClass;
	private final Map<String, Object> defaultProperties;
	private final ClassLoader classLoader;
	private final Class<? extends ConfigurableEnvironment> environmentClass;
	private ConfigurableEnvironment environment;
	private ConfigurableEnvironment envForPrepareEnvironment;

	SpringEnvPreparer(ApplicationContextFactory factory, ApplicationProperties properties,
			boolean addConversionService, boolean addCommandLineProperties, boolean isCustomEnvironment,
			Class<?> mainApplicationClass, Map<String, Object> defaultProperties, ClassLoader classLoader,
			ConfigurableEnvironment environment, Class<? extends ConfigurableEnvironment> environmentClass) {
		this.applicationContextFactory = factory;
		this.properties = properties;
		this.addConversionService = addConversionService;
		this.addCommandLineProperties = addCommandLineProperties;
		this.isCustomEnvironment = isCustomEnvironment;
		this.mainApplicationClass = mainApplicationClass;
		this.defaultProperties = defaultProperties;
		this.classLoader = classLoader;
		this.environment = environment;
		this.environmentClass = environmentClass;
	}

	public ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners,
			DefaultBootstrapContext bootstrapContext, ApplicationArguments applicationArguments) {
		
		ConfigurableEnvironment env = getOrCreateEnvironment();
		
		configureEnvironment(env, applicationArguments.getSourceArgs());
		ConfigurationPropertySources.attach(env);
		listeners.environmentPrepared(bootstrapContext, env);
		ApplicationInfoPropertySource.moveToEnd(env);
		DefaultPropertiesPropertySource.moveToEnd(env);
		Assert.state(!env.containsProperty("spring.main.environment-prefix"),
				"Environment prefix cannot be set via properties.");
		bindToSpringApplication(env);
		
		if (!this.isCustomEnvironment) {
			EnvironmentConverter environmentConverter = new EnvironmentConverter(this.classLoader);
			env = environmentConverter.convertEnvironmentIfNecessary(env, this.environmentClass);
		}
		ConfigurationPropertySources.attach(env);
		
		this.envForPrepareEnvironment = env;
		return env;
	}

	private ConfigurableEnvironment getOrCreateEnvironment() {
		if (this.environment != null) {
			return this.environment;
		}
		WebApplicationType webApplicationType = this.properties.getWebApplicationType();
		ConfigurableEnvironment env = this.applicationContextFactory.createEnvironment(webApplicationType);
		if (env == null && this.applicationContextFactory != ApplicationContextFactory.DEFAULT) {
			env = ApplicationContextFactory.DEFAULT.createEnvironment(webApplicationType);
		}
		return (env != null) ? env : new ApplicationEnvironment();
	}

	protected void configureEnvironment(ConfigurableEnvironment env, String[] args) {
		if (this.addConversionService) {
			env.setConversionService(new ApplicationConversionService());
		}
		configurePropertySources(env, args);
		configureProfiles(env, args);
	}

	protected void configurePropertySources(ConfigurableEnvironment env, String[] args) {
		MutablePropertySources sources = env.getPropertySources();
		if (!CollectionUtils.isEmpty(this.defaultProperties)) {
			DefaultPropertiesPropertySource.addOrMerge(this.defaultProperties, sources);
		}
		if (this.addCommandLineProperties && args.length > 0) {
			String name = CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME;
			PropertySource<?> source = sources.get(name);
			if (source != null) {
				CompositePropertySource composite = new CompositePropertySource(name);
				composite.addPropertySource(new SimpleCommandLinePropertySource("springApplicationCommandLineArgs", args));
				composite.addPropertySource(source);
				sources.replace(name, composite);
			}
			else {
				sources.addFirst(new SimpleCommandLinePropertySource(args));
			}
		}
		env.getPropertySources().addLast(new ApplicationInfoPropertySource(this.mainApplicationClass));
	}

	protected void configureProfiles(ConfigurableEnvironment env, String[] args) {
	}

	protected void bindToSpringApplication(ConfigurableEnvironment env) {
		try {
			Binder.get(env).bind("spring.main", Bindable.ofInstance(this.properties));
		}
		catch (Exception ex) {
			throw new IllegalStateException("Cannot bind to SpringApplication", ex);
		}
	}

	public ConfigurableEnvironment getEnvForPrepareEnvironment() {
		return this.envForPrepareEnvironment;
	}
}