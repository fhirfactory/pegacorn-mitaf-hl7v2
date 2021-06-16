package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.fhir2hl7.configuration;

import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration.BaseFHIR2HL7Configuration;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration.annotation.CreateHL7Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.fhir2hl7.CreateEVNSegment;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.fhir2hl7.CreateMSHSegment;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.fhir2hl7.CreateNK1Segment;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.fhir2hl7.CreatePIDSegment;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.fhir2hl7.CreatePV1Segment;

/**
 * @author Brendan Douglas
 *
 */
@CreateHL7Segment(creationClass = CreateMSHSegment.class)
@CreateHL7Segment(creationClass = CreateEVNSegment.class)
@CreateHL7Segment(creationClass = CreatePIDSegment.class)
@CreateHL7Segment(creationClass = CreateNK1Segment.class)
@CreateHL7Segment(creationClass = CreatePV1Segment.class)
public class ADTA01FHIR2HL7ConversionConfigurationEgres extends BaseFHIR2HL7Configuration {
	private static final Logger LOG = LoggerFactory.getLogger(ADTA01FHIR2HL7ConversionConfigurationEgres.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
