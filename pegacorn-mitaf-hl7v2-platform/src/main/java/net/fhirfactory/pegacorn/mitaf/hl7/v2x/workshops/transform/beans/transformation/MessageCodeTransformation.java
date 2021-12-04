package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation;

import ca.uhn.hl7v2.model.Message;

/**
 * Transforms a message
 * 
 * @author Brendan Douglas
 *
 */
public interface MessageCodeTransformation {
	
	void execute(Message message) throws Exception;
}
