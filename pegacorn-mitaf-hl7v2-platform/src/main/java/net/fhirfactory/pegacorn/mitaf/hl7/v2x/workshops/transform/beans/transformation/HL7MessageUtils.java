package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation;

import java.time.LocalDate;
import java.util.List;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.util.Terser;

/**
 * Utility methods to transform a messages and to get date from a message.
 * 
 * @author Brendan Douglas
 *
 */
public class HL7MessageUtils {
	
	public static String getType(Message message) {
		return message.getName();
	}

	
	/**
	 * Converts a HL7 date field to a {@link LocalDate}.
	 * 
	 * @param message
	 * @param sourcePathSpec
	 * @return
	 */
	public static LocalDate getDate(Message message, String sourcePathSpec) throws Exception {
		return null;
	}

	
	/**
	 * Returns all 
	 * 
	 * @param message
	 * @param identifierTypes
	 */
	public static void removePatientIdentifierField(Message message, String identifier) throws Exception  {
		HL7TerserBasedUtils.removePatientIdentifierField(message, identifier);
	}
	
	
	/**
	 * Returns a list of identifiers in the PID segment.
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static List<String> getPatientIdentifierCodes(Message message) throws Exception {
		return HL7TerserBasedUtils.getPatientIdentifierCodes(message);
	}
	
	
	/**
	 * Is the message of the supplied type?
	 * 
	 * @param message
	 * @param messageType
	 * @return
	 * @throws Exception
	 */
	public static boolean isType(Message message, String messageType) throws Exception {
		return HL7TerserBasedUtils.isType(message, messageType);
	}

	
	/**
	 * Set a field value.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param value
	 * @throws HL7Exception
	 */
	public static void set(Message message, String targetPathSpec, String value) throws Exception {	
		HL7TerserBasedUtils.set(message, targetPathSpec, value);
	}
	
	
	/**
	 * Set a field value from another field.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param sourcePathSpec
	 * @throws HL7Exception
	 */
	public static void copy(Message message, String sourcePathSpec, String targetPathSpec, boolean copyIfSourceIsBlank, boolean copyIfTargetIsBlank) throws Exception {	
		HL7TerserBasedUtils.copy(message, sourcePathSpec, targetPathSpec, copyIfSourceIsBlank, copyIfTargetIsBlank);
	}
	
	
	/**
	 * Uses a lookup table to change a fields value.
	 * 
	 * @param targetPathSpec
	 * @param lookupTable
	 * @throws HL7Exception
	 */
	public static void lookup(Message message, String targetPathSpec, String lookupTableClassName) throws Exception {	
		HL7TerserBasedUtils.lookup(message, targetPathSpec, lookupTableClassName);
	}

	
	/**
	 * Calls a Java class to set the target path value.
	 * 
	 * @param targetPathSpec
	 * @param transformationClass
	 */
	public static void updateFieldFromCode(Message message, String targetPathSpec, String fieldTransformationClassName) throws Exception {
		HL7TerserBasedUtils.updateFieldFromCode(message, targetPathSpec, fieldTransformationClassName);
	}

	
	/**
	 * Calls a Java class to set the target path value.
	 * 
	 * @param targetPathSpec
	 * @param transformationClass
	 */
	public static void updateMessageFromCode(Message message, String transformationClassName) throws Exception {
		HL7TerserBasedUtils.updateMessageFromCode(message, transformationClassName);
	}

	
	/**
	 * Clear a field value.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @throws HL7Exception
	 */
	public static void clear(Message message, String targetPathSpec) throws Exception {
		HL7TerserBasedUtils.clear(message, targetPathSpec);
	}
	
	
	/**
	 * Returns the message row indexes of the supplied segment.  This does not use the terser.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 */
	public static List<Integer> getSegmentIndexes(Message message, String segmentName) throws Exception {
		return HL7StringBasedUtils.getSegmentIndexes(message, segmentName);
	}

	
	/**
	 * Returns a count of the number of segments matching the supplied segment name.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 */
	public static int getSegmentCount(Message message, String segmentName) throws Exception {
		return HL7StringBasedUtils.getSegmentCount(message, segmentName);
	}

	
	/**
	 * Deletes a segment from a HL7 messages at the supplied row index.
	 * 
	 * @param message
	 * @param rowIndex
	 * @throws Exception
	 */
	public static void deleteSegment(Message message, int rowIndex) throws Exception {
		HL7StringBasedUtils.deleteSegment(message, rowIndex);
	}

	
	/**
	 * Deletes all segments from a HL7 messages which match the segment name.
	 * 
	 * @param message
	 * @param rowIndex
	 * @throws Exception
	 */
	public static void deleteAllSegments(Message message, String segmentName) throws Exception {
		HL7StringBasedUtils.deleteAllSegments(message, segmentName);
	}

	
	/**
	 * Deletes all segments which contains the supplied field value.
	 * 
	 * @param message
	 * @param segmentName
	 * @param fieldIndex
	 * @throws Exception
	 */
	public static void deleteAllSegmentMatchingFieldValue(Message message, String segmentName, int fieldIndex, String value) throws Exception {
		HL7StringBasedUtils.deleteAllSegmentMatchingFieldValue(message, segmentName, fieldIndex, value);
	}

	
	/**
	 * Deletes a single segment where the supplied value is part of (contains) the field value.
	 * 
	 * @param message
	 * @param segmentName
	 * @param fieldIndex
	 * @throws Exception
	 */
	public static void deleteAllSegmentContainingFieldValue(Message message, String segmentName, int fieldIndex, String value) throws Exception {
		HL7StringBasedUtils.deleteAllSegmentContainingFieldValue(message, segmentName, fieldIndex, value);
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
		return HL7StringBasedUtils.doesFieldMatchValue(message, segmentName, fieldIndex, value);
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
		return HL7StringBasedUtils.doesFieldContainValue(message, segmentName, fieldIndex, value);
	}

	
	/**
	 * Gets a field value from a segment.  This does not use the HL7 terser.
	 * 
	 * @param message
	 * @param rowIndex
	 * @param fieldIndex
	 * @return
	 */
	public static String getField(Message message, int rowIndex, int fieldIndex) {
		return HL7StringBasedUtils.getField(message, rowIndex, fieldIndex);
	}

	
	/**
	 * Returns a field from a segment.  This does not use the HL7 terser.
	 * 
	 * @param segment
	 * @param fieldIndex
	 * @return
	 */
	public static String getField(String segment, int fieldIndex) {
		return HL7StringBasedUtils.getField(segment, fieldIndex);
	}

	
	/**
	 * Set a field value from a string with variables.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param seperator
	 * @param values
	 * @throws HL7Exception
	 */
	public static void set(Message message, String targetPathSpec, String value, String ... params) throws Exception {
		Terser terser = new Terser(message);
		
		String finalValue = String.format(value, (Object[])params);
		terser.set(targetPathSpec, finalValue);
	}

	
	/**
	 * Changes the message type
	 * 
	 * @param newMessageType
	 * @throws HL7Exception
	 */
	public static void changeMessageType(Message message, String newMessageType) throws Exception {
		Terser terser = new Terser(message);
		terser.set("/MSH-9", newMessageType);
	}

	
	/**
	 * Gets a field value.
	 * 
	 * @param message
	 * @param sourcePathSpec
	 * @return
	 * @throws HL7Exception
	 */
	public static String get(Message message, String sourcePathSpec) throws Exception {	
		Terser terser = new Terser(message);
		return terser.get(sourcePathSpec);
	}
	

	
	/**
	 * Removes a single segment from a message.
	 * 
	 * @param message
	 * @param sourcePathSpec
	 * @throws HL7Exception
	 */
	public static void removeSegment(Message message, String sourcePathSpec) throws Exception {
		HL7TerserBasedUtils.removeSegment(message, sourcePathSpec);
	}
	
	
	/**
	 * Removes all segments matching the segment name no matter where they appear in the message.  Please note the segment name is not a path spec.
	 * 
	 * @param message
	 * @param segmentName
	 * @throws HL7Exception
	 */
	public static void removeAllSegments(Message message, String segmentName) throws Exception {	
		HL7TerserBasedUtils.removeAllSegments(message, segmentName);
	}
	
	
	/**
	 * Sets the segments to send.  All other segments are removed.  
	 * 
	 * @param message
	 * @param requiredSegments
	 */
	public static void setSegmentsToKeep(Message message, String ... setSegmentsToKeep) throws Exception {	
		HL7TerserBasedUtils.setSegmentsToKeep(message, setSegmentsToKeep);
	}

	
	public static void setSegmentsToKeep(Message message, String setSegmentsToKeep) throws Exception {		
		setSegmentsToKeep(message, setSegmentsToKeep.split(","));
	}
	

	/**
	 * Returns a list of all matching segments.  Please note the segment name is not a path spec.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 */
	public static List<Segment>getAllSegments(Message message, String segmentName) throws Exception {
		return HL7TerserBasedUtils.getAllSegments(message, segmentName);
	}
	
		
	/**
	 * Check if a segment exists.
	 * 
	 * @param message
	 * @param segment
	 * @return
	 */
	public static boolean doesSegmentExist(Message message, String segment) throws Exception {
		return HL7TerserBasedUtils.doesSegmentExist(message, segment);
	}

	
	/**
	 * Executes an action for each segment which matches the segment name.
	 * 
	 * @param segment
	 * @param action
	 */
	public static void forEachSegment(Message message, String segmentName, String actionClassName) throws Exception {
		HL7TerserBasedUtils.forEachSegment(message, segmentName, actionClassName);
	}

	
	/**
	 * Executes an action for a single segment.
	 * 
	 * @param segment
	 * @param action
	 */
	public static void segmentAction(Message message, String sourcePathSpec, String actionClassName) throws Exception {
		HL7TerserBasedUtils.segmentAction(message, sourcePathSpec, actionClassName);
	}
}
