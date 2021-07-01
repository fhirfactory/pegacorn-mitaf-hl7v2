package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.annotation.UpdateFHIRReource;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.fhir.BaseFHIRResourceUpdater;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.rule.Rule;

/**
 * Base configuration class for FHIR transformation configurations.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseFHIRUpdateConfiguration extends BaseConfiguration {
	private List<BaseFHIRResourceUpdater> resourceUpdaters = new ArrayList<>();

	public BaseFHIRUpdateConfiguration() {

		UpdateFHIRReource[] transformationAnnotations = this.getClass().getAnnotationsByType(UpdateFHIRReource.class);

		for (UpdateFHIRReource annotation : transformationAnnotations) {

			Rule rule = null;

			// Instantiate the rule class
			try {
				Constructor<?> constructor = annotation.ruleClass().getConstructor();
				rule = (Rule) constructor.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Error creating the rule class", e);
			}

			// Instantiate the transformation class
			try {
				Constructor<?> constructor = annotation.transformationClass().getConstructor(Rule.class);
				BaseFHIRResourceUpdater resourceUpdater = (BaseFHIRResourceUpdater) constructor.newInstance(rule);

				this.resourceUpdaters.add(resourceUpdater);
			} catch (Exception e) {
				throw new RuntimeException("Error creating the rule class", e);
			}
		}
	}

	public List<BaseFHIRResourceUpdater> getResourceUpdaters() {
		return resourceUpdaters;
	}

}
