package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.fhir;

import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;

import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.hl7.MitafHL7Message;
import ca.uhn.hl7v2.HL7Exception;

/**
 * Base class for all FHIR resource creators  A FHIR resource creator knows how to create a {@link Resource} from a {@link MitafHL7Message}
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseFHIRResourceCreator {
	public abstract Logger getLogger();

	public abstract Resource create(MitafHL7Message source) throws HL7Exception;
}
