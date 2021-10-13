package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration;

public enum Repetition {
	ALL("All"),
	FIRST(""), // This is correct.  The value is the prefix of the segment
	SECOND("2"),
	THIRD("3"),
	FOURTH("4"),
	FIFTH("5"),
	SIX("6"),
	SEVENTH("7"),
	EIGHT("8"),
	NINTH("9"),
	TENTH("10");
	
	private String value;
	
	Repetition(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
