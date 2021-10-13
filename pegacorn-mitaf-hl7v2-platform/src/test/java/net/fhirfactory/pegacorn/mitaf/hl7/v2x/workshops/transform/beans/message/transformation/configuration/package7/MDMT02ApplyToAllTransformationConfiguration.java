package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.package7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.BaseHL7MessageTransformationConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.Repetition;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.Egress;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.MessageType;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.RemoveHL7Segment;

/**
 * A test transformation config class.  Removes multiple segments.
 * 
 * @author Brendan Douglas
 *
 */
@Egress
@MessageType("MDM_T02")
@RemoveHL7Segment(value = "OBR", repetition = Repetition.FIRST)
@RemoveHL7Segment(value = "OBR", repetition = Repetition.SECOND)
@RemoveHL7Segment(value = "OBR", repetition = Repetition.THIRD)
@RemoveHL7Segment(value = "OBR", repetition = Repetition.FOURTH)
public class MDMT02ApplyToAllTransformationConfiguration extends BaseHL7MessageTransformationConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(MDMT02ApplyToAllTransformationConfiguration.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
