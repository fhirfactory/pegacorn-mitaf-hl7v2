package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.Egress;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.Ingress;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.MessageType;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.MessageTypes;

/**
 * A class to instantiate the appropriate transformation class or the default configuration.
 * 
 * @author Brendan Douglas
 *
 */
public class ConfigurationUtil {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationUtil.class);
	
	private static final String MESSAGE_TYPE_WILDCARD = "*";

	private ConfigurationUtil() {
		// Hide the constructor.
	}

	/**
	 * Returns the configuration classes.  
	 * 
	 * @param configurationType
	 * @param basePackageName
	 * @param filenamePrefix
	 * @return
	 */
	public static List<BaseHL7MessageTransformationConfiguration> getConfiguration(List<String> packageNames, Direction direction, String messageName) {
		
		List<BaseHL7MessageTransformationConfiguration>configClasses = new ArrayList<>();
		
		for (String packageName : packageNames) {
		
			Reflections ref = new Reflections(packageName);
			
			List<Class<?>>classesWithAnnotation = new ArrayList<>();
		
			for (Class<?> cl : ref.getTypesAnnotatedWith(MessageType.class)) {
				classesWithAnnotation.add(cl);
			}
			
			for (Class<?> cl : ref.getTypesAnnotatedWith(MessageTypes.class)) {
				classesWithAnnotation.add(cl);
			}			
			
			
			for (Class<?> classWithAnnotation : classesWithAnnotation) {
				for (MessageType messageTypeAnnotation : classWithAnnotation.getAnnotationsByType(MessageType.class)) {
					
					boolean messageTypeContainsWildcard = messageTypeAnnotation.value().endsWith(MESSAGE_TYPE_WILDCARD);

					// Check to see if the message name matches or a partial match if a wildcard was supplied.
					if (messageTypeAnnotation.value().equals(messageName) || (messageTypeContainsWildcard && messageTypeAnnotation.value().substring(0, 3).equals(messageName.substring(0, 3)))) {
						
						// A class has been found. Now check the message flow direction.

						if (direction == Direction.EGRESS) {
							Egress messageFlowDirectionAnnotation = classWithAnnotation.getAnnotation(Egress.class);
							if (messageFlowDirectionAnnotation != null) {
								configClasses.add(instantiateConfigurationClass(classWithAnnotation.getName()));
							}
						} else if (direction == Direction.INGRES) {
							Ingress messageFlowDirectionAnnotation = classWithAnnotation.getAnnotation(Ingress.class);
							if (messageFlowDirectionAnnotation != null) {
								configClasses.add(instantiateConfigurationClass(classWithAnnotation.getName()));
							}							
						}
					}
				}
			}
		}
		
		return configClasses;
	}
	
	

	private static BaseHL7MessageTransformationConfiguration instantiateConfigurationClass(String classname) {
		BaseHL7MessageTransformationConfiguration configuration = null;

		// Use reflection to instantiate the message transformer class.
		try {			
			Class<?> transformationClass = Class.forName(classname);
			Constructor<?> constructor = transformationClass.getConstructor();
			configuration = (BaseHL7MessageTransformationConfiguration) constructor.newInstance();

			LOG.info("Found a transformer configuration file: {}", configuration);

			return configuration;
		} catch (Exception e ) {
			throw new RuntimeException("Unable to create the configuration class.  Class name: " + classname);
			
		}
	}
}
