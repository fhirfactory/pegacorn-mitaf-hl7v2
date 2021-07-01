package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.hl7.BaseHL7SegmentCreator;

/**
 * An annotation describing a required FHIR resource to be created.
 * 
 * @author Brendan Douglas
 *
 */
@Repeatable(CreateFHIRResources.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CreateFHIRResource {
	public Class<? extends BaseHL7SegmentCreator> creationClass();
}
