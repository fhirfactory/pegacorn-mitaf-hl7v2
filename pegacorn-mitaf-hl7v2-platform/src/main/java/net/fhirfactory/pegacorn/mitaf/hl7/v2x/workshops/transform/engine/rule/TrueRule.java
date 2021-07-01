package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.rule;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.MitafMessage;

/**
 * A rule which always returns true.
 * 
 * @author Brendan Douglas
 *
 */
public class TrueRule implements Rule {

	@Override
	public boolean executeRule(MitafMessage message) {
		return true;
	}
}
