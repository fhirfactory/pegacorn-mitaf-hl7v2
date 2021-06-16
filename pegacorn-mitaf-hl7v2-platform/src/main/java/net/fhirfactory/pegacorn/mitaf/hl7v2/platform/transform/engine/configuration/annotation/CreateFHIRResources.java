package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation describing a required FHIR resource to be created.
 * 
 * @author Brendan Douglas
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CreateFHIRResources {
	public CreateFHIRResource[] value();
}
