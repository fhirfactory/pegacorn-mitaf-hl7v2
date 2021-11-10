package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.package4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.BaseHL7MessageTransformationConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.Egress;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.MessageType;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.RemoveHL7Segment;

/**
 * A test transformation config class used by all ADT messages.  The * in the @MessageType
 * annotation is a wildcard so in this config it is for all ADT message types.
 * 
 * @author Brendan Douglas
 *
 */
@Egress
@MessageType("ADT_*")
@RemoveHL7Segment("PID")
public class ADTWildcardTransformationConfiguration extends BaseHL7MessageTransformationConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(ADTWildcardTransformationConfiguration.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
