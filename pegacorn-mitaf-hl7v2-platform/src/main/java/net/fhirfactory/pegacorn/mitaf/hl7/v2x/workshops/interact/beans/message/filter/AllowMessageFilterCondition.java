package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

/**
 * The default filter condition which allows the message to be sent.
 * 
 * @author Brendan Douglas
 *
 */
public class AllowMessageFilterCondition implements FilterCondition {
	
    private static final Logger LOG = LoggerFactory.getLogger(AllowMessageFilterCondition.class);

	@Override
	public Logger getLogger() {
		return LOG;
	}
	
	
	@Override
	public boolean execute(Message message) throws HL7Exception {
		return true; // Allow the message
	}
}
