package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.component.hl7.HL7;
import org.apache.commons.lang3.SerializationUtils;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * Utilities that parse/query a HL7 document as a string and do not uses a HL7
 * library or testers.
 * 
 * @author Brendan Douglas
 *
 */
class HL7StringBasedUtils {
	
	enum ComparisionType {
		MATCHES,
		CONTAINS,
		STARTS_WITH,
		ENDS_WITH;
	}
	
	
	/**
	 * Returns the message row indexes of the supplied segment. This does not use
	 * the terser.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 */
	public static List<Integer> getSegmentIndexes(Message message, String segmentName) throws Exception {
		List<Integer> segmentIndexes = new ArrayList<>();

		String[] messageRows = message.toString().split("\r");

		for (int i = 0; i < messageRows.length; i++) {
			if (messageRows[i].startsWith(segmentName + "|")) {
				segmentIndexes.add(i);
			}
		}

		return segmentIndexes;
	}
	
	
	/**
	 * Returns the message row index of the first occurence of the supplied segment name.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 * @throws Exception
	 */
	public static Integer getFirstSegmentIndex(Message message, String segmentName) throws Exception {
		return getSegmentIndex(message, segmentName, 1);
	}
	

	/**
	 * Returns a count of the number of segments matching the supplied segment name.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 */
	public static int getSegmentCount(Message message, String segmentName) throws Exception {
		int segmentCount = 0;

		String[] messageRows = message.toString().split("\r");

		for (int i = 0; i < messageRows.length; i++) {
			if (messageRows[i].startsWith(segmentName + "|")) {
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
	public static Integer getSegmentIndex(Message message, String segmentName, int occurence) throws Exception {
		List<Integer>segmentIndexes = getSegmentIndexes(message, segmentName);
		
		if (segmentIndexes.isEmpty()) {
			return null;
		}
		
		return segmentIndexes.get(--occurence);
	}

	
	/**
	 * Returns a segment as a string.
	 * 
	 * @param message
	 * @param segmentName
	 * @param occurence
	 * @return
	 * @throws Exception
	 */
	public static String getSegment(Message message, String segmentName, int occurence) throws Exception {
		
		Integer index = getSegmentIndex(message, segmentName, occurence);
		
		if (index == null) {
			return null;
		}
		
		return getSegment(segmentName, index);
	}

	
	/**
	 * Deletes a segment from a HL7 messages at the supplied row index. This deletes
	 * based on the row index in the raw HL7 messages and does not use the HL7
	 * terser.
	 * 
	 * @param message
	 * @param rowIndex
	 * @throws Exception
	 */
	public static void deleteSegment(Message message, int rowIndex) throws Exception {
		String[] messageRows = message.toString().split("\r");

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < messageRows.length; i++) {
			if (i != rowIndex) {
				sb.append(messageRows[i]).append("\r");
			}
		}

		message.parse(sb.toString());
	}

	
	/**
	 * Deletes an occurence of a segment.
	 * 
	 * @param message
	 * @param segmentName
	 * @param occurence
	 * @throws HL7Exception
	 */
	public static void deleteSegment(Message message, String segmentName, int occurence) throws Exception {
		String[] messageRows = message.toString().split("\r");
		
		StringBuilder sb = new StringBuilder();

		int currentOccurence = 0;
		
		for (String row : messageRows) {
			if (row.startsWith(segmentName + "|")) {
				currentOccurence++;
				
				if (currentOccurence != occurence) {
					sb.append(row).append("\r");
				}
			} else {
				sb.append(row).append("\r");
			}
		}

		message.parse(sb.toString());		
	}

	
	/**
	 * Deletes all segments from a HL7 messages which match the segment name. This
	 * deletes based on the row index in the raw HL7 messages and does not use the
	 * HL7 terser.
	 * 
	 * @param message
	 * @param rowIndex
	 * @throws Exception
	 */
	public static void deleteAllSegments(Message message, String segmentName) throws Exception {
		String[] messageRows = message.toString().split("\r");

		StringBuilder sb = new StringBuilder();

		for (String row : messageRows) {
			if (!row.startsWith(segmentName + "|")) {
				sb.append(row).append("\r");
			}
		}

		message.parse(sb.toString());
	}
	

	/**
	 * Deletes all segments which contains the supplied field value. This does not
	 * use the HL7 terser..
	 * 
	 * @param message
	 * @param segmentName
	 * @param fieldIndex
	 * @throws Exception
	 */
	public static void deleteAllSegmentsMatchingFieldValue(Message message, String segmentName, int fieldIndex, String value) throws Exception {
		deleteAllSegments(message, segmentName, fieldIndex, value, ComparisionType.MATCHES);
	}
	
	
	/**
	 * Deletes a single segment where the supplied value is part of (contains) the
	 * field value. This does not use the HL7 terser.
	 * 
	 * @param message
	 * @param segmentName
	 * @param fieldIndex
	 * @throws Exception
	 */
	public static void deleteAllSegmentsContainingFieldValue(Message message, String segmentName, int fieldIndex, String value) throws Exception {
		deleteAllSegments(message, segmentName, fieldIndex, value, ComparisionType.CONTAINS);
	}
	
	
	/**
	 * Deletes all segments which match the supplied params using the supp,ied comparision type.
	 * 
	 * @param message
	 * @param segmentName
	 * @param fieldIndex
	 * @throws Exception
	 */
	private static void deleteAllSegments(Message message, String segmentName, int fieldIndex, String value, ComparisionType compareType) throws Exception {
		String[] messageRows = message.toString().split("\r");

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < messageRows.length; i++) {
			if (messageRows[i].startsWith(segmentName + "|")) {
				String field = getField(messageRows[i], fieldIndex);
				if (!compare(field, value, compareType)) {
					sb.append(messageRows[i]).append("\r");
				}
			} else {
				sb.append(messageRows[i]).append("\r");
			}
		}

		message.parse(sb.toString());
	}
	
	
	/**
	 * Does this message contain a segment matching the supplied field value.
	 * 
	 * @param message
	 * @param segmentName
	 * @param fieldIndex
	 * @throws Exception
	 */
	public static boolean doesFieldMatchValue(Message message, String segmentName, int fieldIndex, String value) throws Exception {
		return compareFieldValue(message, segmentName, fieldIndex, value, ComparisionType.MATCHES);
	}
	
	
	/**
	 * Does this message contain a segment matching the supplied field value.
	 * 
	 * @param message
	 * @param segmentName
	 * @param fieldIndex
	 * @throws Exception
	 */
	public static boolean doesFieldContainValue(Message message, String segmentName, int fieldIndex, String value) throws Exception {
		return compareFieldValue(message, segmentName, fieldIndex, value, ComparisionType.CONTAINS);
	}
	
	
	/**
	 * Compares a message field value against a search value using the supplied comparison type.
	 * 
	 * @param message
	 * @param segmentName
	 * @param fieldIndex
	 * @param value
	 * @param comparisonType
	 * @return
	 * @throws Exception
	 */
	private static boolean compareFieldValue(Message message, String segmentName, int fieldIndex, String value, ComparisionType comparisonType) throws Exception {
		String[] messageRows = message.toString().split("\r");

		for (int i = 0; i < messageRows.length; i++) {
			if (messageRows[i].startsWith(segmentName + "|")) {
				String field = getField(messageRows[i], fieldIndex);
				if (compare(field, value, comparisonType)) {
					return true;
				}
			}
		}

		return false;
	}

	
	/**
	 * Gets a field value from a segment. This does not use the HL7 terser.
	 * 
	 * @param message
	 * @param rowIndex
	 * @param fieldIndex
	 * @return
	 */
	public static String getField(Message message, int rowIndex, int fieldIndex) {
		String[] messageRows = message.toString().split("\r");

		String requiredSegment = messageRows[rowIndex];

		// Now break up into fields

		String[] segmentFields = requiredSegment.split("\\|");

		String fieldValue = segmentFields[fieldIndex];

		return fieldValue;
	}

	
	/**
	 * Returns a field from a segment. This does not use the HL7 terser.
	 * 
	 * @param segment
	 * @param fieldIndex
	 * @return
	 */
	public static String getField(String segment, int fieldIndex) {
		String[] segmentFields = segment.split("\\|");

		String fieldValue = segmentFields[fieldIndex];
		return fieldValue;
	}
	
	
	/**
	 * Returns a segment at the specified index.
	 * 
	 * @param message
	 * @param segmentIndex
	 * @return
	 */
	public static String getSegment(String message, int segmentIndex) {
		String[] messageRows = message.toString().split("\r");

		return messageRows[segmentIndex];
	}

	
	/**
	 * Appends a non standard segments at the end of the message.
	 * 
	 * @param semgmentName
	 */
	public static String appendNonStandardSegment(Message message, String newSegmentName) throws HL7Exception {
		return message.addNonstandardSegment(newSegmentName);
	}

	
	/**
	 * Inserts a non standard segments at the specified index.
	 * 
	 * @param segmentName
	 * @param index
	 */
	public static String insertNonStandardSegment(Message message,String newSegmentName, int index) throws HL7Exception {	
		return message.addNonstandardSegment(newSegmentName, index);
	}

	
	/**
	 * Inserts a non standard segments after the the supplied afterSegmentName (1st occurence).
	 * 
	 * @param segmentName
	 * @param afterSegmentName
	 */
	public static String insertNonStandardSegmentAfter(Message message, String newSegmentName, String afterSegmentName) throws Exception {
		Integer index = getFirstSegmentIndex(message, afterSegmentName);
		
		if (index == null) {
			throw new HL7Exception("Segment does not exist: " + afterSegmentName);
		}
		
		return insertNonStandardSegment(message, newSegmentName, ++index);
	}

	
	/**
	 * Inserts a non standard segments before the the supplied afterSegmentName
	 * 
	 * @param segmentName
	 * @param afterSegmentName
	 */
	public static String insertNonStandardSegmentBefore(Message message, String newSegmentName, String beforeSegmentName) throws Exception {
		Integer index = getFirstSegmentIndex(message, beforeSegmentName);
		
		if (index == null) {
			throw new HL7Exception("Segment does not exist: " + index);
		}

		return insertNonStandardSegment(message, newSegmentName, index);
	}	
	
	
	/**
	 * Adds a mew segment after all occurences of an existing segment.
	 * 
	 * @param message
	 * @param newSegmentName
	 * @param afterSegmentName
	 * @return
	 * @throws Exception
	 */
	public static List<String> insertNonStandardSegmentAfterEvery(Message message, String newSegmentName, String afterSegmentName) throws Exception {
		int count = getSegmentCount(message, afterSegmentName);
		List<String>segmentNames = new ArrayList<>();
		
		for (int i = 0; i < count; i++) {
			int segmentIndex = getSegmentIndex(message, afterSegmentName, i);
			segmentNames.add(insertNonStandardSegment(message, newSegmentName, ++segmentIndex));
		}
		
		return segmentNames;
	}

	
	/**
	 * Duplicates a message based on a segment type.  eg. if the supplied segmentType is OBX and the message contains 5 OBX segments then 5 messages are
	 * returned with a single OBX segment.
	 * 
	 * @param message
	 * @param segmentType
	 * @return
	 */
	public static List<Message>duplicateMessage(Message message, String segmentType) throws Exception {
		List<Message>newMessages = new ArrayList<>();
        
		// Create an array of messages.  1 message for each matching segment type. eg. if the segment type exists 5 times then create 5 messages.
    	try (HapiContext context = new DefaultHapiContext();) {	
			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);
	
			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			    	
	    	// Count the number of segments
	    	int numberOfMatchingSegments = HL7MessageUtils.getSegmentCount(message, segmentType);
	    	
	    	if (numberOfMatchingSegments == 0) {
	    		throw new HL7Exception("Unable to duplicate the message as the supplied segment does not exist in the message.  Segment: " + segmentType);
	    	}
	    	
	    	for (int i = 0; i < numberOfMatchingSegments; i++) {
	    		String clonedMessage = SerializationUtils.clone(message.toString());
	    		newMessages.add(parser.parse(clonedMessage));	    		    		
	    	}
    	}	
    	
    	int occurenceToKeep = 1;
    	
    	// Now for each of the new messages remove all except one of the matching segments.
    	for (Message newMessage : newMessages) {
    		int indexOfSegmentToKeep = HL7MessageUtils.getSegmentIndex(newMessage, segmentType, occurenceToKeep);
    		    		
    		// Get all the segments indexes
    		List<Integer>allSegmentIndexes = HL7MessageUtils.getSegmentIndexes(newMessage, segmentType);
    		
    		// Make the first matching segment the same as the segment to keep.  This way we just delete the segments which are not the first.
    		HL7MessageUtils.copySegment(newMessage, indexOfSegmentToKeep, allSegmentIndexes.get(0));
    		
    		for (int i = 1; i < allSegmentIndexes.size(); i++) {
    			HL7MessageUtils.deleteSegment(newMessage, segmentType, 2);
    		}
    		
    		occurenceToKeep++;
    	}
    	
    	return newMessages;
	}
	
	
	/**
	 * Copies the content of one segment to another.
	 * 
	 * @param message
	 * @param sourceIndex
	 * @param targetIndex
	 * @throws Exception
	 */
	public static void copySegment(Message message, int sourceIndex, int targetIndex) throws Exception {
		String[] messageRows = message.toString().split("\r");

		String sourceSegment = messageRows[sourceIndex];
	
		messageRows[targetIndex] = sourceSegment;
		
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < messageRows.length; i++) {
			sb.append(messageRows[i]).append("\r");
		}
		
		message.parse(sb.toString());
		
	}

	
	private static boolean compare(String messageField, String compareField, ComparisionType comparisionType) {
	
		switch(comparisionType) {
			case MATCHES:
				return messageField.equals(compareField);
				
			case CONTAINS:
				return messageField.contains(compareField);
				
			case STARTS_WITH:
				return messageField.startsWith(compareField);
			
			case ENDS_WITH:
				return messageField.endsWith(compareField);
		}
		
		throw new IllegalArgumentException("Unknown comparison type: " + comparisionType);
	}
}
