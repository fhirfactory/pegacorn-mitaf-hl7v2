package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v231.datatype.XPN;
import ca.uhn.hl7v2.model.v231.message.ADT_A01;
import ca.uhn.hl7v2.model.v231.segment.PID;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step.BaseHL7UpdateTransformationStep;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;

public class UpdatePersonIdentifierSegmentLastName extends BaseHL7UpdateTransformationStep {

	public UpdatePersonIdentifierSegmentLastName(Rule rule) {
		super(rule);
	}

	private static final Logger LOG = LoggerFactory.getLogger(UpdatePersonIdentifierSegmentLastName.class);

	@Override
	protected void doUpdate(Message message) throws DataTypeException {
		ADT_A01 adtMessage = (ADT_A01)message;
		
		PID pid = adtMessage.getPID();

		XPN[] patientNames = pid.getPatientName();

		patientNames[0].getFamilyLastName().getFamilyName().setValue((String) "Anderson");
	}

	@Override
	public Logger getLogger() {
		return LOG;
	}
}
