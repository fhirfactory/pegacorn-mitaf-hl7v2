package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SerializationUtils;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model.HL7Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model.Segment;

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
	
	
	/**
	 * 
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
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
	 * Removes a patient identifier from the PID segment.
	 * 
	 * @param message
	 * @param identifier
	 * @throws Exception
	 */
	public static void removePatientIdentifierField(Message message, String identifier) throws Exception  {
		HL7TerserBasedUtils.removePatientIdentifierField(message, identifier, "PID");
	}
	
	
	/**
	 * Gets a patient identifier value from the PID segment
	 * 
	 * @param message
	 * @param identifier
	 * @throws Exception
	 */
	public static String getPatientIdentifierValue(Message message, String identifier) throws Exception  {
		return HL7TerserBasedUtils.getPatientIdentifierValue(message, identifier, "PID");
	}
	
	
	/**
	 * Removes a patient identifier from the PID segment.  The path to the PID segment needs to be supplied eg. PATIENT_RESULT/PATIENT/PID
	 * 
	 * @param message
	 * @param identifier
	 * @throws Exception
	 */
	public static void removePatientIdentifierField(Message message, String identifier, String pidSegmentPath) throws Exception  {
		HL7TerserBasedUtils.removePatientIdentifierField(message, identifier, pidSegmentPath);
	}
	
	
	/**
	 * Gets a patient identifier from the PID segment. The path to the PID segment needs to be supplied eg. PATIENT_RESULT/PATIENT/PID
	 * 
	 * @param message
	 * @param identifier
	 * @throws Exception
	 */
	public static String getPatientIdentifierValue(Message message, String identifier, String pidSegmentPath) throws Exception  {
		return HL7TerserBasedUtils.getPatientIdentifierValue(message, identifier, pidSegmentPath);
	}
	
	
	/**
	 * Returns a list of patient identifiers in the PID segment.
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static List<String> getPatientIdentifierCodes(Message message) throws Exception {
		return HL7TerserBasedUtils.getPatientIdentifierCodes(message, "PID");
	}

	
	/**
	 * Removes patient identifiers which do not match the identifier to keep.
	 * 
	 * @param message
	 * @param identifier
	 * @throws Exception
	 */
	public static void removeOtherPatientIdentifierFields(Message message, String identifierToKeep) throws Exception  {
		HL7TerserBasedUtils.removeOtherPatientIdentifierFields(message, identifierToKeep, "PID");
	}

	
	/**
	 * Removes patient identifiers which do not match the identifier to keep.  The path to the PID segment needs to be supplied eg. PATIENT_RESULT/PATIENT/PID
	 * 
	 * @param message
	 * @param identifier
	 * @throws Exception
	 */
	public static void removeOtherPatientIdentifierFields(Message message, String identifierToKeep, String pidSegmentPath) throws Exception  {
		HL7TerserBasedUtils.removeOtherPatientIdentifierFields(message, identifierToKeep, pidSegmentPath);
	}

	
	/**
	 * Returns a list of patient identifiers in the PID segment.  The path to the PID segment needs to be supplied eg. PATIENT_RESULT/PATIENT/PID
	 * 
	 * @param message
	 * @param pidSegmentPath
	 * @return
	 * @throws Exception
	 */
	public static List<String> getPatientIdentifierCodes(Message message, String pidSegmentPath) throws Exception {
		return HL7TerserBasedUtils.getPatientIdentifierCodes(message, pidSegmentPath);
	}

	
	/**
	 * Removes the patient identifier type code but leave everything else in the identifier field. The path to the PID segment needs to be supplied eg. PATIENT_RESULT/PATIENT/PID
	 * 
	 * @param message
	 * @param identifier
	 * @throws Exception
	 */
	public static void removePatientIdentifierTypeCode(Message message, String identifier, String pidSegmentPath) throws Exception  {
		HL7TerserBasedUtils.removePatientIdentifierTypeCode(message, identifier, pidSegmentPath);
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
	 * Concatenates the content of the source fields with the specified seperator.
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
	 * 
	 * Concatenates the content of the source fields without a seperator.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param sourcePathSpecs
	 * @throws Exception
	 */
	public static void concatenate(Message message, String targetPathSpec, String ... sourcePathSpecs) throws Exception {
		HL7TerserBasedUtils.concatenate(message, targetPathSpec, "", sourcePathSpecs);
	}

	
	/**
	 * Appends the supplied text to the value at the targetPathSpec.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param textToAppend
	 */
	public static void append(Message message, String targetPathSpec, String textToAppend) throws Exception {
		HL7TerserBasedUtils.append(message, targetPathSpec, textToAppend);
	}

	
	/**
	 * Prepends the supplied text to the value at the targetPathSpec.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param textToPrepend
	 */
	public static void prepend(Message message, String targetPathSpec, String textToPrepend) throws Exception {
		HL7TerserBasedUtils.prepend(message, targetPathSpec, textToPrepend);		
	}
	
	
	/**
	 * Clear a single field value including all repetitions.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @throws HL7Exception
	 */
	public static void clear(Message message, String targetPathSpec) throws Exception {
		HL7TerserBasedUtils.clear(message, targetPathSpec);
	}
	
	
	/**
	 * Clears multiple fields in a single command.
	 * 
	 * @param message
	 * @param targetPathSpecs
	 * @throws Exception
	 */
	public static void clear(Message message, String ... targetPathSpecs) throws Exception {
		
		for (String targetPathSpec : targetPathSpecs) {
			HL7TerserBasedUtils.clear(message, targetPathSpec);
		}
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
	 * Removes all segments matching the segment name no matter where they appear in the message.
	 * 
	 * @param message
	 * @param segmentName
	 * @throws HL7Exception
	 */
	public static void removeAllSegments(Message message, String segmentName) throws Exception {	
		HL7Message hl7Message = new HL7Message(message);
		hl7Message.removeAllMatchingSegments(segmentName);
	}

	
	/**
	 * Sets the segments to keep. 
	 * 
	 * @param message
	 * @param requiredSegments
	 */
	public static void setSegmentsToKeep(Message message, String ... segmentsToKeep) throws Exception {	
		HL7Message hl7Message = new HL7Message(message);
		
		List<String>segmentsToRemove = new ArrayList<>();
		
		for (Segment segment : hl7Message.getSegments()) {
			if(doesContainSegment(segment.getName(), segmentsToKeep)) {
				segmentsToRemove.add(segment.getName());
			}
		}
		
		for (String segmentToRemove : segmentsToRemove) {
			hl7Message.removeAllMatchingSegments(segmentToRemove);
		}
	}

	
	private static boolean doesContainSegment(String segmentName, String[] requiredSegments) {
		for (String requiredSegment : requiredSegments) {
			if (segmentName.equals(requiredSegment)) {
				return true;
			}
		}
		
		return false;
	}

	
	/**
	 * Sets the segments to keep.  The segments to keep are a comma delimited list.
	 * 
	 * @param message
	 * @param setSegmentsToKeep
	 * @throws Exception
	 */
	public static void setSegmentsToKeep(Message message, String segmentsToKeep) throws Exception {		
		setSegmentsToKeep(message, segmentsToKeep.split(","));
	}
	
	
	/**
	 * Check if a segment exists.
	 * 
	 * @param message
	 * @param segment
	 * @return
	 */
	public static boolean doesSegmentExist(Message message, String segment) throws Exception {
		 String regex = segment + "\\|";
		 Pattern pattern = Pattern.compile(regex);
		 Matcher matcher = pattern.matcher(message.toString());
		 
		 return matcher.find();
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
	public static Segment getSegment(Message message, String segmentName, int occurence) throws Exception {
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
	 * Changes the message version number.
	 * 
	 * @param message
	 * @param newVersion
	 * @throws Exception
	 */
	public static void changeMessageVersion(Message message, String newVersion) throws Exception {
		HL7TerserBasedUtils.set(message, "MSH-12", newVersion);
	}

	
	/**
	 * Returns the number of repetitions of a field within a segment.
	 * 
	 * @param message
	 * @param segmentPathSpec
	 * @param fieldIndex
	 * @return
	 * @throws Exception
	 */
	public static int getNumberOfRepetitions(Message message, String segmentPathSpec, int fieldIndex) throws Exception {
		return HL7TerserBasedUtils.getNumberOfRepetitions(message, segmentPathSpec, fieldIndex);
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
		HL7Message HL7Message = new HL7Message(message);
		return HL7Message.getSegmentIndexes(segmentName);
	}
	
	
	/**
	 * Returns a count of the number of segments matching the supplied segment name.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 */
	public static int getSegmentCount(Message message, String segmentName) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		return hl7Message.getSegmentCount(segmentName);
	}

	
	/**
	 * Returns the index of a matching segment.
	 * 
	 * @param message
	 * @param segmentName
	 * @param occurence - starts at 0
	 * @return
	 * @throws Exception
	 */
	public static Integer getSegmentIndex(Message message, String segmentName, int occurence) throws Exception {
		List<Integer>segmentIndexes = getSegmentIndexes(message, segmentName);
		
		if (segmentIndexes.isEmpty()) {
			return null;
		}
		
		if (occurence > segmentIndexes.size()) {
			return null;
		}
		
		return segmentIndexes.get(occurence);
	}
	
	
	/**
	 * Deletes a segment from a HL7 messages at the supplied row index.
	 * 
	 * @param message
	 * @param rowIndex
	 * @throws Exception
	 */
	public static void removeSegment(Message message, int rowIndex) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		hl7Message.removeSegment(rowIndex);
		
		hl7Message.refreshSourceHL7Message();
	}

	
	/**
	 * Deletes an occurence of a segment.
	 * 
	 * @param message
	 * @param segmentName
	 * @param occurence - starts at 0
	 * @throws HL7Exception
	 */
	public static void removeSegment(Message message, String segmentName, int occurence) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		hl7Message.removeSegment(segmentName, occurence);
		hl7Message.refreshSourceHL7Message();		
	}

	
	/**
	 * Deletes matching segments(s).
	 * 
	 * @param message
	 * @param segmentName Either the name of a segment or a path.
	 * @throws Exception
	 */
	public static void removeSegment(Message message, String segmentName) throws Exception {
		if (segmentName.length() == 3) {
			HL7Message hl7Message = new HL7Message(message);
			hl7Message.removeAllMatchingSegments(segmentName);
			
			hl7Message.refreshSourceHL7Message();
		} else {
			HL7TerserBasedUtils.removeSegment(message, segmentName);	
		}
	}
	
	
	/**
	 * Returns a segment at the specified index.
	 * 
	 * @param message
	 * @param segmentIndex
	 * @return
	 */
	public static Segment getSegment(Message message, int segmentIndex) {
		HL7Message hl7Message = new HL7Message(message);
		return hl7Message.getSegment(segmentIndex);
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
	    	int numberOfMatchingSegments = getSegmentCount(message, segmentType);
	    	
	    	if (numberOfMatchingSegments == 0) {
	    		throw new HL7Exception("Unable to duplicate the message as the supplied segment does not exist in the message.  Segment: " + segmentType);
	    	}
	    	
	    	for (int i = 0; i < numberOfMatchingSegments; i++) {
	    		String clonedMessage = SerializationUtils.clone(message.toString());
	    		newMessages.add(parser.parse(clonedMessage));	    		    		
	    	}
    	}	
    	
    	int occurenceToKeep = 0;
    	
    	// Now for each of the new messages remove all except one of the matching segments.
    	for (Message newMessage : newMessages) {
  		HL7Message hl7Message = new HL7Message(newMessage);
    		
    		int indexOfSegmentToKeep = hl7Message.getSegmentIndex(segmentType, occurenceToKeep);
    		    		
    		// Get all the segments indexes
    		List<Integer>allSegmentIndexes = hl7Message.getSegmentIndexes(segmentType);
    		
    		// Make the first matching segment the same as the segment to keep.  This way we just delete the segments which are not the first.
    		hl7Message.copySegment(indexOfSegmentToKeep, allSegmentIndexes.get(0));
    		
    		for (int i = 1; i < allSegmentIndexes.size(); i++) {
    			hl7Message.removeSegment(segmentType, 1);
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
		HL7Message HL7Message = new HL7Message(message);
		HL7Message.copySegment(sourceIndex, targetIndex);
		
		HL7Message.refreshSourceHL7Message();
	}
	
	
	/**
	 * Returns a list of all matching segments.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 * @throws Exception
	 */
	public static List<ca.uhn.hl7v2.model.Segment>getAllSegments(Message message, String segmentName) throws Exception {
		return HL7TerserBasedUtils.getAllSegments(message, segmentName);
	}
}
