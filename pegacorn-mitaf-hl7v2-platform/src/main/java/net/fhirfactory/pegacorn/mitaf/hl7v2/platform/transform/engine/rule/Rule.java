package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.rule;

import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.MitafMessage;

/**
 * Interface for all rules.  A rule determines if a message updating should be executed.
 * 
 * @author Brendan Douglas
 *
 */
public interface Rule {
	boolean executeRule(MitafMessage message);
}
