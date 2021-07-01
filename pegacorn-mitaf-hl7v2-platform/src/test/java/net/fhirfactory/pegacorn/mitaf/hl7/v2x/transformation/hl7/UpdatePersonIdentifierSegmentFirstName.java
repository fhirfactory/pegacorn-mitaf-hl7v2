package net.fhirfactory.pegacorn.mitaf.hl7.v2x.transformation.hl7;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.BaseDataRetriever;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.hl7.BaseHL7SegmentUpdater;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.hl7.MitafHL7Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.rule.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v231.datatype.XPN;
import ca.uhn.hl7v2.model.v231.segment.PID;

public class UpdatePersonIdentifierSegmentFirstName extends BaseHL7SegmentUpdater {

	public UpdatePersonIdentifierSegmentFirstName(Rule rule) {
		super(rule, "PID", new FirstNameFieldRetriver());
	}

	private static final Logger LOG = LoggerFactory.getLogger(UpdatePersonIdentifierSegmentFirstName.class);

	@Override
	protected void doUpdate(AbstractSegment segment, BaseDataRetriever<MitafHL7Message> fieldValueRetriever) throws DataTypeException {
		PID pid = (PID) segment;

		XPN[] patientNames = pid.getPatientName();

		MitafHL7Message message = new MitafHL7Message(segment.getMessage());

		patientNames[0].getGivenName().setValue((String) fieldValueRetriever.get(message));
	}

	@Override
	public Logger getLogger() {
		return LOG;
	}
}
