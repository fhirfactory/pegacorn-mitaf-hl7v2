package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a default HL7 transformation configuration which currently does nothing.
 * 
 * @author Brendan Douglas
 *
 */
public class DefaultHL7UpdateConfiguration extends BaseHL7UpdateConfiguration  {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultHL7UpdateConfiguration.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
