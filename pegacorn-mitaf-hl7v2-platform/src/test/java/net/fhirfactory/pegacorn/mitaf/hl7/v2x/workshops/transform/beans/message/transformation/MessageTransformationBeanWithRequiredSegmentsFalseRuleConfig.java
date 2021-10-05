package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.ConfigPath;

@ConfigPath("net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.package6") // This config has a allowed segment annotations.
public class MessageTransformationBeanWithRequiredSegmentsFalseRuleConfig extends BaseMessageTransform {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransformationBeanWithRequiredSegmentsFalseRuleConfig.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
