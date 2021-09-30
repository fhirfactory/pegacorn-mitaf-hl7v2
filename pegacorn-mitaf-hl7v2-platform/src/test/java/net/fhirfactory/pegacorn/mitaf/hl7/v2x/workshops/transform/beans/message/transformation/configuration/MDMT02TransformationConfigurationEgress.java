package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.RemoveHL7Segment;

@RemoveHL7Segment(segmentCode = "OBR")
@RemoveHL7Segment(segmentCode = "OBR2")
@RemoveHL7Segment(segmentCode = "OBR3")
@RemoveHL7Segment(segmentCode = "OBR4")
public class MDMT02TransformationConfigurationEgress extends BaseHL7MessageTransformationConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(MDMT02TransformationConfigurationEgress.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
