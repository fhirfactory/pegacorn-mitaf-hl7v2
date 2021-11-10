package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.package6;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.FalseRule;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.UpdatePersonIdentifierSegmentFirstName;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.UpdatePersonIdentifierSegmentLastName;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.BaseHL7MessageTransformationConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.Egress;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.MessageType;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.RequiredHL7Segment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.UpdateHL7Message;

/**
 * A test transformation config with a false rule so the transformation will not occur.
 * 
 * @author Brendan Douglas
 *
 */
@Egress
@MessageType("ADT_A01")
@RequiredHL7Segment(value="MSH", ruleClass = FalseRule.class)
@RequiredHL7Segment(value = "EVN", ruleClass = FalseRule.class)
@UpdateHL7Message(updateClass = UpdatePersonIdentifierSegmentFirstName.class, ruleClass = FalseRule.class)
@UpdateHL7Message(updateClass = UpdatePersonIdentifierSegmentLastName.class, ruleClass = FalseRule.class)
public class ADTRequiredSegmentsFalseRuleTransformationConfiguration extends BaseHL7MessageTransformationConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(ADTRequiredSegmentsFalseRuleTransformationConfiguration.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
