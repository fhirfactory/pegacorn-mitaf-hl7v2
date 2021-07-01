package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.annotation.CreateFHIRResource;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.fhir.BaseFHIRResourceCreator;

/**
 * Base configuration class for HL7 to FHIR conversion configurations.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL72FHIRConfiguration extends BaseConfiguration {
	private List<BaseFHIRResourceCreator> resourceCreators = new ArrayList<>();

	/**
	 * 
	 */
	public BaseHL72FHIRConfiguration() {

		CreateFHIRResource[] transformationAnnotations = this.getClass().getAnnotationsByType(CreateFHIRResource.class);

		for (CreateFHIRResource annotation : transformationAnnotations) {

			try {
				Constructor<?> constructor = annotation.creationClass().getConstructor();
				BaseFHIRResourceCreator resourceCreator = (BaseFHIRResourceCreator) constructor.newInstance();

				this.resourceCreators.add(resourceCreator);
			} catch (Exception e) {
				throw new RuntimeException("Error creating the fhir resource creator class", e);
			}
		}
	}

	public List<BaseFHIRResourceCreator> getResourceCreators() {
		return resourceCreators;
	}
}
