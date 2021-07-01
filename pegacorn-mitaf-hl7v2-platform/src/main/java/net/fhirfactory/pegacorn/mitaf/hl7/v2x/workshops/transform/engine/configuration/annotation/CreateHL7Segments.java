package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation describing a required HL7 segment to be created.
 * 
 * @author Brendan Douglas
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CreateHL7Segments {
	public CreateHL7Segment[] value();
}
