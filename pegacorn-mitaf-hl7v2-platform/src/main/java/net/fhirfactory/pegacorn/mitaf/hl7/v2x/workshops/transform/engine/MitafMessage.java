package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.BaseConfiguration;
import org.slf4j.Logger;

import ca.uhn.hl7v2.HL7Exception;

/**
 * Base class for all mitaf messages.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MitafMessage {
	protected abstract Logger getLogger();

	/**
	 * Returns the message wrapped in this object.
	 * 
	 * @return
	 */
	public abstract Object getMessage();

	/**
	 * 
	 * 
	 * @param config
	 * @return
	 */
	public abstract MitafMessage convert(BaseConfiguration config) throws HL7Exception;

	/**
	 * 
	 * 
	 * @param config
	 * @return
	 */
	public abstract void update(BaseConfiguration config) throws HL7Exception;
}
