package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step;

import org.slf4j.Logger;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractGroup;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;

/**
 * Base class for all HL7 update segment transformation steps.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL7UpdateSegmentTransformationStep extends BaseMitafMessageTransformStep {
	protected String segmentCode;

	public abstract Logger getLogger();
	
	public BaseHL7UpdateSegmentTransformationStep(Rule rule, String segmentCode) {
		super(rule);

		this.segmentCode = segmentCode;
	}

	
	@Override
	public void process(Message message) throws HL7Exception {

		if (rule.executeRule(message)) {
			AbstractGroup group = (AbstractGroup) message;

			AbstractSegment segment = (AbstractSegment) group.get(segmentCode);
			
			doUpdate(segment);
		}
	}
	
	
	/**
	 * Update the segment.
	 * 
	 * @param segment
	 * @param fieldValueRetriever
	 */
	protected abstract void doUpdate(AbstractSegment segment) throws DataTypeException;
}
