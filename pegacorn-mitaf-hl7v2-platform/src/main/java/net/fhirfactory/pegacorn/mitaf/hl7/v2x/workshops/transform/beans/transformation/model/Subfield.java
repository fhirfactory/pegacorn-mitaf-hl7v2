package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * A sub sub field.  This is for sub fields which have multiple components.
 * 
 * @author Bendan_Douglas
 *
 */
public class Subfield extends MessageComponent implements Serializable {
	private static final long serialVersionUID = -8174677055244513493L;

	private List<SubSubfield> subSubFields = new ArrayList<>();
	
	private FieldRepetition fieldRepetition = null;
	
	public Subfield(String subField, boolean handleSeperators, FieldRepetition fieldRepetition) {
		this.fieldRepetition = fieldRepetition;
		
		String[] splitSubSubFields = null;
		
		if (handleSeperators) {
			splitSubSubFields = subField.split("\\&");
		} else {
			splitSubSubFields = new String[1];
			splitSubSubFields[0] = subField;
		}
		
		for (String value : splitSubSubFields) {
			SubSubfield subSubField = new SubSubfield(value, this);
			subSubFields.add(subSubField);
		}		
	}

	@Override
	public String value() {
		return subSubFields.stream().map(SubSubfield::toString).collect(Collectors.joining("&"));
	}

	@Override
	public void setValue(String value) throws Exception {
		subSubFields.clear();

		String[] splitSubSubFields = null;
		
		splitSubSubFields = value.split("\\&");
		
		for (String subSubFieldValue : splitSubSubFields) {
			SubSubfield subSubField = new SubSubfield(subSubFieldValue, this);
			subSubFields.add(subSubField);
		}
	}
	
	
	@Override
	public String toString() {
		return value();
	}
	
	
	@Override
	public void clear() throws Exception {
		setValue("");
	}
	
	
    public FieldRepetition geFieldRepetition() {
		return fieldRepetition;
	}
    
    
    public SubSubfield getSubSubField(int subSubFieldIndex) {
    	return subSubFields.get(--subSubFieldIndex);
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
