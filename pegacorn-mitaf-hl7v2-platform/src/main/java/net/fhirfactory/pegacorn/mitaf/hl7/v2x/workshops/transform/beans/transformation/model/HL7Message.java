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
}
