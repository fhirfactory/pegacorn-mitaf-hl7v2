package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.hl7.BaseHL7SegmentRemover;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.rule.Rule;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.rule.TrueRule;

/**
 * An annotation describing a required HL7 segment to be removed.
 * 
 * @author Brendan Douglas
 *
 */
@Repeatable(RemoveHL7Segments.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RemoveHL7Segment {
	public Class<? extends BaseHL7SegmentRemover> removalClass();

	public Class<? extends Rule> ruleClass() default TrueRule.class;
}
