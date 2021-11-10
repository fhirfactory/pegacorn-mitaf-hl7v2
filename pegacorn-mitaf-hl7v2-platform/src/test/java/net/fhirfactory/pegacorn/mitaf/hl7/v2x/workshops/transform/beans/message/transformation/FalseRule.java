package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;

/**
 * A rule which returns false so the transformation does not occur.
 * 
 * @author Brendan Douglas
 *
 */
public class FalseRule extends Rule {

	@Override
	public boolean executeRule(Message message) {
		return false;
	}

	@Override
	public boolean executeRule(Message message, int repetition) {
		return false;
	}
}
	
	

