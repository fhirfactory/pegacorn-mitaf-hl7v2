package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step;

import org.slf4j.Logger;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.TrueRule;

/**
 * Base class for all HL7 update transformation steps.
 * 
 * Each step needs to update 1 segment only.  This is not enforced in the code but please
 * make sure it does.
 * 
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL7UpdateTransformationStep extends BaseMitafMessageTransformStep {

	public abstract Logger getLogger();
	
	public BaseHL7UpdateTransformationStep() {
		super(new TrueRule());
	}
	
	public BaseHL7UpdateTransformationStep(Rule rule) {
		super(rule);
	}

	
	@Override
	public void process(Message message) throws HL7Exception {

		if (rule.executeRule(message)) {			
			doUpdate(message);
		}
	}
	
	
	/**
	 * Update the segment.
	 * 
	 * @param segment
	 * @param fieldValueRetriever
	 */
	protected abstract void doUpdate(Message message) throws HL7Exception;
}
