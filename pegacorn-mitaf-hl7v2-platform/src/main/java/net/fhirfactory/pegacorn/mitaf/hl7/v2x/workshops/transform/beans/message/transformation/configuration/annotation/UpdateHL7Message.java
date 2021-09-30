package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step.BaseHL7UpdateTransformationStep;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.TrueRule;

/**
 * An annotation to use when updating a message.
 * 
 * 
 * @author Brendan Douglas
 *
 */
@Repeatable(UpdateHL7Messages.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UpdateHL7Message {
	public Class<? extends BaseHL7UpdateTransformationStep> updateClass();

	public Class<? extends Rule> ruleClass() default TrueRule.class;
}