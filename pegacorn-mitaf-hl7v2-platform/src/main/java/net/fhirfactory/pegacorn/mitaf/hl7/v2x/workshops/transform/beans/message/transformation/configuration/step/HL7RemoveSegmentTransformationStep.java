package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.util.SegmentFinder;
import ca.uhn.hl7v2.util.Terser;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.TrueRule;

/**
 * Removes segments.  
 * 
 * @author Brendan Douglas
 *
 */
public class HL7RemoveSegmentTransformationStep extends BaseMitafMessageTransformStep {
	private static final Logger LOG = LoggerFactory.getLogger(HL7RemoveSegmentTransformationStep.class);
	
	protected String segmentCode;
	protected int repetition;
	
	public HL7RemoveSegmentTransformationStep(String segmentCode) {
		this(segmentCode, new TrueRule());
	}

	public HL7RemoveSegmentTransformationStep(String segmentCode, Rule rule) {
		super(rule);
		
		this.segmentCode = segmentCode;
	}

	@Override
	public void process(Message message) throws HL7Exception {
		Terser terser = new Terser(message);
		
		SegmentFinder finder = terser.getFinder();
		
		while(true) {
			try {
				String name = finder.iterate(true, false); // iterate segments only.  The first true = segments.
				
				if (name.startsWith(segmentCode)) {
					
					for (int i = 0; i < finder.getCurrentChildReps().length; i++) {
						Structure structure = finder.getCurrentStructure(i);
						
						if (rule.executeRule(message,i)) {
							AbstractSegment segment = (AbstractSegment)structure;
							segment.clear();
						}
					}
				}
			} catch(HL7Exception e) {
				break;
			}
		}
		
		// Update the message object with the changes.
		message.parse(message.toString());
	}
	

	@Override
	public Logger getLogger() {
		return LOG;
	}
}
