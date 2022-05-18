package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A single field within a segment.
 * 
 * @author Brendan Douglas
 *
 */
public class Field implements Serializable {
	private static final long serialVersionUID = 3815655672813186758L;

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
	 * Returns the value of a repetition of this field.  If the repetition does not exist the returned value is an empty string.
	 * 
	 * @param repetition
	 * @return
	 */
	public String getRepetitionValue(int repetition) {
		FieldRepetition fieldRepetition = getRepetition(repetition);
		
		if (fieldRepetition == null) {
			return "";
		}
		
		return fieldRepetition.value();
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
	 * Sets a field value.
	 * 
	 * @param value
	 * @param clearExistingContent - if true the field value becomes the only value in this field.  All other repetitions are cleared.
	 * @throws Exception
	 */
	public void setValue(String value) throws Exception {
		repetitions.clear();

		String[] splitFieldRepetitions = null;
		
		splitFieldRepetitions = value.split("\\~");
		
		for (String fieldValue : splitFieldRepetitions) {
			FieldRepetition repetition = new FieldRepetition(fieldValue, true, this);
			repetitions.add(repetition);
		}
		
		this.getSegment().getMessage().refreshSourceHL7Message();
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
	 * Gets a subfield from this field.  Repetition 0.
	 * 
	 * @param subFieldIndex
	 * @return
	 */
	public Subfield getSubField(int subFieldIndex) {
		return getSubField(0, subFieldIndex);
	}
	
	
	/**
	 * Gets a subfield value as a string. Repetition 0. If the subfield does not exist then an empty string is returned.
	 * 
	 * @param subFieldIndex
	 * @return
	 */
	public String getSubFieldValue(int subFieldIndex) {
		return getSubFieldValue(0, subFieldIndex);
	}
	
	
	/**
	 * Gets a subfield from this field.
	 * 
	 * @param subFieldIndex
	 * @return
	 */
	public Subfield getSubField(int repetition, int subFieldIndex) {
		if (subFieldIndex > getRepetitions().get(repetition).getSubFields().size()) {
			return null;
		}
		
		return getRepetitions().get(repetition).getSubField(subFieldIndex);
	}
	
	
	/**
	 * Gets a subfield value as a string.  If the subfield does not exist then an empty string is returned.
	 * 
	 * @param subFieldIndex
	 * @return
	 */
	public String getSubFieldValue(int repetition, int subFieldIndex) {
		if (subFieldIndex > getRepetitions().get(repetition).getSubFields().size()) {
			return "";
		}
		
		Subfield subField = getRepetitions().get(repetition).getSubField(subFieldIndex);
		
		if (subField == null) {
			return "";
		}
		
		return subField.value();			
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
	public void clearSubFieldFromAllFieldRepetitions(int subFieldIndex) throws Exception {
		for (FieldRepetition repetition : this.getRepetitions()) {
			repetition.clearSubField(subFieldIndex);
		}
	}

	
	/**
	 * Clears all repetitions of a field.
	 * 
	 * @param fieldIndex
	 * @throws Exception
	 */
	public void clearAllRepetitions() throws Exception {
		for (FieldRepetition repetition : this.getRepetitions()) {
			repetition.clear();
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
	 * Clears a subfield in the 1st field repetition.
	 * 
	 * @param subFieldIndex
	 * @param repetition
	 * @throws Exception
	 */
	public void clearSubField(int subFieldIndex) throws Exception {
		clearSubField(subFieldIndex, 0);
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
	
	
	/**
	 * Checks to see if the sub field is empty.  Either doesn't exist or is blank.
	 * 
	 * @param fieldIndex
	 * @return
	 */
	public boolean isSubFieldEmpty(int subFieldIndex) {
		return isSubFieldEmpty(0, subFieldIndex);
	}
	
	
	/**
	 * Checks to see if the sub field is exists.
	 * 
	 * @param fieldIndex
	 * @return
	 */
	public boolean doesSubFieldExist(int subFieldIndex) {
		return doesSubFieldExist(0, subFieldIndex);
	}
	
	
	/**
	 * Checks to see if the sub field at the specified repetition is empty.  Either doesn't exist or is blank.
	 * 
	 * @param fieldIndex
	 * @return
	 */
	public boolean isSubFieldEmpty(int repetition, int subFieldIndex) {
		FieldRepetition fieldRepetition = getRepetition(repetition);
		
		if (fieldRepetition == null) {
			return true;
		}
		
		Subfield subField = fieldRepetition.getSubField(subFieldIndex);
		
		if (subField == null) {
			return true;
		}
		
		return subField.isEmpty();
	}
	
	
	/**
	 * Checks to see if the sub field at the specified repetition exists.
	 * 
	 * @param fieldIndex
	 * @return
	 */
	public boolean doesSubFieldExist(int repetition, int subFieldIndex) {
		FieldRepetition fieldRepetition = getRepetition(repetition);
		
		if (fieldRepetition == null) {
			return true;
		}
		
		return fieldRepetition.getSubField(subFieldIndex) != null;
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
	 * Combine all subField values into a single field with the supplied separator between the subfields.
	 */
	public void combinedSubFields(int fieldRepetition, String separator, boolean allowSequentialSeparators) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		for (Subfield subField : getSubFields(fieldRepetition)) {
		
			if (sb.length() > 0) {
				if (allowSequentialSeparators) {
					sb.append(separator);
				} else {
					if (!sb.toString().endsWith(separator)) {
						sb.append(separator);
					}
				}
			}
			
			sb.append(subField.value());
		}
		
		this.setValue(sb.toString());
	}
	
	
	/**
	 * Combine all subField values into a single field with the supplied separator between the subfields.
	 */
	public void combinedSubFields(String separator, boolean allowSequentialSeparators) throws Exception {
		combinedSubFields(0, separator, allowSequentialSeparators);
	}
	
	
	/**
	 * returns a list of the subFields for a field repetition.
	 * 
	 * @param fieldRepetition
	 * @return
	 * @throws Exception
	 */
	public List<Subfield>getSubFields(int fieldRepetition) throws Exception {
		if (this.getRepetition(fieldRepetition) == null) {
			return new ArrayList<Subfield>();
		}
		
		return this.getRepetition(fieldRepetition).getSubFields();
	}
	
	
	/**
	 * returns a list of the subFields for the 1st field repetition.
	 * 
	 * @param fieldRepetition
	 * @return
	 * @throws Exception
	 */
	public List<Subfield>getSubFields() throws Exception {
		return getSubFields(0);
	}
}
