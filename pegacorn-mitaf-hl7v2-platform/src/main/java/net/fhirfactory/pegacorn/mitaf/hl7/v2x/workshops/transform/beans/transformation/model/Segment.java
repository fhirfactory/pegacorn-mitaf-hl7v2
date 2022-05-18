package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
	public void setFields(List<Field> fields) {
		this.fields = fields;
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
	public Field getField(int fieldIndex) {
		if (fieldIndex >= fields.size()) {
			return null;
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
	public FieldRepetition getFieldRepetition(int fieldIndex, int repetition) {
		Field field = getField(fieldIndex);
		
		if (field == null) {
			return null;
		}
		
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
			
		if (index >= getFields().size()) {
			int sizeDifference = index - getFields().size();
			
			for (int i = 0; i < sizeDifference; i++) {
				getFields().add(new Field("", true, this));
			}
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
	public Field addField(String value, int index) throws Exception {
	    Field field = new Field(value, true, this);
		addField(field,index);
		return field;
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
		
		if (field == null) {
			return;
		}
		
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
		
		if (field == null) {
			return;
		}
		
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
		
		if (field == null) {
			return;
		}
		
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
		
		if (field == null) {
			return;
		}
		
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
	public Subfield getSubField(int fieldIndex, int repetition, int subFieldIndex) {
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
	public Subfield getSubField(int fieldIndex, int subFieldIndex) {
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
	public boolean isFieldEmpty(int fieldIndex) {
		Field field =  getField(fieldIndex);
		
		if (field == null) {
			return true;
		}
		
		return field.isEmpty();
	}

	
	/**
	 * Checks to see if the field is exists.
	 * 
	 * @param fieldIndex
	 * @return
	 */
	public boolean doesFieldExist(int fieldIndex) {
		return getField(fieldIndex) != null;
	}

	
	/**
	 * Checks to see if the sub field is exists in the supplied repetition of the field.
	 * 
	 * @param fieldIndex
	 * @return
	 */
	public boolean doesSubFieldExist(int fieldIndex, int repetition, int subFieldIndex) {
		Field field = getField(fieldIndex);
		
		if (field == null) {
			return false;
		}
		
		return field.doesSubFieldExist(repetition, subFieldIndex);	
	}

	
	/**
	 * Checks to see if the sub field is exists in the 1st repetition of the field.
	 * 
	 * @param fieldIndex
	 * @return
	 */
	public boolean doesSubFieldExist(int fieldIndex, int subFieldIndex) {
		return doesSubFieldExist(fieldIndex, subFieldIndex);
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
		
		this.getMessage().refreshSourceHL7Message();
	}
	
	
	/**
	 * Returns the field value as a string.  If the field does not exist an empty string is returned.
	 * 
	 * @param fieldIndex
	 * @param repetition
	 * @return
	 */
	public String getFieldValue(int fieldIndex, int repetition) {
		Field field = getField(fieldIndex);
		
		if (field == null) {
			return "";
		}
		
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
		
		if (field == null) {
			return;
		}		
		
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
		
		if (field == null) {
			return;
		}		
		
		field.combinedSubFields(0, separator, allowSequentialSeparators);
	}
	
	
	/**
	 * Returns the field value as a string. Repetition 0.  If the field does not exist an empty string is returned.
	 * 
	 * @param fieldIndex
	 * @return
	 */
	public String getFieldValue(int fieldIndex) {
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
}
