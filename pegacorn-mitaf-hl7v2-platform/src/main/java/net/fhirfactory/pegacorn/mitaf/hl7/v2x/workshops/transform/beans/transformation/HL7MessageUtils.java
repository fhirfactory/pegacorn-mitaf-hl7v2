package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;

/**
 * Utility methods to transform a messages and to get date from a message.
 * 
 * @author Brendan Douglas
 *
 */
public class HL7MessageUtils {
	
	
	/**
	 * Returns a {@link Message} from a String
	 * 
	 * @param message
	 * @return
	 */
	public static Message getMessage(String message) throws Exception {
		try (HapiContext context = new DefaultHapiContext();) {    
            PipeParser parser = context.getPipeParser();
            parser.getParserConfiguration().setValidating(false);
    
            ModelClassFactory cmf = new DefaultModelClassFactory();
            context.setModelClassFactory(cmf);
            
            Message inputMessage = parser.parse(message);
            
            return inputMessage;
		}
	}
	
	
	public static String getMessageCode(Message message) throws Exception {
		return get(message, "MSH-9-1");
	}

	
	/**
	 * Gets the message type.
	 * 
	 * @param message
	 * @return
	 */
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
	 * Removes a patient identifier.
	 * 
	 * @param message
	 * @param identifier
	 * @throws Exception
	 */
	public static void removePatientIdentifierField(Message message, String identifier) throws Exception  {
		HL7TerserBasedUtils.removePatientIdentifierField(message, identifier);
	}
	
	
	/**
	 * @param message
	 * @param identifier
	 * @throws Exception
	 */
	public static String getIdentifierValue(Message message, String identifier) throws Exception  {
		return HL7TerserBasedUtils.getIdentifierValue(message, identifier);
	}
	
	
	/**
	 * Returns a list of patient identifiers in the PID segment.
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
	 * Set the target field to the supplied value.
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
	 * Copies the content of one field to another.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param sourcePathSpec
	 * @throws HL7Exception
	 */
	public static void copy(Message message, String targetPathSpec, String sourcePathSpec, boolean copyIfSourceIsBlank, boolean copyIfTargetIsBlank) throws Exception {	
		HL7TerserBasedUtils.copy(message, targetPathSpec, sourcePathSpec, copyIfSourceIsBlank, copyIfTargetIsBlank);
	}

	
	/**
	 * Copies the content of one field to another.
	 * 
	 * @param message
	 * @param sourcePathSpec
	 * @param targetPathSpec
	 * @throws Exception
	 */
	public static void copy(Message message, String targetPathSpec, String sourcePathSpec) throws Exception {	
		HL7TerserBasedUtils.copy(message, targetPathSpec, sourcePathSpec, true, true);
	}

	
	/**
	 * Copies the content from one field to another.  If the source field is null then the default source path is used.
	 * 
	 * @param message
	 * @param sourcePathSpec
	 * @param targetPathSpec
	 * @param defaultIfSourceIsNull
	 */
	public static void copy(Message message, String targetPathSpec, String sourcePathSpec, String defaultSourcepathSpec) throws Exception {
		HL7TerserBasedUtils.copy(message, targetPathSpec, sourcePathSpec, defaultSourcepathSpec);
	}
	
	
	/**
	 * Copies the content of the source path before the seperator character to the target.  If the seperator does not exists the entire field is copied.
	 * 
	 * @param message
	 * @param sourcePathSpec
	 * @param targetPathSpec
	 * @param seperator
	 */
	public static void copySubstringBefore(Message message, String targetPathSpec, String sourcePathSpec, String seperator) throws Exception {
		HL7TerserBasedUtils.copySubstringBefore(message, targetPathSpec, sourcePathSpec, seperator);			
	}
	
	
	/**
	 * Copies the content of the source path after the seperator character to the target.  If the seperator does not exists the entire field is copied.
	 * 
	 * @param message
	 * @param sourcePathSpec
	 * @param targetPathSpec
	 * @param seperator
	 */
	public static void copySubstringAfter(Message message, String targetPathSpec, String sourcePathSpec, String seperator) throws Exception {
		HL7TerserBasedUtils.copySubstringAfter(message, targetPathSpec, sourcePathSpec, seperator);
	}

	
	/**
	 * Concatenates the content of the source fields.
	 * 
	 * @param message
	 * @param targetpathSpec
	 * @param seperator
	 * @param sourcePathSpecs
	 */
	public static void concatenate(Message message, String targetPathSpec, String seperator, String ... sourcePathSpecs) throws Exception {
		HL7TerserBasedUtils.concatenate(message, targetPathSpec, seperator, sourcePathSpecs);
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
	 * Returns the message row indexes of the supplied segment.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 * @throws Exception
	 */
	public static List<Integer> getSegmentIndexes(Message message, String segmentName) throws Exception {
		return HL7StringBasedUtils.getSegmentIndexes(message, segmentName);
	}
	
	
	/**
	 * Returns the index of a matching segment starting from the supplied starting from index.
	 * 
	 * @param message
	 * @param segmentName
	 * @param startingFrom
	 * @return
	 * @throws Exception
	 */
	public static Integer getNextIndex(Message message, String segmentName, int startFromIndex) throws Exception {
		String[] messageRows = message.toString().split("\r");

		for (int i = startFromIndex; i < messageRows.length; i++) {
			if (messageRows[i].startsWith(segmentName + "|")) {
				return i;
			}
		}
		
		return null;
	}
	
	
	/**
	 * Returns all the indexes of a matching segment startinf from the supplied start from index,
	 * 
	 * @param message
	 * @param segmentName
	 * @param startFromIndex
	 * @return
	 * @throws Exception
	 */
	public static  List<Integer> getSegmentIndexes(Message message, String segmentName, int startFromIndex) throws Exception {
		List<Integer> segmentIndexes = new ArrayList<>();
		
		String[] messageRows = message.toString().split("\r");

		for (int i = startFromIndex; i < messageRows.length; i++) {
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
	 * Deletes an occurence of a segment.
	 * 
	 * @param message
	 * @param segmentName
	 * @param occurence
	 */
	public static void deleteSegment(Message message, String segmentName, int occurence) throws Exception {
		HL7StringBasedUtils.deleteSegment(message, segmentName, occurence);
	}

	
	/**
	 * Deletes all segments which contains the supplied field value.
	 * 
	 * @param message
	 * @param segmentName
	 * @param fieldIndex
	 * @throws Exception
	 */
	public static void deleteAllSegmentsMatchingFieldValue(Message message, String segmentName, int fieldIndex, String value) throws Exception {
		HL7StringBasedUtils.deleteAllSegmentsMatchingFieldValue(message, segmentName, fieldIndex, value);
	}

	
	/**
	 * Deletes a single segment where the supplied value is part of (contains) the field value.
	 * 
	 * @param message
	 * @param segmentName
	 * @param fieldIndex
	 * @throws Exception
	 */
	public static void deleteAllSegmentsContainingFieldValue(Message message, String segmentName, int fieldIndex, String value) throws Exception {
		HL7StringBasedUtils.deleteAllSegmentsContainingFieldValue(message, segmentName, fieldIndex, value);
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
	 * Returns a subfield.
	 * 
	 * @param segment
	 * @param fieldIndex
	 * @param subfield
	 */
	public static String getSubfield(String field, int subFieldIndex) {
		return HL7StringBasedUtils.getSubfield(field, subFieldIndex);
	}

	
	/**
	 * Returns a subfield.
	 * 
	 * @param segment
	 * @param fieldIndex
	 * @param subfield
	 */
	public static String getSubfield(String segment, int fieldIndex, int subFieldIndex) {
		return HL7StringBasedUtils.getSubfield(segment, fieldIndex, subFieldIndex);
	}
	
	
	/**
	 * Returns a subfield.
	 * 
	 * @param message
	 * @param segmentIndex
	 * @param fieldIndex
	 * @param subFieldIndex
	 * @return
	 */
	public static String getSubfield(String message, int segmentIndex, int fieldIndex, int subFieldIndex) {
		return HL7StringBasedUtils.getSubfield(message, segmentIndex, fieldIndex, subFieldIndex);
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
	 * Removes all segments matching the segment name no matter where they appear in the message.
	 * 
	 * @param message
	 * @param segmentName
	 * @throws HL7Exception
	 */
	public static void removeAllSegments(Message message, String segmentName) throws Exception {	
		HL7TerserBasedUtils.removeAllSegments(message, segmentName);
	}
	
	
	/**
	 * Sets the segments to keep. 
	 * 
	 * @param message
	 * @param requiredSegments
	 */
	public static void setSegmentsToKeep(Message message, String ... setSegmentsToKeep) throws Exception {	
		HL7TerserBasedUtils.setSegmentsToKeep(message, setSegmentsToKeep);
	}

	
	/**
	 * Sets the segments to keep.  The segments to keep are a comma delimited list.
	 * 
	 * @param message
	 * @param setSegmentsToKeep
	 * @throws Exception
	 */
	public static void setSegmentsToKeep(Message message, String setSegmentsToKeep) throws Exception {		
		setSegmentsToKeep(message, setSegmentsToKeep.split(","));
	}

	/**
	 * Returns a list of all matching segments.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 * @throws Exception
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

	
	/**
	 * Appends a non standard segment at the end of the message.
	 * 
	 * @param semgmentName
	 */
	public static String appendNonStandardSegment(Message message, String newSegmentName) throws Exception {	
		return HL7StringBasedUtils.appendNonStandardSegment(message, newSegmentName);
	}
	
	
	/**
	 * Inserts a non standard segment at the specified index.
	 * 
	 * @param segmentName
	 * @param index
	 */
	public static String insertNonStandardSegment(Message message, String newSegmentName, int index) throws Exception {	
		return HL7StringBasedUtils.insertNonStandardSegment(message, newSegmentName, index);
	}

	
	/**
	 * Inserts a non standard segment after the the supplied afterSegmentName (1st occurence).
	 * 
	 * @param segmentName
	 * @param afterSegmentName
	 */
	public static String insertNonStandardSegmentAfter(Message message, String newSegmentName, String afterSegmentName) throws Exception {
		return HL7StringBasedUtils.insertNonStandardSegmentAfter(message, newSegmentName, afterSegmentName);
	}

	
	/**
	 * Inserts a non standard segment before the the supplied beforeSegmentName (1st occurence)
	 * 
	 * @param segmentName
	 * @param afterSegmentName
	 */
	public static String insertNonStandardSegmentBefore(Message message, String newSegmentName, String beforeSegmentName) throws Exception {
		return HL7StringBasedUtils.insertNonStandardSegmentBefore(message, newSegmentName, beforeSegmentName);
	}	

	
	/**
	 * Insert a non standard segment after every afterSegmentName.
	 * 
	 * @param segmentName
	 * @param afterSegmentName
	 */
	public static List<String> insertNonStandardSegmentAfterEvery(Message message, String newSegmentName, String afterSegmentName) throws Exception{	
		return HL7StringBasedUtils.insertNonStandardSegmentAfterEvery(message, newSegmentName, afterSegmentName);
	}

	
	/**
	 * Gets a segment at the specified index.
	 * 
	 * @param message
	 * @param segmentIndex
	 * @return
	 * @throws Exception
	 */
	public static String getSegment(Message message, int segmentIndex) throws Exception {
		return HL7StringBasedUtils.getSegment(message, segmentIndex);
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
		return HL7StringBasedUtils.getSegmentIndex(message, segmentName, occurence);
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
		return getSegmentIndex(message, segmentName, 0);
	}

	/**
	 * Returns a segment content as a string.
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
		
		return getSegment(message, index);
	}
	
	
	/**
	 * Copies a value from one field to another and replace the params.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param text
	 * @param sourcePathSpecs
	 * @throws Exception
	 */
	public static void copyReplaceParam(Message message, String targetPathSpec, String sourcePathSpec, String ... sourcePathSpecs) throws Exception {
		HL7TerserBasedUtils.copyReplaceParam(message, targetPathSpec, sourcePathSpec, sourcePathSpecs);
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
		return HL7StringBasedUtils.duplicateMessage(message, segmentType);
	}
	
	
	/**
	 * Copies the content of one segment to another.
	 * 
	 * @param message
	 * @param sourceIndex
	 * @param targetIndex
	 */
	public static void copySegment(Message message, int sourceIndex, int targetIndex) throws Exception {
		HL7StringBasedUtils.copySegment(message, sourceIndex, targetIndex);
	}
}
