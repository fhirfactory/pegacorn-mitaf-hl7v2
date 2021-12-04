package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation;

import ca.uhn.hl7v2.model.Segment;

/**
 * An action to executes for a segment.
 * 
 * @author Brendan Douglas
 *
 */
public interface SegmentAction {

	void execute(Segment segment) throws Exception;
}
