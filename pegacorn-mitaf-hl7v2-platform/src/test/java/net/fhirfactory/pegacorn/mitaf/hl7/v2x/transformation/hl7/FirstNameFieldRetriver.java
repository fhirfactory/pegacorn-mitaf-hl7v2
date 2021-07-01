package net.fhirfactory.pegacorn.mitaf.hl7.v2x.transformation.hl7;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.BaseDataRetriever;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.hl7.MitafHL7Message;

public class FirstNameFieldRetriver extends BaseDataRetriever<MitafHL7Message> {

	@Override
	public String get(MitafHL7Message message) {
		return "Peter";
	}
}
