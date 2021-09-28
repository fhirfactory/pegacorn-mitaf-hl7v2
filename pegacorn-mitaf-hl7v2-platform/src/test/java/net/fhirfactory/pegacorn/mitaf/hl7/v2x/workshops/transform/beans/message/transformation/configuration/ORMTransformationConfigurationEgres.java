package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.FalseRule;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.RemoveHL7Segment;

@RemoveHL7Segment(segmentCode = "ZDS",ruleClass = FalseRule.class)
public class ORMTransformationConfigurationEgres extends BaseHL7MessageTransformationConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(ORMTransformationConfigurationEgres.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
