package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ca.uhn.hl7v2.HL7Exception;

/**
 * A single HL7 message segment.
 * 
 * @author Brendan Douglas
 *
 */
public class Segment implements Serializable {
	private static final long serialVersionUID = -8797054428191615724L;
	
    private List<Field>fields = new ArrayList<Field>();
	private HL7Message message = null;
	
	public Segment(String segment, HL7Message message) {
		this.message = message;
		
		String[] splitSegmentFields = segment.split("\\|");
				
		fields.add(new Field(splitSegmentFields[0], true, this));
		
		int startIndex = 1;
			
		// For the MSH segment the MSH-1 field value is the separator character (|) and we split based on this so create an empty field then add the seperator character to the field.
		if (segment.startsWith("MSH")) {
			startIndex = 2;
			fields.add(new Field("|", false, this));
			fields.add(new Field(splitSegmentFields[1],false, this));
        }

		
		for (int i = startIndex; i < splitSegmentFields.length; i++) {
			String value = splitSegmentFields[i];
			Field field = new Field(value, true, this);
			fields.add(field);
		}
	}

	
	/**
	 * Gets all the fields for this segment.
	 * 
	 * @return
	 */
	public List<Field> getFields() {
		return fields;
	}

	
	/**
	 * Sets the fields for this segment.
	 * 
	 * @param fields
	 */
	public void setFields(List<Field> fields) throws Exception {
		this.fields = fields;
		
		this.getMessage().refreshSourceHL7Message();
	}

	
	public String toString() {	
	
		// Hack for MSH segment.
		if (this.getName().equals("MSH") ) {
			String segment = fields.stream().skip(3).map(Field::toString).collect(Collectors.joining("|"));
			
			return "MSH|^~\\&|"+segment;
		}
		
		return fields.stream().map(Field::toString).collect(Collectors.joining("|"));
	}

	
	/**
	 * Gets the field at the supplied index.
	 * 
	 * @param fieldIndex
	 * @return
	 */
	public Field getField(int fieldIndex) throws Exception {
		if (fieldIndex >= fields.size()) {
			addField("", fieldIndex);
		}
		
		return fields.get(fieldIndex);
	}

	
	/**
	 * Returns a field repetition.
	 * 
	 * @param fieldIndex
	 * @param repetition
	 * @return
	 */
	public FieldRepetition getFieldRepetition(int fieldIndex, int repetition) throws Exception {
		Field field = getField(fieldIndex);
				
		return field.getRepetition(repetition);
	}

	
	/**
	 * Gets the segment type.
	 * 
	 * @return
	 */
	public String getName() {
		return fields.get(0).toString();
	}
	
	public HL7Message getMessage() {
		return message;
	}
	
	
	/**
	 * Adds a field to this segment.
	 * 
	 * @param field
	 */
	public void addField(Field field) throws Exception {
		this.fields.add(field);
		
		this.getMessage().refreshSourceHL7Message();
	}
	
	
	/**
	 * Adds a field at the specified index.
	 * 
	 * @param field
	 * @param index
	 * @throws Exception
	 */
	public void addField(Field field, int index) throws Exception {
		
		if (index < getFields().size()) {
			throw new HL7Exception("This method adds a field to the end of the segment so the index supplied must not be the index of an existing field");
		}
			
		int sizeDifference = index - getFields().size();
		
		for (int i = 0; i < sizeDifference; i++) {
			getFields().add(new Field("", true, this));
		}
		
		this.getFields().add(index, field);
		
		this.getMessage().refreshSourceHL7Message();
	}
	
	
	/**
	 * Adds a field at the specified index.
	 * 
	 * @param value
	 * @param index
	 * @throws Exception
	 */
	public void addField(String value, int index) throws Exception {
		addField(new Field(value, true, this),index);
	}
	
	
	/**
	 * Clears an entire field including all repetitions..
	 * 
	 * @param fieldIndex
	 * @throws Exception
	 */
	public void clearField(int fieldIndex) throws Exception {		
		if (fieldIndex >= fields.size()) {
			return;
		}
		
		this.getField(fieldIndex).clear();
	}
	
	
	/**
	 * Clears a single repetition of a field..
	 * 
	 * @param fieldIndex
	 * @param repetition
	 * @throws Exception
	 */
	public void clearField(int fieldIndex, int repetition) throws Exception {
		Field field = this.getField(fieldIndex);
			
		field.clear(repetition);
	}
	
	
	/**
	 * Clears all repetitions of a field.
	 * 
	 * @param fieldIndex
	 * @throws Exception
	 */
	public void clearAllFieldRepetitions(int fieldIndex) throws Exception {
		Field field = this.getField(fieldIndex);
				
		field.clearAllRepetitions();
	}

	
	/**
	 * Clears a sub field from the supplied repetition of a field.
	 * 
	 * @param fieldIndex
	 * @param repetition
	 * @throws Exception
	 */
	public void clearSubField(int fieldIndex, int repetition, int subFieldIndex) throws Exception {
		Field field = this.getField(fieldIndex);
				
		field.clearSubField(subFieldIndex, repetition);
	}

	
	/**
	 * Clears a sub field from the 1st repetition of a field.
	 * 
	 * @param fieldIndex
	 * @param repetition
	 * @throws Exception
	 */
	public void clearSubField(int fieldIndex, int subFieldIndex) throws Exception {
		clearSubField(fieldIndex, 0, subFieldIndex);
	}
	
	
	/**
	 * Clears a subField from all repetitions of a field.
	 * 
	 * @param fieldIndex
	 * @param subFieldIndex
	 * @throws Exception
	 */
	public void clearSubFieldFromAllFieldRepetitions(int fieldIndex, int subFieldIndex) throws Exception {
		Field field = this.getField(fieldIndex);
				
		field.clearSubFieldFromAllFieldRepetitions(subFieldIndex);		
	}

	
	/**
	 * Returns the subfield for the supplied field repetition.
	 * 
	 * @param fieldIndex
	 * @param repetition
	 * @param subFieldIndex
	 * @return
	 */
	public Subfield getSubField(int fieldIndex, int repetition, int subFieldIndex) throws Exception {
		FieldRepetition fieldRepetition = getFieldRepetition(subFieldIndex, repetition);
		
		if (fieldRepetition == null) {
			return null;
		}
		
		return fieldRepetition.getSubField(subFieldIndex);
	}

	
	/**
	 * Returns the subfield for the 1st field repetition.
	 * 
	 * @param fieldIndex
	 * @param repetition
	 * @param subFieldIndex
	 * @return
	 */
	public Subfield getSubField(int fieldIndex, int subFieldIndex) throws Exception {
		return getSubField(fieldIndex, 0, subFieldIndex);
	}

	
	/**
	 * Is this segment empty?
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return this.getFields().size() == 1;
	}

	
	/**
	 * Checks to see if the field is empty.  Either doesn't exist or is blank.
	 * 
	 * @param fieldIndex
	 * @return
	 */
	public boolean isFieldEmpty(int fieldIndex) throws Exception {
		Field field =  getField(fieldIndex);
						
		return field.isEmpty();
	}

	
	/**
	 * Changes the name of this segment.
	 * 
	 * @param newName
	 * @throws Exception
	 */
	public void changeName(String newName) throws Exception {
		this.getField(0).setValue(newName);
		
		this.getMessage().refreshSourceHL7Message();
	}
	
	
	/**
	 * Clears the segment
	 */
	public void clear() throws Exception {
		for (Field field : getFields()) {
			field.clear();
		}
	}
	
	
	/**
	 * Returns the field value as a string.  If the field does not exist an empty string is returned.
	 * 
	 * @param fieldIndex
	 * @param repetition
	 * @return
	 */
	public String getFieldValue(int fieldIndex, int repetition) throws Exception {
		Field field = getField(fieldIndex);
				
		FieldRepetition fieldRepetition = field.getRepetition(repetition);
		
		if (fieldRepetition == null) {
			return "";
		}
		
		return fieldRepetition.value();
	}
	
	
	/**
	 * Combines multiple subField values into a single field value.  This methids combines the fields in the supplied repetition.
	 * 
	 * @param fieldIndex
	 * @param fieldRepetition
	 * @param separator
	 * @param allowSequentialSeparators
	 * @throws Exception
	 */
	public void combinedSubFields(int fieldIndex, int fieldRepetition, String separator, boolean allowSequentialSeparators) throws Exception {
		Field field = getField(fieldIndex);
		
		field.combinedSubFields(fieldRepetition, separator, allowSequentialSeparators);
	}
	
	
	/**
	 * Combines multiple subField values into a single field value.  This method combines the fields in the 1st repetition.
	 * 
	 * @param fieldIndex
	 * @param separator
	 * @throws Exception
	 */
	public void combinedSubFields(int fieldIndex, String separator, boolean allowSequentialSeparators) throws Exception {
		Field field = getField(fieldIndex);
		
		field.combinedSubFields(0, separator, allowSequentialSeparators);
	}
	
	
	/**
	 * Returns the field value as a string. Repetition 0.  If the field does not exist an empty string is returned.
	 * 
	 * @param fieldIndex
	 * @return
	 */
	public String getFieldValue(int fieldIndex) throws Exception {
		return getFieldValue(fieldIndex, 0);
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
	 * Clears all fields from this segment starting at the supplied startingFieldIndex.
	 * 
	 * @param startingFieldIndex
	 */
	public void clearFieldsFrom(int startingFieldIndex) throws Exception {
		clearFieldRange(startingFieldIndex, -1);
	}
	
	
	/**
	 * Moves a field value from one field to another
	 * 
	 * @param fromFieldIndex
	 * @param toFieldIndex
	 * @throws Exception
	 */
	public void moveField(int fromFieldIndex, int toFieldIndex) throws Exception {
		Field fromField = this.getField(fromFieldIndex);
		Field toField = this.getField(toFieldIndex);
		
		toField.setValue(fromField.value());
		fromField.clear();
	}
	
	
	/**
	 * Clears all fields from the supplied startingFieldIndex to the endingFieldIndex in this segment.
	 * 
	 * @param startingFieldIndex
	 * @param endingFieldIndex
	 * @throws Exception
	 */
	public void clearFieldRange(int startingFieldIndex, int endingFieldIndex) throws Exception {
		if (endingFieldIndex == -1) {
			endingFieldIndex = getFields().size();
		}			
		
		for (int i = startingFieldIndex; i < endingFieldIndex; i++) {
			Field field = getField(i);
			
			if (field != null) {
				field.clear();
			}
		}
	}

	
	/**
	 * Does any repetition of the field in this segment contain the supplied value.
	 * 
	 * @param fieldIndex
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public boolean doesFieldContainValue(int fieldIndex, String value) throws Exception {
		Field field = getField(fieldIndex);
		
		return field.doesFieldContainValue(value);
	}

	
	public boolean doesSubFieldContainValue(int fieldIndex, int subFieldIndex, String value) throws Exception {
		Field field = getField(fieldIndex);
		
		return field.doesSubFieldContainValue(subFieldIndex, value);
	}
	
	
	/**
	 * Sets a segment value.
	 * 
	 * @param value
	 * @param clearExistingContent - if true the field value becomes the only value in this field.  All other repetitions are cleared.
	 * @throws Exception
	 */
	public void setValue(String value) throws Exception {
		fields.clear();

		String[] splitFields = null;
		
		splitFields = value.split("\\|");
		
		for (String fieldValue : splitFields) {
			Field field = new Field(fieldValue, true, this);
			fields.add(field);
		}
		
		this.getMessage().refreshSourceHL7Message();
	}
}
