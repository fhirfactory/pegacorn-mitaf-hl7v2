package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractGroup;
import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.TrueRule;

/**
 * Base class for all HL7 remove segment transformation steps.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL7RemoveSegmentTransformationStep extends BaseMitafMessageTransformStep {
	protected String segmentCode;
	
	public BaseHL7RemoveSegmentTransformationStep(String segmentCode) {
		this(new TrueRule(), segmentCode);
	}

	public BaseHL7RemoveSegmentTransformationStep(Rule rule, String segmentCode) {
		super(rule);
		
		this.segmentCode = segmentCode;
	}

	@Override
	public void process(Message message) throws HL7Exception {
	
		if (rule.executeRule(message)) {
			AbstractGroup group = (AbstractGroup) message.getMessage();
			group.removeRepetition(segmentCode, 0);
		}
	}
}
