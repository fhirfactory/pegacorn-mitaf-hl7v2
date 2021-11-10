package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.package1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.UpdatePersonIdentifierSegmentFirstName;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.UpdatePersonIdentifierSegmentLastName;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.BaseHL7MessageTransformationConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.Egress;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.MessageType;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.RemoveHL7Segment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.UpdateHL7Message;

/**
 * A test transformation config class.  Performs both a remove and updates.
 * 
 * @author Brendan Douglas
 *
 */
@Egress
@MessageType("ADT_A01")
@RemoveHL7Segment("EVN")
@UpdateHL7Message(updateClass = UpdatePersonIdentifierSegmentFirstName.class)
@UpdateHL7Message(updateClass = UpdatePersonIdentifierSegmentLastName.class)
public class ADTO01TransformationConfiguration extends BaseHL7MessageTransformationConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(ADTO01TransformationConfiguration.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
