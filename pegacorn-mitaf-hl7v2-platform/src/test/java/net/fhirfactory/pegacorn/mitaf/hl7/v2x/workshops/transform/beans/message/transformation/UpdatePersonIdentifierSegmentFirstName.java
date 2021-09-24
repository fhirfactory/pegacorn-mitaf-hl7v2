package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v231.datatype.XPN;
import ca.uhn.hl7v2.model.v231.segment.PID;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step.BaseHL7UpdateSegmentTransformationStep;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;

public class UpdatePersonIdentifierSegmentFirstName extends BaseHL7UpdateSegmentTransformationStep {

	public UpdatePersonIdentifierSegmentFirstName(Rule rule) {
		super(rule, "PID");
	}

	private static final Logger LOG = LoggerFactory.getLogger(UpdatePersonIdentifierSegmentFirstName.class);

	@Override
	protected void doUpdate(AbstractSegment segment) throws DataTypeException {
		PID pid = (PID) segment;

		XPN[] patientNames = pid.getPatientName();

		patientNames[0].getGivenName().setValue((String) "Peter");
	}

	@Override
	public Logger getLogger() {
		return LOG;
	}
}
