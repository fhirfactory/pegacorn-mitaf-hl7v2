package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractGroup;
import ca.uhn.hl7v2.model.Message;
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

		if (rule.executeRule(message)) {
			AbstractGroup group = (AbstractGroup) message.getMessage();
			
			// I think there is a bug in the HL7 library because instead of adding each repetition of a segment to a list which can then be accessed based on their index
			// it is appending a prefix number to the segment code and adding to a list where the list size is always 1.  The removeRepetition method below only works with index 0, this
			// doesn't match the method API doc.
			
			try {
				if (repetition == Repetition.ALL) {
					
					for (String name : group.getNames()) {
						if (name.startsWith(segmentCode)) {
							try {
								group.removeRepetition(name, 0);
							} catch(HL7Exception e) {
								LOG.info("Attept to remove a segment which does not exist");
							}					
						}
					}
					
				} else {
					String repetitionPrefix = repetition.getValue();
					group.removeRepetition(segmentCode + repetitionPrefix, 0);
				}
			} catch(HL7Exception e) {
				LOG.info("Attept to remove a segment which does not exist");
			}
		}
	}
	

	@Override
	public Logger getLogger() {
		return LOG;
	}
}
