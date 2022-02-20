package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.hl7v2.model.Message;

/**
 * An object to store the segments and fields of a HL7 messages.  Provides methods to manipulate the
 * message and reconstruct it.
 * 
 * @author Brendan Douglas
 *
 */
public class HL7Message  {
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
	public void removeSegment(int rowIndex) {
		if (rowIndex >= segments.size()) {
			return;
		}
		
		this.segments.remove(rowIndex);
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
	
		getSegments().set(targetIndex, sourceSegment);
		
		refreshSourceHL7Message();
	}
}
