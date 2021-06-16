package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.hl7;

import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.BaseDataRetriever;

public class EmptyFieldValueRetriever extends BaseDataRetriever<MitafHL7Message> {

	@Override
	public String get(MitafHL7Message message) {
		return "";
	}
}
