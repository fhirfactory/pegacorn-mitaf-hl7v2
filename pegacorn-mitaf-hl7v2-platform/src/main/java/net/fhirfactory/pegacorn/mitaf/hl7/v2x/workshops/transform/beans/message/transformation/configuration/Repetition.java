package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration;

public enum Repetition {
	ALL("All"),
	FIRST("0"),
	SECOND("1"),
	THIRD("2"),
	FOURTH("3"),
	FIFTH("4"),
	SIX("5"),
	SEVENTH("6"),
	EIGHT("7"),
	NINTH("8"),
	TENTH("9");
	
	private String value;
	
	Repetition(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
