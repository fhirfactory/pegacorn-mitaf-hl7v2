package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.hl7;

import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Segment;

/**
 * Base class for all HL7 segment creators  A HL7 segment creator knows how to create a {@link Segment} from the resources within {@link Bundle}
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL7SegmentCreator {

	public abstract Segment create(Bundle source) throws HL7Exception;
}
