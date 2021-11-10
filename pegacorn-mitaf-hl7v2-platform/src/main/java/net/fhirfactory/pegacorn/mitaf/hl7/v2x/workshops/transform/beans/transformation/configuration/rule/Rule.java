package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule;

import ca.uhn.hl7v2.model.Message;

/**
 * Interface for all rules.  A rule determines if a message updating should be executed.
 * 
 * @author Brendan Douglas
 *
 */
public class Rule {
	public boolean executeRule(Message message) {
		return true;
	}
	
	public boolean executeRule(Message message, int repetition) {
		return true;
	}
}
