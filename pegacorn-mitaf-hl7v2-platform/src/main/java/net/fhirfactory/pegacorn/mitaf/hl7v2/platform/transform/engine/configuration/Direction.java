package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration;

public enum Direction {
	INGRESS("Ingress"), EGRES("Egres");

	private String name;

	Direction(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
