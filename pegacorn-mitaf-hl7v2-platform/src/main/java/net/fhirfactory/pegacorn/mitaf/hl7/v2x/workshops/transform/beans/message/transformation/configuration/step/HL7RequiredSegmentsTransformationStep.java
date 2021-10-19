package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractGroup;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Structure;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.TrueRule;

/**
 * A class which removes segments which are not one of the allowed segments.
 * 
 * @author Brendan Douglas
 *
 */
public class HL7RequiredSegmentsTransformationStep extends BaseMitafMessageTransformStep {
	private static final Logger LOG = LoggerFactory.getLogger(HL7RequiredSegmentsTransformationStep.class);
	
	protected List<String>allowedSegmentCodes;
	
	public HL7RequiredSegmentsTransformationStep(List<String> segmentCodes) {
		this(segmentCodes, new TrueRule());
	}

	public HL7RequiredSegmentsTransformationStep(List<String> allowedSegmentCodes, Rule rule) {
		super(rule);
		
		this.allowedSegmentCodes = allowedSegmentCodes;
	}

	@Override
	public void process(Message message) throws HL7Exception {

		if (rule.executeRule(message)) {
			AbstractGroup group = (AbstractGroup) message.getMessage();
			
	
			for (String name : group.getNames()) {
				
				// Allow all segments which start with one of the allowed segment names.
				boolean matchFound = false;
				
				for (String allowedSegment : allowedSegmentCodes) {
					if (name.startsWith(allowedSegment)) {
						matchFound = true;
						break;
					}
				}
				
				if (!matchFound) {
					
					Structure[] segments = message.getAll(name);
					
					for (int i = 0; i < segments.length; i++) {
						try {
							group.removeRepetition(name, i);
						} catch(HL7Exception e) {
							LOG.info("Attept to remove a segment which does not exist");
						}
					}				
				}
			}
		}
	}

	
	@Override
	public Logger getLogger() {
		return LOG;
	}
}