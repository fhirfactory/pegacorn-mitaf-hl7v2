package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ca.uhn.hl7v2.HL7Exception;

/**
 * A repetition of a field.
 * 
 * @author Brendan_Douglas
 *
 */
public class FieldRepetition implements Serializable  {
	private static final long serialVersionUID = -861537957069177073L;
	
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
			Subfield subField = new Subfield(value, this);
			subFields.add(subField);
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
	public Subfield getSubField(int subFieldIndex) throws Exception {
		
		// If the subfield doesn't exist then add it.
		if (subFieldIndex > subFields.size()) {
			addSubField("", --subFieldIndex);
		}
		
		return subFields.get(--subFieldIndex);
	}
	
	
	/**
	 * Gets a subfield value as a string.  If the subfield does not exist an empty string is returned.
	 * 
	 * @param subFieldIndex
	 * @return
	 */
	public String getSubFieldValue(int subFieldIndex) throws Exception {
		Subfield subField = getSubField(subFieldIndex);
				
		return subField.value();
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
		Subfield subField = getSubField(subFieldIndex);
				
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
		if (index <= getSubFields().size()) {
			throw new HL7Exception("This method adds a subField to the end of the field so the index supplied must not be the index of an existing subField");
		}
		
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
	 * Sets the first subField
	 * 
	 * @param value
	 * @throws Exception
	 */
	public void setValue(String value) throws Exception {
		subFields.clear();
			
		String[] splitSubFieldRepetitions = null;
		
		splitSubFieldRepetitions = value.split("\\^");
			
		for (String fieldRepetitonValue : splitSubFieldRepetitions) {
			Subfield subField = new Subfield(fieldRepetitonValue, this);
			subFields.add(subField);
		}	
		
		this.getField().getSegment().getMessage().refreshSourceHL7Message();
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
		Subfield subField = getSubField(subFieldIndex);
		
		subField.setValue(value);
	}
	
	
	/**
	 * Adds a field at the specified index.  This adds a new sub field, it does not update an existing one.
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
	
	
	/**
	 * Checks to see if the subField is empty.  Either doesn't exist or is blank.
	 * 
	 * @param fieldIndex
	 * @return
	 */
	public boolean isSubFieldEmpty(int subFieldIndex) throws Exception {
		Subfield subField =  getSubField(subFieldIndex);
				
		return subField.isEmpty();
	}
	
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.toString());
	}


	@Override
	public boolean equals(Object obj) {	
		return Objects.equals(this.toString(), obj.toString());
	}
	
	
	/**
	 * Clears all subFields from the supplied startingFieldIndex.
	 * 
	 * @param startingSubFieldIndex
	 * @throws Exception
	 */
	public void clearSubFieldsFrom(int startingSubFieldIndex) throws Exception {
		clearSubFieldRange(startingSubFieldIndex, -1);
	}
	
	
	/**
	 * Clears all subFields from the supplied startingFieldIndex to the endingFieldIndex.
	 * 
	 * @param startingFieldIndex
	 * @param endingFieldIndex
	 * @throws Exception
	 */
	public void clearSubFieldRange(int startingSubFieldIndex, int endingSubFieldIndex) throws Exception {
			
		if (endingSubFieldIndex == -1) {
			endingSubFieldIndex = getSubFields().size();
		} 
		
		for (int i = startingSubFieldIndex; i <= endingSubFieldIndex; i++) {
			Subfield subField = getSubField(i);
			
			if (subField != null) {
				subField.clear();
			}
		}
	}
}
