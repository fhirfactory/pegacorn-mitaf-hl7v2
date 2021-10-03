package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to use when removing a segment.
 * 
 * @author Brendan Douglas
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RemoveHL7Segments {
	public RemoveHL7Segment[] value();
}
