package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.fhir.BaseFHIRResourceUpdater;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.rule.Rule;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.rule.TrueRule;

/**
 * An annotation describing a required FHIR transformation.
 * 
 * @author Brendan Douglas
 *
 */
@Repeatable(UpdateFHIRResources.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UpdateFHIRReource {
	public Class<? extends BaseFHIRResourceUpdater> transformationClass();

	public Class<? extends Rule> ruleClass() default TrueRule.class;
}
