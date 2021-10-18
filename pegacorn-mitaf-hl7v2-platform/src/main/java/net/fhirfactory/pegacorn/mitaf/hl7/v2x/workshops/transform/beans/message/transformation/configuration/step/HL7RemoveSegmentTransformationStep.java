package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractGroup;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Structure;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.MessageUtils;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.Repetition;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.TrueRule;

/**
 * Removes a segment.
 * 
 * @author Brendan Douglas
 *
 */
public class HL7RemoveSegmentTransformationStep extends BaseMitafMessageTransformStep {
	private static final Logger LOG = LoggerFactory.getLogger(HL7RemoveSegmentTransformationStep.class);
	
	protected String segmentCode;
	protected Repetition repetition;
	
	public HL7RemoveSegmentTransformationStep(String segmentCode) {
		this(segmentCode, new TrueRule(), Repetition.FIRST);
	}

	public HL7RemoveSegmentTransformationStep(String segmentCode, Rule rule, Repetition repetition) {
		super(rule);
		
		this.segmentCode = segmentCode;
		this.repetition = repetition;
	}

	@Override
	public void process(Message message) throws HL7Exception {
		if (!MessageUtils.doesSegmentExist(message, segmentCode)) {
			return;
		}

		AbstractGroup group = (AbstractGroup) message.getMessage();
		
	  	Structure[] segments = message.getAll(segmentCode);
			
		try {
			if (repetition == Repetition.ALL) {
				
				for (int i = 0; i < segments.length; i++) {
					
					try {
						if (rule.executeRule(message, i)) {
							group.removeRepetition(segmentCode, i);
							i--;
						}
						
					} catch(HL7Exception e) {
						LOG.info("Attept to remove a segment which does not exist");
					}					
				}
				
			} else {

				if (rule.executeRule(message, Integer.valueOf(repetition.getValue()).intValue())) {
					group.removeRepetition(segmentCode, Integer.valueOf(repetition.getValue()).intValue());
				}
			}
		} catch(HL7Exception e) {
			LOG.info("Attept to remove a segment which does not exist");
		}
	}
	

	@Override
	public Logger getLogger() {
		return LOG;
	}
}
