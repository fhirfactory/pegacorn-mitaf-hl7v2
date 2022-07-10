package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * A sub-subfield.  Subfields are delimited by & within a field.
 * 
 * @author Bendan_Douglas
 *
 */
public class SubSubfield implements Serializable {
	private static final long serialVersionUID = -8174677055244513493L;

	private String value;
	
	private Subfield subField = null;
	
	public SubSubfield(String value, Subfield subField) {
		this.value = value;
		this.subField = subField;
	}

	public String value() {
		return value;
	}

	public void setValue(String value) throws Exception {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	
	public void clear() throws Exception {
		setValue("");
	}
	
	
    public Subfield geSubField() {
		return subField;
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
