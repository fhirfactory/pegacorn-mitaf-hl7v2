package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.package5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.BaseHL7MessageTransformationConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.RequiredHL7Segment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.Egress;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.MessageType;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.RemoveHL7Segment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.RemoveHL7Segments;

/**
 * A test transformation config class with allowed segment annotations.
 * 
 * @author Brendan Douglas
 *
 */
@Egress
@MessageType("ADT_A01")
@RequiredHL7Segment("MSH")
@RequiredHL7Segment("EVN")
public class ADTRequiredSegmentsTransformationConfiguration extends BaseHL7MessageTransformationConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(ADTRequiredSegmentsTransformationConfiguration.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
