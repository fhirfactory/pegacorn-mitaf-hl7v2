package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration;

public enum Direction {
	INGRES("Ingres"), EGRESS("Egress");

	private String name;

	Direction(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
