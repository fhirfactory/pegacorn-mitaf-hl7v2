package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.RemoveEventSegment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.UpdatePersonIdentifierSegmentFirstName;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.RemoveHL7Segment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.UpdateHL7Message;

@RemoveHL7Segment(removalClass = RemoveEventSegment.class)
@UpdateHL7Message(updateClass = UpdatePersonIdentifierSegmentFirstName.class)
public class ADTA01TransformationConfigurationEgres extends BaseHL7MessageTransformationConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(ADTA01TransformationConfigurationEgres.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
