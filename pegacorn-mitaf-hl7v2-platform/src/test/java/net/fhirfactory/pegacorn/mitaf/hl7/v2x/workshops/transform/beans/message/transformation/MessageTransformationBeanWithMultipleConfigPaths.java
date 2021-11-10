package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.ConfigPath;

/**
 * This test transformation bean points to 2 config locations.  The rules in ALL config
 * files found in these locations are executed in the order they are defined.
 * 
 * @author Brendan Douglas
 *
 */
@ConfigPath("net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.package2")
@ConfigPath("net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.package1")
public class MessageTransformationBeanWithMultipleConfigPaths extends BaseMessageTransform {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransformationBeanWithMultipleConfigPaths.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
