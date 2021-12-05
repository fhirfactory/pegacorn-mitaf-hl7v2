package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation;

import java.util.ArrayList;
import java.util.List;

import ca.uhn.hl7v2.model.Message;

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
	public static void deleteAllSegmentMatchingFieldValue(Message message, String segmentName, int fieldIndex, String value) throws Exception {
		deleteAllSegment(message, segmentName, fieldIndex, value, ComparisionType.MATCHES);
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
	public static void deleteAllSegmentContainingFieldValue(Message message, String segmentName, int fieldIndex, String value) throws Exception {
		deleteAllSegment(message, segmentName, fieldIndex, value, ComparisionType.CONTAINS);
	}
	
	
	/**
	 * Deletes all segments which match the supplied params using the supp,ied comparision type.
	 * 
	 * @param message
	 * @param segmentName
	 * @param fieldIndex
	 * @throws Exception
	 */
	private static void deleteAllSegment(Message message, String segmentName, int fieldIndex, String value, ComparisionType compareType) throws Exception {
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
