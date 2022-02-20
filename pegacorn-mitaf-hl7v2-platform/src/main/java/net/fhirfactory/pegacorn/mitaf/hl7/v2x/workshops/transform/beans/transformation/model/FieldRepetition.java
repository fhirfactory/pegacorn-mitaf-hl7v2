package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A repetition of a field.
 * 
 * @author Brendan_Douglas
 *
 */
public class FieldRepetition {
	private List<Subfield>subFields = new ArrayList<>();
	private Field field = null;
	
	public FieldRepetition(String fieldRepetition, boolean handleSeperators, Field field) {
		this.field = field;
		
		String[] splitSubFieldRepetitions = null;
		
		if (handleSeperators) {
			splitSubFieldRepetitions = fieldRepetition.split("\\^");
		} else {
			splitSubFieldRepetitions = new String[1];
			splitSubFieldRepetitions[0] = fieldRepetition;
		}
				
		for (String value : splitSubFieldRepetitions) {
			Subfield subfield = new Subfield(value, this);
			subFields.add(subfield);
		}				
	}

	
	public List<Subfield> getSubFields() {
		return subFields;
	}
	

	public void setSubFields(List<Subfield> subFields) {
		this.subFields = subFields;
	}
	
	
	public String toString() {		
		return subFields.stream().map(Subfield::toString).collect(Collectors.joining("^"));
	}

	
	/**
	 * Returns a single sub field.
	 * 
	 * @param subFieldIndex
	 * @return
	 */
	public Subfield getSubfield(int subFieldIndex) {
		if (subFieldIndex > subFields.size()) {
			return null;
		}
		
		return subFields.get(--subFieldIndex);
	}
	
	
	/**
	 * Clears this field repetition.
	 * 
	 * @throws Exception
	 */
	public void clear() throws Exception {
		for (Subfield subField : subFields) {
			subField.clear();
		}
	}
	
	public Field getField() {
		return field;
	}	
	
	
	public String value() {
		return toString();
	}
	
	
	/**
	 * Clears this field repetitions.  Other repetitions are not cleared.
	 * 
	 * @param subFieldIndex
	 * @throws Exception
	 */
	public void clearSubField(int subFieldIndex) throws Exception {	
		Subfield subField = getSubfield(subFieldIndex);
		
		if (subField == null) {
			return;
		}
		
		subField.clear();
	}

	
	/**
	 * Adds a field at the specified index.
	 * 
	 * @param field
	 * @param index
	 * @throws Exception
	 */
	public void addSubField(Subfield subField, int index) throws Exception {
		index--;	
		
		if (index >= getSubFields().size()) {
			int sizeDifference = index - getSubFields().size();
			
			for (int i = 0; i < sizeDifference; i++) {
				getSubFields().add(new Subfield("", this));
			}
		}
		
		this.getSubFields().add(index, subField);
		
		this.field.getSegment().getMessage().refreshSourceHL7Message();
	}

	
	/**
	 * Sets the first subfield
	 * 
	 * @param value
	 * @throws Exception
	 */
	public void setValue(String value) throws Exception {
		Subfield subField = getSubfield(1);
		
		if (subField == null) {
			return;
		}
		
		subField.setValue(value);
	}
	
	
	/**
	 * Sets a value at the supplied subFIeldIndex.  If the subFieldIndex does not exist then the 
	 * sub field is created.
	 * 
	 * @param value
	 * @param subFieldIndex
	 * @throws Exception
	 */
	public void setValue(String value, int subFieldIndex) throws Exception {
		Subfield subField = getSubfield(subFieldIndex);
		
		if (subField == null) {
			addSubField(value, subFieldIndex);
		} else {
			subField.setValue(value);
		}
	}
	
	
	/**
	 * Adds a field at the specified index.  This adds a new sub field it does not update an existing one.
	 * 
	 * @param value
	 * @param index
	 * @throws Exception
	 */
	public void addSubField(String value, int index) throws Exception {
		addSubField(new Subfield(value, this),index);
	}
	
	
	/**
	 * Is this field repetition empty?
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		for (Subfield subFIeld : subFields) {
			if (!subFIeld.isEmpty()) {
				return false;
			}
		}
		
		return true;
	}
}
