package net.fhirfactory.pegacorn.mitaf.hl7.v2x.transformation.hl7.configuration;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.BaseHL7UpdateConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.annotation.RemoveHL7Segment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.annotation.UpdateHL7Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.transformation.hl7.RemoveEventSegment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.transformation.hl7.UpdatePersonIdentifierSegmentFirstName;

@RemoveHL7Segment(removalClass = RemoveEventSegment.class)
@UpdateHL7Segment(updateClass = UpdatePersonIdentifierSegmentFirstName.class)
public class ADTA01UpdateConfigurationEgres extends BaseHL7UpdateConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(ADTA01UpdateConfigurationEgres.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
