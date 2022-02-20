package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import org.apache.commons.lang3.StringUtils;

/**
 * A single subfield within a field.
 * 
 * @author Bendan_Douglas
 *
 */
public class Subfield {
	private String value;
	
	private FieldRepetition fieldRepetition = null;
	
	public Subfield(String value, FieldRepetition fieldRepetition) {
		this.value = value;
		this.fieldRepetition = fieldRepetition;
	}

	public String value() {
		return value;
	}

	public void setValue(String value) throws Exception {
		this.value = value;
		
		this.fieldRepetition.getField().getSegment().getMessage().refreshSourceHL7Message();
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	
	public void clear() throws Exception {
		setValue("");
	}
	
	
	public FieldRepetition geFIeldRepetition() {
		return fieldRepetition;
	}
	
	
	/**
	 * Is this subfield empty?
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return StringUtils.isBlank(toString());
	}
}
