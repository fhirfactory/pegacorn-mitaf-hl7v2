package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.ConfigPath;

/**
 * 
 * This transform bean should find package6 from this class and package 7 from the superclass.
 *
 */
@ConfigPath("net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.package1")
public class MessageTransformationBeanExtendsTheDefaultConfig extends DefaultMessageTransform {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransformationBeanExtendsTheDefaultConfig.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
