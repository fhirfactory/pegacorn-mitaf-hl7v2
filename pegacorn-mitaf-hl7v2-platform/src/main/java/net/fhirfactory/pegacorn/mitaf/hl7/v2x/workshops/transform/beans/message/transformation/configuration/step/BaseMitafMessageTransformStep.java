package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step;

import org.slf4j.Logger;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;

/**
 * Base class for all HL7 transformation classes.
 *
 *
 */
public abstract class BaseMitafMessageTransformStep {
	protected Rule rule;

	public BaseMitafMessageTransformStep(Rule rule) {
		this.rule = rule;
	}
	
	public abstract Logger getLogger();

	/**
	 * Do the transformation.
	 * 
	 * @param message
	 */
	public abstract void process(Message message) throws HL7Exception;
}
