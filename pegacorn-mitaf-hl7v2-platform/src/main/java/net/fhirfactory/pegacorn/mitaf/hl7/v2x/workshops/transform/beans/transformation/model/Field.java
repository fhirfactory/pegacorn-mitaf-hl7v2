package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A single field within a segment.
 * 
 * @author Brendan Douglas
 *
 */
public class Field {
	private List<FieldRepetition> repetitions = new ArrayList<>();
	
	private Segment segment = null;
	
	public Field(String field, boolean handleSeperators, Segment segment) {
		this.segment = segment;
		
		String[] splitFieldRepetitions = null;
		
		if (handleSeperators) {
			splitFieldRepetitions = field.split("\\~");
		} else {
			splitFieldRepetitions = new String[1];
			splitFieldRepetitions[0] = field;
		}
		
		for (String value : splitFieldRepetitions) {
			FieldRepetition repetition = new FieldRepetition(value, handleSeperators, this);
			repetitions.add(repetition);
		}		
	}

	
	public List<FieldRepetition> getRepetitions() {
		return repetitions;
	}
	
	
	public void setRepetitions(List<FieldRepetition> repetitions) {
		this.repetitions = repetitions;
	}
	
	
	public String toString() {	
		return repetitions.stream().map(FieldRepetition::toString).collect(Collectors.joining("~"));
	}

	
	/**
	 * Returns a single repetition of this field.
	 * 
	 * @param repetition
	 * @return
	 */
	public FieldRepetition getRepetition(int repetition) {
		if (repetition > repetitions.size()) {
			return null;
		}
		
		return repetitions.get(repetition);
	}

	
	/**
	 * Removes a repetition of the field.
	 * 
	 * @param repetition
	 */
	public void removeRepetition(int repetition) throws Exception {
		if (repetition > repetitions.size()) {
			return;
		}
		
		repetitions.remove(repetition);
		
		this.segment.getMessage().refreshSourceHL7Message();
	}
	
	
	/**
	 * Adds a repetition to this field.
	 * 
	 * @param fieldRepetition
	 */
	public void addRepetition(FieldRepetition fieldRepetition) throws Exception {
		getRepetitions().add(fieldRepetition);
		
		this.segment.getMessage().refreshSourceHL7Message();
	}
	
	
	/**
	 * Adds a repetition to this field.
	 * 
	 * @param value
	 * @throws Exception
	 */
	public void addRepetition(String value) throws Exception {
		FieldRepetition fieldRepetition = new FieldRepetition(value, false, this);
		this.addRepetition(fieldRepetition);
	}
	
	
	/**
	 * Sets the field value.  The value becomes the entire field value.  All existing repetitions are
	 * removed and as a result the value becomes the only field in the first repetition.
	 * 
	 * @param value
	 * @throws Exception
	 */
	public void setValue(String value) throws Exception {
		setValue(value, true);
	}
	
	
	/**
	 * Sets a field value as the 1st subfield in the 1st repetition.  Optionally clears all existing
	 * content before setting the new value.
	 * 
	 * @param value
	 * @param clearExistingContent - if true the field value becomes the only value in this field.  All other repetitions are cleared.
	 * @throws Exception
	 */
	public void setValue(String value, boolean clearExistingContent) throws Exception {
		if (clearExistingContent) {
			getRepetition(0).clear();
		}
		
		getRepetition(0).setValue(value);
	}
	
	
	/**
	 * Sets as value as the first subfield of the supplied repetition.  If the repetition does not
	 * exist then it is created.  If the repetition does exist then the current field content is removed
	 * prior to setting the new value.
	 * 
	 * @param value
	 * @param epetition
	 * @throws Exception
	 */
	public void setValue(String value, int repetition) throws Exception {
		setValue(value, repetition, true);
	}
	
	
	/**
	 * Sets a value as the first subfield of the supplied repetition.  If the repetition does not
	 * exist then it is created.  Optionally clears the existing field content before storing.
	 * 
	 * @param value
	 * @param epetition
	 * @throws Exception
	 */
	public void setValue(String value, int repetition, boolean clearExistingContent) throws Exception {		
		FieldRepetition fieldRepetition = this.getRepetition(repetition);
		
		if (fieldRepetition == null) {
			return;
		}
		
		if (clearExistingContent) {
			this.getRepetition(repetition).clear();
		}
		
		fieldRepetition.setValue(value);
	}

	
	/**
	 * Sets a sub field value for the supplied repetition.
	 * 
	 * @param value
	 * @param subFieldIndex
	 * @param repetition
	 * @throws Exception
	 */
	public void setValue(String value, int subFieldIndex, int repetition) throws Exception {
		FieldRepetition fieldRepetition = this.getRepetition(repetition);
		
		if (fieldRepetition == null) {
			return;
		}
		
		fieldRepetition.setValue(value, subFieldIndex);		
	}

	
	/**
	 * Returns the number of repetitions of this field.
	 * 
	 * @return
	 */
	public int getNumberOfRepetitions() {
		return repetitions.size();
	}
	
	
	public Segment getSegment() {
		return segment;
	}
	
	
	/**
	 * Gets a subfield from this field.
	 * 
	 * @param subFieldIndex
	 * @return
	 */
	public Subfield getSubField(int subFieldIndex) {
		if (subFieldIndex > getRepetitions().get(0).getSubFields().size()) {
			return null;
		}
		
		Subfield subField = getRepetitions().get(0).getSubfield(subFieldIndex);
		
		return subField;
	}
	
	
	public String value() {
		return toString();
	}
	
	
	/**
	 * Clears the entire field, including all repetitions.
	 */
	public void clear() throws Exception {
		for (FieldRepetition repetition : repetitions) {
			repetition.clear();
		}
	}

	/**
	 * Clears a single repetition of the field
	 * 
	 * @param repetition
	 */
	public void clear(int repetition) throws Exception {
		
		FieldRepetition fieldRepetition = getRepetition(repetition);
		
		if (fieldRepetition == null) {
			return;
		}
		
		getRepetition(repetition).clear();
	}
	
	
	/**
	 * Clears a subfield in all repetitions of a field
	 * 
	 * @param subFieldIndex
	 * @throws Exception
	 */
	public void clearSubField(int subFieldIndex) throws Exception {
		for (FieldRepetition repetition : this.getRepetitions()) {
			repetition.clearSubField(subFieldIndex);
		}
	}
	
	
	/**
	 * Clears a subfield in a single repetition of a field.
	 * 
	 * @param subFieldIndex
	 * @param repetition
	 * @throws Exception
	 */
	public void clearSubField(int subFieldIndex, int repetition) throws Exception {
		FieldRepetition fieldRepetition = getRepetition(repetition);
		
		if (fieldRepetition == null) {
			return;
		}
		
		fieldRepetition.clearSubField(subFieldIndex);
	}
	
	
	/**
	 * Adds a subfield at the supplied position.  This inserts a new subfield, it does not replace the value
	 * of an existing subfield.
	 * 
	 * @param value
	 * @param repetition
	 * @param subFieldIndex
	 * @throws Exception
	 */
	public void addSubField(String value, int repetition, int subFieldIndex) throws Exception {
		FieldRepetition fieldRepetition = this.getRepetition(repetition);
		
		if (fieldRepetition == null) {
			return;
		}
		
		fieldRepetition.addSubField(value, subFieldIndex);
	}

	
	/**
	 * Adds a subfield at the supplied position.  This inserts a new subfield, it does not replace the value
	 * of an existing subfield.
	 * 
	 * @param subField
	 * @param repetition
	 * @param subFieldIndex
	 * @throws Exception
	 */
	public void addSubField(Subfield subField, int repetition, int subFieldIndex) throws Exception {
		FieldRepetition fieldRepetition = this.getRepetition(repetition);
		
		if (fieldRepetition == null) {
			return;
		}
		
		fieldRepetition.addSubField(subField, subFieldIndex);
	}
	
	
	/**
	 * Is this field empty?
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		for (FieldRepetition fieldRepetition : repetitions) {
			if (!fieldRepetition.isEmpty()) {
				return false;
			}
		}
		
		return true;
	}
}