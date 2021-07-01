package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.rule;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.MitafMessage;

/**
 * Interface for all rules.  A rule determines if a message updating should be executed.
 * 
 * @author Brendan Douglas
 *
 */
public interface Rule {
	boolean executeRule(MitafMessage message);
}
