package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.rule.Rule;
import org.slf4j.Logger;

import ca.uhn.hl7v2.HL7Exception;

/**
 * Base class for all MITAF message updater classes.  A updater is where the actual updating of a message occurs.
 *
 *
 */
public abstract class BaseMitafMessageUpdater {
	protected Rule rule;

	public BaseMitafMessageUpdater(Rule rule) {
		this.rule = rule;
	}
	
	public abstract Logger getLogger();

	/**
	 * Does the actual update.
	 * 
	 * @param message
	 */
	public abstract MitafMessage update(MitafMessage message) throws HL7Exception;
}
