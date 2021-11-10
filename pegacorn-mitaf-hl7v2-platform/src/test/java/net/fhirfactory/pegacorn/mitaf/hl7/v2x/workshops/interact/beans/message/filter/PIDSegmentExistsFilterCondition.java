package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v231.message.ADT_A01;

public class PIDSegmentExistsFilterCondition implements FilterCondition {
	private static final Logger LOG = LoggerFactory.getLogger(PIDSegmentExistsFilterCondition.class);

	@Override
	public boolean execute(Message message) throws HL7Exception {
		ADT_A01 adtMessage = (ADT_A01)message;
		
		return adtMessage.toString().contains("PID");
	}

	@Override
	public Logger getLogger() {
		return LOG;
	}
}
