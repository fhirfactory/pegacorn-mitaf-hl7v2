package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default message transformation bean which does nothing.  If no transformations
 * are required then do not extend this class and this class will be injected.
 * 
 * @author Brendan Douglas
 *
 */
@ApplicationScoped
public class DefaultMessageTransform extends BaseMessageTransform {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultMessageTransform.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
