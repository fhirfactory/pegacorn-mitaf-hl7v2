package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.MDM_T02;

public class EVNSegmentExistsFilterCondition implements FilterCondition {

	@Override
	public boolean execute(Message message) throws HL7Exception {
		MDM_T02 adtMessage = (MDM_T02)message;
		
		return adtMessage.toString().contains("EVN");
	}
}
