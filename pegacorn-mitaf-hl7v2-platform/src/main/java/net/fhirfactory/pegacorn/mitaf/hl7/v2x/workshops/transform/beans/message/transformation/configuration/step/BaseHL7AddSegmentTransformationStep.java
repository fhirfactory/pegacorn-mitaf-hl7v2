package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;

/**
 * Base class for all HL7 add new segment transformation steps.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL7AddSegmentTransformationStep extends BaseMitafMessageTransformStep {
	
	public BaseHL7AddSegmentTransformationStep(Rule rule) {
		super(rule);
	}

	@Override
	public void process(Message message) throws HL7Exception {
		if (rule.executeRule(message)) {
			createNewSegment((Message)message.getMessage());
		}
	}


	/**
	 * Add a segment.
	 * 
	 * @param segment
	 * @throws DataTypeException
	 */
	protected abstract void createNewSegment(Message message) throws DataTypeException;
}
