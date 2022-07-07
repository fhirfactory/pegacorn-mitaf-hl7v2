package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;

import ca.uhn.hl7v2.model.Message;

/**
 * An object to store the segments and fields of a HL7 messages.  Provides methods to manipulate the
 * message and reconstruct it.
 * 
 * @author Brendan Douglas
 *
 */
public class HL7Message implements Serializable   {
	private static final long serialVersionUID = -2986106720527032903L;

	private List<Segment>segments = new ArrayList<Segment>();
	
	private Message sourceHL7Message = null;
	
	public HL7Message(Message sourceHL7Message) {
		this.sourceHL7Message = sourceHL7Message;

		String[] splitMessageSegments = sourceHL7Message.toString().split("\r");
		
		for (String value : splitMessageSegments) {
			Segment segment = new Segment(value, this);
			segments.add(segment);
		}
	}

	
	/**
	 * Gets all segments.
	 * 
	 * @return
	 */
	public List<Segment> getSegments() {
		return segments;
	}

	
	/**
	 * Set all segments.
	 * 
	 * @param segments
	 */
	public void setSegments(List<Segment> segments) {
		this.segments = segments;
	}

	
	@Override
	public String toString() {	
		return segments.stream().map(Segment::toString).collect(Collectors.joining("\r"));	
	}
	
	
	/**
	 * Removes an occurrence of a segment.
	 * 
	 * @param name
	 * @param occurrence
	 */
	public void removeSegment(String name, int occurrence) throws Exception {
		
		int currentOccurrence = 0;
		
		for (Iterator<Segment> iter = getSegments().iterator(); iter.hasNext(); ) {
			Segment segment = iter.next();
			
			if (segment.getName().equals(name)) {
				if (currentOccurrence == occurrence) {
					iter.remove();
					refreshSourceHL7Message();
					return;
				}
				
				currentOccurrence++;
			}
		}	
	}
	
	
	/**
	 * Removes the segment at the supplied rowIndex.
	 * 
	 * @param rowIndex
	 */
	public void removeSegment(int rowIndex) throws Exception {
		if (rowIndex >= segments.size()) {
			return;
		}
		
		this.segments.remove(rowIndex);
		
		refreshSourceHL7Message();
	}
	
	
	/**
	 * Removes the supplied segment from the list of segments.
	 * 
	 * @param segment
	 * @throws Exception
	 */
	public void removeSegment(Segment segment) throws Exception {	
		this.segments.remove(segment);
		
		refreshSourceHL7Message();
	}
	
	
	/**
	 * Removes all segments of the supplied name.
	 * 
	 * @param name
	 */
	public void removeAllMatchingSegments(String name) throws Exception {
		for (Iterator<Segment> iter = getSegments().iterator(); iter.hasNext(); ) {
			Segment segment = iter.next();
			
			if (segment.getName().equals(name)) {
				iter.remove();
			}
		}
		
		refreshSourceHL7Message();
	}
	
	
	/**
	 * Gets the segment at the supplied index.
	 * 
	 * @param segmentIndex
	 * @return
	 */
	public Segment getSegment(int rowIndex) {
		if (rowIndex >= segments.size()) {
			return null;
		}
		
		return segments.get(rowIndex);
	}


	/**
	 * Refreshes the source HL7 message with all the changes applied,
	 * 
	 * @return
	 */
	public void refreshSourceHL7Message() throws Exception {
		sourceHL7Message.parse(this.toString());
	}

	
	/**
	 * Returns a count of the number of segments matching the supplied segment name.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 */
	public int getSegmentCount(String segmentName) throws Exception {
		int segmentCount = 0;
		
		for (Segment segment : getSegments()) {
			if (segment.getName().equals(segmentName)) {
				segmentCount++;
			}
		}
		
		return segmentCount;
	}
	
	
	/**
	 * Returns the index of a matching segment.
	 * 
	 * @param message
	 * @param segmentName
	 * @param occurence
	 * @return
	 * @throws Exception
	 */
	public Integer getSegmentIndex(String segmentName, int occurrence) throws Exception {
		List<Integer>segmentIndexes = getSegmentIndexes(segmentName);
		
		if (segmentIndexes.isEmpty()) {
			return null;
		}
		
		if (occurrence > segmentIndexes.size()) {
			return null;
		}
		
		return segmentIndexes.get(occurrence);
	}
	
	
	/**
	 * Returns the row indexes of segments which match the supplied name.
	 * 
	 * @param segmentName
	 * @return
	 * @throws Exception
	 */
	public List<Integer> getSegmentIndexes(String segmentName) throws Exception {
		List<Integer> segmentIndexes = new ArrayList<>();
		
		for (int i = 0; i < getSegments().size(); i++) {
			Segment segment = getSegment(i);
			
			if (segment.getName().equals(segmentName)) {
				segmentIndexes.add(i);
			}			
		}

		return segmentIndexes;
	}
	
	
	/**
	 * Returns the first matching segment.
	 * 
	 * @param segmentName
	 * @return
	 * @throws Exception
	 */
	public Integer getFirstSegmentIndex(String segmentName) throws Exception {
		return getSegmentIndex(segmentName, 0);
	}
	
	
	/**
	 * Copies a segment.
	 * 
	 * @param message
	 * @param sourceIndex
	 * @param targetIndex
	 * @throws Exception
	 */
	public void copySegment(int sourceIndex, int targetIndex) throws Exception {
		if (sourceIndex >= getSegments().size()) {
			return;
		}
		
		Segment sourceSegment = getSegments().get(sourceIndex);
	
		getSegments().set(targetIndex, SerializationUtils.clone(sourceSegment));
		
		refreshSourceHL7Message();
	}
	
	
	/**
	 * Inserts a segment at the target index which has identical content to the source segment.
	 * 
	 * @param message
	 * @param sourceIndex
	 * @param targetIndex
	 * @throws Exception
	 */
	public Segment insertSegment(Segment source, int targetIndex) throws Exception {
		Segment segment = new Segment(source.toString(), this);
		
		getSegments().add(targetIndex, segment);
		refreshSourceHL7Message();
		
		return segment;
	}
	
	
	/**
	 * Moves a segment from one location to another.  If the newIndex is not in range then the segment is appended to the end.
	 * 
	 * @param message
	 * @param currentIndex
	 * @param newIndex
	 * @throws Exception
	 */
	public void moveSegment(int currentIndex, int newIndex) throws Exception {	
		if (currentIndex >= getSegments().size()) {
			return;
		}
		
		boolean append = false;
		
		if (newIndex >= getSegments().size()) {
			append = true;
		}
		
		Segment sourceSegment = getSegments().get(currentIndex);
		
		if (append) {
			getSegments().add(SerializationUtils.clone(sourceSegment));
		} else {
			getSegments().add(newIndex, SerializationUtils.clone(sourceSegment));
		}
			
		getSegments().remove(sourceSegment);
		
		refreshSourceHL7Message();
	}
	
	
	/**
	 * Insert a segment.
	 * 
	 * @param message
	 * @param newSegmentName
	 * @param segmentIndex
	 * @param id
	 * @throws Exception
	 */
	public Segment insertSegment(String newSegmentName, int segmentIndex, int id) throws Exception {	
		Segment segment = new Segment(newSegmentName + "|" + id, this);
		
		getSegments().add(segmentIndex, segment);
		refreshSourceHL7Message();
		
		return segment;
	}
	
	
	/**
	 * Appends a segment to the end of the message.
	 * 
	 * @param newSegmentName
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Segment appendSegment(String newSegmentName, int id) throws Exception {	
		Segment segment = new Segment(newSegmentName + "|" + id, this);
		
		getSegments().add(segment);
		refreshSourceHL7Message();
		
		return segment;
	}	

	
	/**
	 * Returns the {@link HL7Message} so the messages can be used directly instead of going through this utility class.
	 * 
	 * @param message
	 * @return
	 */
	public HL7Message getHL7Message(Message message) {
		return new HL7Message(message);
	}
	
	
	/**
	 * Returns all matching segments.
	 * 
	 * @param segmentName
	 * @return
	 * @throws Exception
	 */
	public List<Segment> getSegments(String segmentName) throws Exception {
		List<Segment>segments = new ArrayList<>();		
		
		for (Segment segment : getSegments()) {
			if (segment.getName().equals(segmentName)) {
				segments.add(segment);
			}
		}
		
		return segments;	
	}
	
	
	/**
	 * Returns the row index of the supplied segment.
	 * 
	 * @param message
	 * @param segment
	 * @return
	 * @throws Exception
	 */
	public Integer getSegmentIndex(Segment segment) throws Exception {

		for (int i = 0; i < getSegments().size(); i++) {
			if (getSegment(i).equals(segment)) {
				return i;
			}
		}
		
		return null;
	}

	
	/**
	 * Clears the same field from each occurrences of a segment.
	 * 
	 * @param segment
	 * @param fieldIndex
	 */
	public void clearFieldFromAllSegments(String segmentName, int fieldIndex) throws Exception {
		setFieldInAllSegments(segmentName, fieldIndex, "");
	}

	
	/**
	 * Sets a field to the same value in all occurrences of a segment.
	 * 
	 * @param segment
	 * @param fieldIndex
	 */
	public void setFieldInAllSegments(String segmentName, int fieldIndex, String value) throws Exception {
		for (Segment segment : getSegments(segmentName)) {
			Field field = segment.getField(fieldIndex);
			
			if (field != null) {
				for (FieldRepetition repetition : field.getRepetitions()) {
					repetition.setValue(value);
				}
			}
		}
	}

	
	/**
	 * Sets a sub field to the same value in all occurrences of a segment.
	 * 
	 * @param segment
	 * @param fieldIndex
	 * @param subFieldIndex
	 */
	public void setSubFieldInAllSegments(String segmentName, int fieldIndex, int subFieldIndex, String value) throws Exception {
		for (Segment segment : getSegments(segmentName)) {
			Field field = segment.getField(fieldIndex);
			
			if (field != null) {
				for (FieldRepetition repetition : field.getRepetitions()) {
					Subfield subField = repetition.getSubField(subFieldIndex);
					
					if (subField != null) {
						subField.setValue(value);
					}
				}
			}
		}
	}

	
	/**
	 * Clears all fields from a segment starting at the supplied startingFieldIndex in all matching segments.
	 * 
	 * @param segment
	 * @param startingFieldIndex
	 */
	public void clearFieldsFrom(String segmentName, int startingFieldIndex) throws Exception {
		clearFieldRange(segmentName, startingFieldIndex, -1);
	}

	
	/**
	 * Clears all fields from the supplied startingFieldIndex to the endingFieldIndex in all matching segments.
	 * 
	 * @param segment
	 * @param startingFieldIndex
	 * @param endingFieldIndex
	 */
	public void clearFieldRange(String segmentName, int startingFieldIndex, int endingFieldIndex) throws Exception {

		for (Segment segment : getSegments(segmentName)) {
			segment.clearFieldRange(startingFieldIndex, endingFieldIndex);
		}
	}

	
	/**
	 * Returns the number of segment groups for the supplied segment.
	 * 
	 * @param segmentName
	 * @return
	 */
	public Integer getNumberOfSegmentGroups(String segmentName) throws Exception {
		return getStartIndexesOfSegmentGroups(segmentName).size();
	}

	
	/**
	 * Returns the start index of the segment group.
	 * 
	 * @param segmentName
	 * @param groupNumber
	 * @return
	 */
	public Integer getStartIndexOfSegmentGroup(String segmentName, int groupNumber) throws Exception {
		List<Integer>groupStartIndexes = getStartIndexesOfSegmentGroups(segmentName);
		
		if (groupStartIndexes.size() >= groupNumber) {
			return groupStartIndexes.get(groupNumber);
		}
		
		return null;
	}

	
	/**
	 * Returns the end index of the segment group.
	 * 
	 * @param segmentName
	 * @param groupNumber
	 * @return
	 */
	public Integer getEndIndexOfSegmentGroup(String segmentName, int groupNumber) throws Exception {
		List<Integer>groupStartIndexes = getStartIndexesOfSegmentGroups(segmentName);
		
		if (groupStartIndexes.size() >= groupNumber) {
			int startIndex = groupStartIndexes.get(groupNumber);
			
			for (int i = startIndex; i < this.getSegments().size(); i++) {
				Segment segment = this.getSegment(i);
				
				if (!segment.getName().equalsIgnoreCase(segmentName)) {
					return --i;  // The group ended at the index before this one.
				}
			}
		}
		
		return this.segments.size() - 1; // The segment ended with the last element in the list.		
	}

	
	/**
	 * Returns all the segments within a group
	 * 
	 * @param segmentName
	 * @param groupNumber
	 * @return
	 */
	public List<Segment> getSegmentsWithinGroup(String segmentName, int groupNumber) throws Exception {
		List<Segment>segmentsWithingroup = new ArrayList<>();
		
		
		Integer startIndex = getStartIndexOfSegmentGroup(segmentName, groupNumber);
		
		if (startIndex == null) {
			return segmentsWithingroup;
		}

		Integer endIndex = getEndIndexOfSegmentGroup(segmentName, groupNumber);
		
		if (endIndex == null) {
			return segmentsWithingroup;
		}		

		for (int i = startIndex; i <= endIndex; i++) {
			Segment segment = this.getSegment(i);
			segmentsWithingroup.add(segment);
		}

		return segmentsWithingroup;
	}

	
	/**
	 * Returns an array of indexes which are the start indexes of each segment group.
	 * 
	 * @param segmentName
	 * @return
	 */
	public List<Integer> getStartIndexesOfSegmentGroups(String segmentName) throws Exception {	
		List<Integer> startGroupIndexes = new ArrayList<>();
	
		boolean groupFound = false;
		
		for (int i = 0; i < this.getSegments().size(); i++) {
			Segment segment = this.getSegment(i);
						
			if (segmentName.equals(segment.getName())) {
				if (!groupFound) {
					groupFound = true;
					startGroupIndexes.add(i);
				}
			} else {
				groupFound = false;
			}
		}
	
		return startGroupIndexes;
	}
	
	
	/**
	 * Appends a new segment to a group.
	 * 
	 * @param segmentName
	 * @param groupNumber
	 * @return
	 * @throws Exception
	 */
	public Segment appendSegmentToGroup(String segmentName, int groupNumber) throws Exception {
		if (groupNumber >= getNumberOfSegmentGroups(segmentName)) {
			return null; // Just ignore
		}
		
		int endIndexOfgroup = getEndIndexOfSegmentGroup(segmentName, groupNumber);
		
		Segment endGroupSegment = getSegment(endIndexOfgroup);
		int currentId = Integer.valueOf(endGroupSegment.getField(1).value()).intValue();
		
		return insertSegment(segmentName, ++endIndexOfgroup, ++currentId);
	}
	
	
	/**
	 * Does the supplied value appear in the specified field of any matching segment.  All field repetitions are checked.
	 * 
	 * @param segmentName
	 * @param fieldIndex
	 * @param value
	 * @return
	 */
	public boolean doesFieldContainValue(String segmentName, int fieldIndex, String value) throws Exception {
		for (Segment segment : this.getSegments(segmentName)) {
			if (segment.doesFieldContainValue(fieldIndex, value)) {
				return true;
			}
		}
		
		return false;
	}

	
	/**
	 * Does the supplied value appear in the specified subField of any matching segment.  All field repetitions are checked.
	 * 
	 * @param segmentName
	 * @param fieldIndex
	 * @param subFieldIndex
	 * @param value
	 * @return
	 */
	public boolean doesSubFieldContainValue(String segmentName, int fieldIndex, int subFieldIndex, String value) throws Exception {
		for (Segment segment : this.getSegments(segmentName)) {
			if (segment.doesSubFieldContainValue(fieldIndex, subFieldIndex, value)) {
				return true;
			}
		}
		
		return false;
	}

	
	/**
	 * Removes a field repetition where the matchValue matches the subField value.
	 * 
	 * @param fieldIndex
	 * @param subFieldIndex
	 * @param matchValue
	 * @return
	 */
	public void removeMatchingFieldRepetitions(String segmentName, int fieldIndex, int subFieldIndex, String matchValue) throws Exception {
		for (Segment segment : this.getSegments(segmentName)) {
			segment.removeMatchingFieldRepetitions(fieldIndex, subFieldIndex, matchValue);
		}
	}

	
	/**
	 * Removes a field repetition where the matchValue does not match the subField value.
	 * 
	 * @param fieldIndex
	 * @param subFieldIndex
	 * @param matchValue
	 * @return
	 */
	public void removeNotMatchingFieldRepetitions(String segmentName, int fieldIndex, int subFieldIndex, String matchValue) throws Exception {
		for (Segment segment : this.getSegments(segmentName)) {
			segment.removeNotMatchingFieldRepetitions(fieldIndex, subFieldIndex, matchValue);
		}
	}
}
