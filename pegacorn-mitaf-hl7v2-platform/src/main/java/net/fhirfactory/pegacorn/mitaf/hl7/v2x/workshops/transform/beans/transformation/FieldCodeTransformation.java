package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation;

import ca.uhn.hl7v2.model.Message;

/**
 * Transforms a single field.
 * 
 * @author Brendan Douglas
 *
 */
public interface FieldCodeTransformation {
	
	String execute(Message message) throws Exception;
}
