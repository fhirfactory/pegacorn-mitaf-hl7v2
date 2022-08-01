package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * A single field within a segment.
 * 
 * @author Brendan Douglas
 *
 */
public class Field extends MessageComponent implements Serializable {
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
	
	
	@Override
	public String toString() {		
		String originalValue = repetitions.stream().map(FieldRepetition::toString).collect(Collectors.joining("~"));
		
		if (originalValue.isEmpty()) {
			return originalValue;
		}
		
		try {
			String value = StringUtils.replace(originalValue, "^", "");
			value = StringUtils.replace(value, "~", "");
			value = StringUtils.replace(value, "&", "");
			
			if (value.isEmpty()) {
				clear();
			}
		} catch(Exception e) {
			return "";
		}
		
		return originalValue;
	}

	
	/**
	 * Returns a single repetition of this field.
	 * 
	 * @param repetition
	 * @return
	 */
	public FieldRepetition getRepetition(int repetition) throws Exception {
		if (repetition >= repetitions.size()) {
			return addRepetition("");
		}
		
		return repetitions.get(repetition);
	}
	
	
	/**
	 * Returns the value of a repetition of this field.  If the repetition does not exist the returned value is an empty string.
	 * 
	 * @param repetition
	 * @return
	 */
	public String getRepetitionValue(int repetition) throws Exception{
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
	}
	
	
	/**
	 * Removes a repetition of the field.
	 * 
	 * @param repetition
	 */
	public void removeRepetition(FieldRepetition repetition) throws Exception {		
		repetitions.remove(repetition);
	}
	
	
	/**
	 * Removes a list of repetitions from the field.
	 * 
	 * @param repetition
	 */
	public void removeRepetitions(List<FieldRepetition> repetitionsToRemove) throws Exception {		
		repetitions.removeAll(repetitionsToRemove);
	}
	
	
	/**
	 * Adds a repetition to this field.
	 * 
	 * @param fieldRepetition
	 */
	public void addRepetition(FieldRepetition fieldRepetition) throws Exception {
		
		// if the 1st repetition is empty then replace, otherwise add a new repetition to the end.
		if (!this.value().isEmpty() ) {
			getRepetitions().add(fieldRepetition);
		} else if (!repetitions.isEmpty()) {
			getRepetitions().set(0, fieldRepetition);
		} else {
			getRepetitions().add(fieldRepetition);
		}
	}

	
	/**
	 * Adds a repetition to this field.
	 * 
	 * @param value
	 * @throws Exception
	 */
	public FieldRepetition addRepetition(String value) throws Exception {
		FieldRepetition fieldRepetition = new FieldRepetition(value, false, this);
		this.addRepetition(fieldRepetition);
		
		return fieldRepetition;
	}

	
	/**
	 * Adds an empty repetition.
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public FieldRepetition addEmptyRepetition() throws Exception {
		return addRepetition("");
	}

	
	/**
	 * Sets a field value.
	 * 
	 * @param value
	 * @param clearExistingContent - if true the field value becomes the only value in this field.  All other repetitions are cleared.
	 * @throws Exception
	 */
	@Override
	public void setValue(String value) throws Exception {
		repetitions.clear();

		String[] splitFieldRepetitions = null;
		
		splitFieldRepetitions = value.split("\\~");
		
		for (String fieldValue : splitFieldRepetitions) {
			FieldRepetition repetition = new FieldRepetition(fieldValue, true, this);
			repetitions.add(repetition);
		}
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
	public Subfield getSubField(int subFieldIndex) throws Exception {
		return getSubField(0, subFieldIndex);
	}
	
	
	/**
	 * Gets a subfield value as a string. Repetition 0. If the subfield does not exist then an empty string is returned.
	 * 
	 * @param subFieldIndex
	 * @return
	 */
	public String getSubFieldValue(int subFieldIndex) throws Exception {
		return getSubFieldValue(0, subFieldIndex);
	}
	
	
	/**
	 * Gets a subfield from this field.
	 * 
	 * @param subFieldIndex
	 * @return
	 */
	public Subfield getSubField(int repetition, int subFieldIndex) throws Exception {
	
		// If the subfield does not exist then add it.
		if (subFieldIndex > getRepetitions().get(repetition).getSubFields().size()) {
			addSubField("", subFieldIndex);
			
			return getSubField(subFieldIndex);
		}
		
		return getRepetitions().get(repetition).getSubField(subFieldIndex);
	}
	
	
	/**
	 * Gets a subfield value as a string.  If the subfield does not exist then an empty string is returned.
	 * 
	 * @param subFieldIndex
	 * @return
	 */
	public String getSubFieldValue(int repetition, int subFieldIndex) throws Exception {
		if (subFieldIndex > getRepetitions().get(repetition).getSubFields().size()) {
			return "";
		}
		
		Subfield subField = getRepetitions().get(repetition).getSubField(subFieldIndex);
				
		return subField.value();			
	}
	
	
	@Override
	public String value() {
		return toString();
	}
	
	
	/**
	 * Clears the entire field, including all repetitions.
	 */
	@Override
	public void clear() throws Exception {
		setValue("");
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
	 * of an existing subfield.  1st repetition only.
	 * 
	 * @param value
	 * @param subFieldIndex
	 * @throws Exception
	 */
	public void addSubField(String value, int subFieldIndex) throws Exception {
		addSubField(value, 0, subFieldIndex);
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
	 * Adds a subfield at the supplied position.  This inserts a new subfield, it does not replace the value
	 * of an existing subfield.  1st repetition only.
	 * 
	 * @param subField
	 * @param repetition
	 * @param subFieldIndex
	 * @throws Exception
	 */	
	public void addSubField(Subfield subField, int subFieldIndex) throws Exception {
		addSubField(subField, subFieldIndex);
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
	public boolean isSubFieldEmpty(int subFieldIndex) throws Exception {
		return isSubFieldEmpty(0, subFieldIndex);
	}

	
	/**
	 * Checks to see if the sub field at the specified repetition is empty.  Either doesn't exist or is blank.
	 * 
	 * @param fieldIndex
	 * @return
	 */
	public boolean isSubFieldEmpty(int repetition, int subFieldIndex) throws Exception {
		FieldRepetition fieldRepetition = getRepetition(repetition);
		
		if (fieldRepetition == null) {
			return true;
		}
		
		Subfield subField = fieldRepetition.getSubField(subFieldIndex);
				
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
	 * Combine all subField values into a single field with the supplied separator between the subfields.
	 */
	public void combineSubFields(int fieldRepetition, String separator, boolean allowSequentialSeparators) throws Exception {
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
	public void combineSubFields(String separator, boolean allowSequentialSeparators) throws Exception {
		combineSubFields(0, separator, allowSequentialSeparators);
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
	
	
	/**
	 * Clears all subFields from the startingSubFieldIndex in the supplied field repetition.
	 * 
	 * @param fieldRepetition
	 * @param startingSubFieldIndex
	 * @throws Exception
	 */
	public void clearSubFieldsStartingFrom(int fieldRepetition, int startingSubFieldIndex) throws Exception {
		clearSubFieldRange(fieldRepetition, startingSubFieldIndex, -1);
	}

	
	/**
	 * Clears all subFields from the startingSubFieldIndex in all field repetitions.
	 * 
	 * @param startingSubFieldIndex
	 * @throws Exception
	 */
	public void clearSubFieldsStartingFrom(int startingSubFieldIndex) throws Exception {
		for (FieldRepetition repetition : this.repetitions) {
			repetition.clearSubFieldsFrom(startingSubFieldIndex);
		}
	}

	
	/**
	 * Clears all subFields from the supplied startingFieldIndex to the endingFieldIndex in the supplied field repetition.
	 * 
	 * @param startingFieldIndex
	 * @param endingFieldIndex
	 * @throws Exception
	 */
	public void clearSubFieldRange(int fieldRepetition, int startingSubFieldIndex, int endingSubFieldIndex) throws Exception {
		FieldRepetition repetition = this.getRepetition(fieldRepetition);
		
		if (repetition == null) {
			return;
		}
		
		repetition.clearSubFieldRange(startingSubFieldIndex, endingSubFieldIndex);
	}

	
	/**
	 * Clears all subFields from the supplied startingFieldIndex to the endingFieldIndex in all field repetitions.
	 * 
	 * @param startingSubFieldIndex
	 * @param endingSubFieldIndex
	 * @throws Exception
	 */
	public void clearSubFieldRange(int startingSubFieldIndex, int endingSubFieldIndex) throws Exception {
		for (FieldRepetition fieldRepetition : this.repetitions) {
			fieldRepetition.clearSubFieldRange(startingSubFieldIndex, endingSubFieldIndex);
		}
	}

	
	/**
	 * 
	 * 
	 * @param value
	 * @return
	 */
	public boolean hasFieldMatchingValue(String value) {
		return hasFieldMatchingValue("equals", value);
	}
	
	
	/**
	 * 
	 * 
	 * @param value
	 * @return
	 */
	public boolean hasFieldMatchingValue(String matchType, String ... matchValues) {
		for (FieldRepetition fieldRepetition : this.repetitions) {
			if (compare(matchType, fieldRepetition.value(), matchValues)) {
				return true;
			}
		}
		
		return false;
	}

	
	/**
	 * 
	 * 
	 * @param subFieldIndex
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public boolean hasSubFieldMatchingValue(String matchType, int subFieldIndex, String ... matchValues) throws Exception {
		for (FieldRepetition fieldRepetition : this.repetitions) {
			if (compare(matchType, fieldRepetition.getSubField(subFieldIndex).value(), matchValues)) {
				return true;
			}
		}
		
		return false;		
	}
	
	
	/**
	 * 
	 * 
	 * @param subFieldIndex
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public boolean hasSubFieldMatchingValue(int subFieldIndex, String value) throws Exception {
		return hasSubFieldMatchingValue("equals", subFieldIndex, value);
	}
	
	
	/**
	 * Removes field repetitions where the matchValue matches the subField value. 
	 * 
	 * @param subFieldIndex
	 * @param matchValue
	 */
	public void removeMatchingFieldRepetitions(int subFieldIndex, String ... matchValues) throws Exception {
		removeMatchingFieldRepetitions("equals", subFieldIndex, matchValues);
	}
	
	
	/**
	 * Removes field repetitions where the matchValue matches the subField value. 
	 * 
	 * @param subFieldIndex
	 * @param matchValue
	 */
	public void removeMatchingFieldRepetitions(String matchType, int subFieldIndex, String ... matchValues) throws Exception {
		List<FieldRepetition> fieldRepetitions = this.getRepetitionsMatchingValue(matchType, subFieldIndex, matchValues);
		this.repetitions.removeAll(fieldRepetitions);
	}

	
	/**
	 * Removes field repetitions where the matchValue does not match the subField value.
	 * 
	 * @param subFieldIndex
	 * @param matchValue
	 * @throws Exception
	 */
	public void removeNotMatchingFieldRepetitions(int subFieldIndex, String ... matchValues) throws Exception {	
		removeNotMatchingFieldRepetitions("equals", subFieldIndex, matchValues);
	}
	

	/**
	 * Removes field repetitions where the matchValue does not match the subField value.
	 * 
	 * @param subFieldIndex
	 * @param matchValue
	 * @throws Exception
	 */
	public void removeNotMatchingFieldRepetitions(String matchType, int subFieldIndex, String ... matchValues) throws Exception {	
		List<FieldRepetition>fieldRepetitions = this.getRepetitionsNotMatchingValue(matchType, subFieldIndex, matchValues);
		this.repetitions.removeAll(fieldRepetitions);
	}


	/**
	 * Sets a subField value in all repetitions.
	 * 
	 * @param subFieldIndex
	 * @param value
	 */
	public void setSubField(int subFieldIndex, String value) throws Exception {

		for (FieldRepetition repetition : getRepetitions()) {
			Subfield subField = repetition.getSubField(subFieldIndex);
			
			if (subField != null) {
				subField.setValue(value);
			}
		}
	}


	/**
	 * Returns a repetition of this field Matching the supplied value at the supplied sub field index.  The first match is returned.
	 * 
	 * @param subFieldIndex
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public FieldRepetition getRepetitionMatchingValue(String matchType, int subFieldIndex, String ... matchValues) throws Exception {
		for (FieldRepetition repetition : getRepetitions()) {
			Subfield subField = repetition.getSubField(subFieldIndex);
			
			if (compare(matchType, subField.value(), matchValues)) {
				return repetition;
			}
		}
		
		return null;
	}
	
	
	/**
	 * Returns a repetition of this field Matching the supplied value at the supplied sub field index.  The first match is returned.
	 * 
	 * @param subFieldIndex
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public FieldRepetition getRepetitionMatchingValue(int subFieldIndex, String ...matchValues) throws Exception {
		return getRepetitionMatchingValue("equals", subFieldIndex, matchValues);
	}
	
	
	/**
	 * Returns a list of repetitions of this field Matching the supplied value at the supplied sub field index.
	 * 
	 * @param subFieldIndex
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public List<FieldRepetition> getRepetitionsMatchingValue(String matchType, int subFieldIndex, String ... matchValues) throws Exception {
		List<FieldRepetition>fieldRepetitions = new ArrayList<>();
		
		for (FieldRepetition repetition : getRepetitions()) {
			Subfield subField = repetition.getSubField(subFieldIndex);
			
			if (compare(matchType, subField.value(), matchValues)) {
				fieldRepetitions.add(repetition);
			}
		}
		
		return fieldRepetitions;
	}
	
	
	/**
	 * Returns a list of repetitions of this field Matching the supplied value at the supplied sub field index.
	 * 
	 * @param subFieldIndex
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public List<FieldRepetition> getRepetitionsMatchingValue(int subFieldIndex, String ... matchValues) throws Exception {
		return getRepetitionsMatchingValue("equals", subFieldIndex, matchValues);
	}

	
	/**
	 * Returns a list of Field repetitions of this field not Matching one of the supplied value at the supplied sub field index.
	 * 
	 * @param subFieldIndex
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public List<FieldRepetition> getRepetitionsNotMatchingValue(String matchType, int subFieldIndex, String ... matchValues) throws Exception {
		List<FieldRepetition>fieldRepetitions = new ArrayList<>();
		
	
		for (FieldRepetition repetition : getRepetitions()) {
			Subfield subField = repetition.getSubField(subFieldIndex);

			if (!compare(matchType, subField.value(), matchValues)) {
				fieldRepetitions.add(repetition);
			}
		}
		
		return fieldRepetitions;
	}
	
	
	/**
	 * Returns a list of Field repetitions of this field not Matching one of the supplied value at the supplied sub field index.
	 * 
	 * @param subFieldIndex
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public List<FieldRepetition> getRepetitionsNotMatchingValue(int subFieldIndex, String ... matchValues) throws Exception {
		return getRepetitionsNotMatchingValue("equals", subFieldIndex, matchValues);
	}
	
	
	/**
	 * Does one of the supplied match values match the text using the supplied match type.
	 * 
	 * @param matchType
	 * @param text
	 * @param compareValue
	 * @return
	 */
	private boolean compare(String matchType, String text, String ... matchValues) {
		MatchTypeEnum type = MatchTypeEnum.get(matchType);
		
		
		for (String matchValue : matchValues) {
		
			switch (type) {
				case EQUALS:
					if  (text.equals(matchValue)) {
						return true;
					}
					break;
				case CONTAINS:
					if (text.contains(matchValue)) {
						return true;
					}
				case ENDS_WITH:
					if (text.endsWith(matchValue)) {
						return true;
					}
					break;
				case STARTS_WITH:
					if (text.startsWith(matchValue)) {
						return true;
					}
					break;
				default:
					if (text.equals(matchValue)) {
						return true;
					}
			}
		}
		
		return false;
	}
}
