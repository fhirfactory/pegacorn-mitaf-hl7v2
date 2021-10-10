package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

/**
 * Interface for all filter conditions.  A filter condition contains the rules for
 * filtering.
 * 
 * @author Brendan Douglas
 *
 */
public interface FilterCondition {
	boolean execute(Message message) throws HL7Exception;
}
