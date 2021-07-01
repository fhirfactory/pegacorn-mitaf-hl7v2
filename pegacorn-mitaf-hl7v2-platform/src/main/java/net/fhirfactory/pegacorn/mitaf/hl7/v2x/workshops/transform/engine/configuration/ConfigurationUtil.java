package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All class to retrieve the appropriate transformation class or the default configuration.
 * 
 * @author Brendan Douglas
 *
 */
public class ConfigurationUtil {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationUtil.class);

	private ConfigurationUtil() {
		// Hide the constructor.
	}

	/**
	 * Returns the appropriate configuration object.
	 * 
	 * @param configurationType
	 * @param basePackageName
	 * @param filenamePrefix
	 * @return
	 */
	public static BaseConfiguration getConfiguration(ConfigurationType configurationType, String basePackageName, Direction direction, String filenamePrefix) {
		StringBuilder sb = new StringBuilder();

		sb.append(basePackageName);
		sb.append(".");
		sb.append(configurationType.getEndPackageName());
		sb.append(".");
		sb.append(filenamePrefix.replaceAll("_", ""));
		sb.append(configurationType.getFilenameSuffix());
		sb.append(direction);

		// Now try and instantiate the class.
		return instantiateConfigurationClass(sb.toString(), configurationType.getDefaultConfiguration());
	}

	private static BaseConfiguration instantiateConfigurationClass(String classname, Class<?>defaultConfiguration) {
		BaseConfiguration configuration = null;

		// Use reflection to instantiate the message transformer class.
		try {
			Class<?> transformationClass = Class.forName(classname);
			Constructor<?> constructor = transformationClass.getConstructor();
			configuration = (BaseConfiguration) constructor.newInstance();

			LOG.info("Found a transformer configuration file: {}", configuration);

			return configuration;
		} catch (ClassNotFoundException e) {
			
			if (defaultConfiguration != null) {
				LOG.info("No transformation class found so using the default");
				

				try {
					Constructor<?> constructor = defaultConfiguration.getConstructor();
					configuration = (BaseConfiguration) constructor.newInstance();
				} catch(Exception e1) {
					throw new RuntimeException("Unable to instantiate the default configuration", e);
				}
				
				return configuration;
			} 
			
			throw new RuntimeException("Unable to find a configuration class");
		} catch (Exception e) {
			throw new RuntimeException("Error constructing a transformation class.  The configuration might be incorrect", e);
		}
	}
}
