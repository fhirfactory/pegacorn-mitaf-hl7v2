package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.fhir;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.rule.Rule;
import org.slf4j.Logger;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.BaseMitafMessageUpdater;

/**
 * Base class for all FHIR resource modifier classes.  The idea is there will be one subclass for each field in a resource that
 * needs to be changed.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseFHIRResourceUpdater extends BaseMitafMessageUpdater {
	public abstract Logger getLogger();

	public BaseFHIRResourceUpdater(Rule rule) {
		super(rule);
	}
}
