package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.CreateNK1SegmentForADTO01Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.RemoveEventSegment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.UpdatePersonIdentifierSegmentFirstName;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.AddHL7Segment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.RemoveHL7Segment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.UpdateHL7Segment;

@RemoveHL7Segment(removalClass = RemoveEventSegment.class)
@UpdateHL7Segment(updateClass = UpdatePersonIdentifierSegmentFirstName.class)
@AddHL7Segment(creationClass = CreateNK1SegmentForADTO01Message.class)
public class ADTA01UpdateConfigurationEgres extends BaseHL7MessageTransformationConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(ADTA01UpdateConfigurationEgres.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
