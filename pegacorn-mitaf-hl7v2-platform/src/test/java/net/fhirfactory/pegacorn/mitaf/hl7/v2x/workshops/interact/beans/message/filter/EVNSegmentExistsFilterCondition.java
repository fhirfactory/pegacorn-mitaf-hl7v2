package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.MDM_T02;

public class EVNSegmentExistsFilterCondition implements FilterCondition {
    private static final Logger LOG = LoggerFactory.getLogger(EVNSegmentExistsFilterCondition.class);

	@Override
	public boolean execute(Message message) throws HL7Exception {
		MDM_T02 adtMessage = (MDM_T02)message;
		
		return adtMessage.toString().contains("EVN");
	}

	@Override
	public Logger getLogger() {
		return LOG;
	}
	
	
}
