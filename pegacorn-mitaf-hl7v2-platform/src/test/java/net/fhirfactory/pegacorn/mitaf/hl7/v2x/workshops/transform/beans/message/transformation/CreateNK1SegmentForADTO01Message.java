package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v231.message.ADT_A01;
import ca.uhn.hl7v2.model.v231.segment.NK1;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step.BaseHL7AddSegmentTransformationStep;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;

public class CreateNK1SegmentForADTO01Message extends BaseHL7AddSegmentTransformationStep {
	private static final Logger LOG = LoggerFactory.getLogger(CreateNK1SegmentForADTO01Message.class);
	
	public CreateNK1SegmentForADTO01Message(Rule rule) {
		super(rule);
	}

	@Override
	protected void createNewSegment(Message message) throws DataTypeException {
		ADT_A01 adtMessage = (ADT_A01)message;
		NK1 nk1 = adtMessage.getNK1();
		
		nk1.getJobStatus().setValue("Employed full time");
	}

	@Override
	public Logger getLogger() {
		return LOG;
	}
}
