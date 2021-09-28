package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class HL7RemoveSegmentTransformationStep extends BaseMitafMessageTransformStep {
	private static final Logger LOG = LoggerFactory.getLogger(HL7RemoveSegmentTransformationStep.class);
	
	protected String segmentCode;
	
	public HL7RemoveSegmentTransformationStep(String segmentCode) {
		this(segmentCode, new TrueRule());
	}

	public HL7RemoveSegmentTransformationStep(String segmentCode, Rule rule) {
		super(rule);
		
		this.segmentCode = segmentCode;
	}

	@Override
	public void process(Message message) throws HL7Exception {

		if (rule.executeRule(message)) {
			AbstractGroup group = (AbstractGroup) message.getMessage();
			
			String[] names = group.getNames();
			
			group.removeRepetition(segmentCode, 0);
		}
	}
	

	@Override
	public Logger getLogger() {
		return LOG;
	}
}
