package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * A single subfield within a field.
 * 
 * @author Bendan_Douglas
 *
 */
public class Subfield implements Serializable {
	private static final long serialVersionUID = -8174677055244513493L;

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
	
	
    public FieldRepetition geFieldRepetition() {
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
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.toString());
	}


	@Override
	public boolean equals(Object obj) {	
		return Objects.equals(this.toString(), obj.toString());
	}
}
