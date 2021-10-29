package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractGroup;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Structure;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.MessageUtils;
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
		this(segmentCode, new TrueRule(), -1);
	}

	public HL7RemoveSegmentTransformationStep(String segmentCode, Rule rule, int repetition) {
		super(rule);
		
		this.segmentCode = segmentCode;
		this.repetition = repetition;
	}

	@Override
	public void process(Message message) throws HL7Exception {
		//TODO fix this.  This works fine for the current remove segments but will not work on some messages where the HL7 library groups them eg. ORM groups PID under PATIENT.  This only works when the segment
		// is a child of the root message.  
		// I would prefer not to use the HL7 library and just process the message as a string.  Same as the logic for the required segment annotation.
		
		if (!MessageUtils.doesSegmentExist(message, segmentCode)) {
			return;
		}

		AbstractGroup group = (AbstractGroup) message.getMessage();

	  	Structure[] segments = message.getAll(segmentCode);
	  	
	  	List<String> allSegmentsCodes = new ArrayList<String>();
	  	allSegmentsCodes.add(segmentCode);
	  	
	  	// Remove one because index is 0 based.
	  	repetition--;
	  	

	  	// We need to get the segment code from each group that exists in the message.  Groups are named like OBX, OBX2, OBX3 etc
	  	// Groups are created by the HL7 library when the same segment codes are not grouped together in the message.
	  	int groupIndex = 2;

	  	boolean found = true;
	  	
	  	while (found) {
	  		try {
	  			message.get(segmentCode + String.valueOf(groupIndex));
	  			allSegmentsCodes.add(segmentCode + String.valueOf(groupIndex));
	  			groupIndex++;
	  		} catch(HL7Exception e ) {
	  			found = false;
	  		}
	  	}
	  	
	  	for (String segmentToRemove : allSegmentsCodes) {
	  	
			try {
				if (repetition < 0) {
					
					for (int i = 0; i < segments.length; i++) {
						
						try {
							if (rule.executeRule(message, i)) {
								group.removeRepetition(segmentToRemove, i);
								i--;
							}
							
						} catch(HL7Exception e) {
							LOG.info("Attept to remove a segment which does not exist");
						}					
					}
					
				} else {
	
					if (rule.executeRule(message, repetition)) {
						group.removeRepetition(segmentToRemove, Integer.valueOf(repetition));
					}
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
