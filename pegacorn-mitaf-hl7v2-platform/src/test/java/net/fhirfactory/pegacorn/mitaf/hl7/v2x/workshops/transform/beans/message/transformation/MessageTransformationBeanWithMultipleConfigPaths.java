package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.ConfigPath;

@ConfigPath("net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.package2")
@ConfigPath("net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.package1")
public class MessageTransformationBeanWithMultipleConfigPaths extends BaseMessageTransform {
	private static final Logger LOG = LoggerFactory.getLogger(HL7MessageTransformation.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
