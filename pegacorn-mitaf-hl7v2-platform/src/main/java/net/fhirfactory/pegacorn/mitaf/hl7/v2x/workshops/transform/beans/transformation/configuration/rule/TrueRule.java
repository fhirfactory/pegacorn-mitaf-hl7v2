package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule;

import ca.uhn.hl7v2.model.Message;

/**
 * A rule which always returns true.
 * 
 * @author Brendan Douglas
 *
 */
public class TrueRule implements Rule {

	@Override
	public boolean executeRule(Message message) {
		return true;
	}
}
