package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;

public class FalseRule implements Rule {

	@Override
	public boolean executeRule(Message message) {
		return false;
	}
	
	

}
