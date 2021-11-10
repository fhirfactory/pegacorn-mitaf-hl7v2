package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.ConfigPath;

/**
 * A class use to demonstrate the use of the @ConfigPath annotation in a super class.  The config location in this class and the sub classes are used.
 * 
 * @author Brendan Douglas
 *
 */
@ApplicationScoped
@ConfigPath("net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.package7")
public class DefaultMessageTransform extends BaseMessageTransform {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultMessageTransform.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
