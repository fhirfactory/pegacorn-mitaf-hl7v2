package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.ConfigPath;

@ConfigPath("net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.package1")
public class MessageTransformationBeanWithOneConfigPath extends BaseMessageTransform {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransformationBeanWithOneConfigPath.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
