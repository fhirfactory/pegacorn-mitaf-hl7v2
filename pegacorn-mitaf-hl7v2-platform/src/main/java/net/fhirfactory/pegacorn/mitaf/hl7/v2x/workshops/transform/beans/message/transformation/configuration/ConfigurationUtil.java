package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to instantiate the appropriate transformation class or the default configuration.
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
	public static BaseHL7MessageTransformationConfiguration getConfiguration( String basePackageName, Direction direction, String filenamePrefix) {
		
		List<String>classNames = new ArrayList<>();

		// Create 2 possible files names.  eg. ORMO01 and the other ORM.  The generic one needs to be added last.
		String[] messageNames = new String[] {filenamePrefix.replace("_", ""), filenamePrefix.substring(0,3)};
		
		for (String name : messageNames) {
			StringBuilder sb = new StringBuilder();
			sb.append(basePackageName);
			sb.append(".");
			sb.append("configuration");
			sb.append(".");
			sb.append(name);
			sb.append("TransformationConfiguration");
			sb.append(direction);
			
			classNames.add(sb.toString());
		}

		// Now try and instantiate the class.
		return instantiateConfigurationClass(classNames, DefaultHL7TransformationConfiguration.class);
	}

	
	@SuppressWarnings("unused")
	private static BaseHL7MessageTransformationConfiguration instantiateConfigurationClass(List<String>classNames, Class<?>defaultConfiguration) {
		BaseHL7MessageTransformationConfiguration configuration = null;

			
		// Try all of the possible class names.  
		for (String className : classNames) {
			try {
		
				Class<?> transformationClass = Class.forName(className);
				Constructor<?> constructor = transformationClass.getConstructor();
				configuration = (BaseHL7MessageTransformationConfiguration) constructor.newInstance();
	
				LOG.info("Found a transformer configuration file: {}", configuration);
				
				return configuration;
			} catch(ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
				// Just ignore
			}
		}
		
		
		// Try and use the default
		if (configuration == null) {
		
			if (defaultConfiguration != null) {
				LOG.info("No transformation class found so using the default");
				

				try {
					Constructor<?> constructor = defaultConfiguration.getConstructor();
					configuration = (BaseHL7MessageTransformationConfiguration) constructor.newInstance();
					
					return configuration;
				} catch(Exception e1) {
					throw new RuntimeException("Unable to instantiate the default configuration", e1);
				}
			} 
			
			throw new RuntimeException("Unable to find a configuration class");
		}
		
		return configuration;
	}
}
