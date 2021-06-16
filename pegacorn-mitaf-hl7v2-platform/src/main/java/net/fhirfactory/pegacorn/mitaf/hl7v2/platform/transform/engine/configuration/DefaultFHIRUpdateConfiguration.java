package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a default FHIR transformation configuration which currently does nothing.
 * 
 * @author Brendan Douglas
 *
 */
public class DefaultFHIRUpdateConfiguration extends BaseFHIRUpdateConfiguration  {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultFHIRUpdateConfiguration.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
