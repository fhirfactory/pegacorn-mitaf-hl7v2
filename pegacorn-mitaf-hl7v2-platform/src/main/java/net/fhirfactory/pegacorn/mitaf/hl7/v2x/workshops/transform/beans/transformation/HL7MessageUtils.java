package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import net.fhirfactory.pegacorn.csv.core.CSV;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model.Field;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model.HL7Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model.Segment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model.Subfield;

/**
 * Utility methods to transform a messages and to get date from a message.
 * 
 * @author Brendan Douglas
 *
 */
public class HL7MessageUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(HL7MessageUtils.class);
	
	
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
	 * Returns a pegacorn HL7 message object from message.
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static HL7Message getHL7Message(Message message) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		
		return hl7Message;
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
     * Copies the contents, including all repetitions and subfields (if any).  Throws a
     * NonExistentHL7ElementException if the source does not exist or is empty or if the target segment
     * does not exist.  Will copy to target field that does not exist as long as an existing segment exists.
     * If multiple source or target segments then the first one will be used for each.
     * 
     * @param message
     * @param targetSegmentName  Segment name to copy to.  Must exist or a NonExistentHL7ElementException
     *                           will be thrown
     * @param targetFieldIndex   Field index to copy to.  Will be created if it does not exist
     * @param sourceSegmentName  Segment name to copy from.  Must exist and not be blank or a
     *                           NonExistentHL7ElementException will be thrown.
     * @param sourceFieldIndex   Field index to copy from.  Must exist and not be blank or a
     *                           NonExistentHL7ElementException will be thrown.
     * @throws Exception
     */
    public static void copyFullField(Message message, String targetSegmentName, int targetFieldIndex, String sourceSegmentName, int sourceFieldIndex) throws Exception {
        String copyMessageDisplay = sourceSegmentName + "-" + sourceFieldIndex + "->" + targetSegmentName + "-" + targetFieldIndex;
        // sanity check on segment names
        if (!StringUtils.isAlphanumeric(targetSegmentName) || !StringUtils.isAlphanumeric(sourceSegmentName)) {
            throw new IllegalArgumentException("Segment names must be alphanumeric: Copy: " + copyMessageDisplay);
        }
        
        // get source segment
        HL7Message hl7Message = new HL7Message(message);
        List<Segment> segments = hl7Message.getSegments(sourceSegmentName);
        if (segments.size() == 0) {
            throw new NonExistentHL7ElementException("Source Segment does not exist: Copy: " + copyMessageDisplay);
        }
        Segment sourceSegment = segments.get(0);
        
        // get the source field
        Field sourceField = sourceSegment.getField(sourceFieldIndex);
        if (sourceField == null || sourceField.isEmpty()) {
            throw new NonExistentHL7ElementException("Source Field does not exist: Copy: " + copyMessageDisplay);
        }
        
        // get target segment
        segments = hl7Message.getSegments(targetSegmentName);
        if (segments.size() == 0) {
            throw new NonExistentHL7ElementException("Target Segment does not exist: Copy: " + copyMessageDisplay);
        }
        Segment targetSegment = segments.get(0);
        
        // get the target field and copy
        Field targetField = targetSegment.getField(targetFieldIndex);
        if (targetField == null) {
            targetSegment.addField(sourceField.value(), targetFieldIndex);
        } else {
            targetField.setValue(sourceField.value());
        }
        
        // reload message
        hl7Message.refreshSourceHL7Message();
        LOG.debug(".copyFullField(): Copied " + copyMessageDisplay);
    }
    
    
    /**
     * Copies the contents, including all repetitions and subfields (if any).  If the source does not exist
     * or is empty then the default source will be used instead.  Throws a NonExistentHL7ElementException
     * if the default source does not exist or is empty or if the target segment does not exist.  Will copy
     * to target field that does not exist as long as an existing segment exists.  If multiple source or
     * target segments then the first one will be used for each.
     * 
     * @param message
     * @param targetSegmentName  Segment name to copy to.  Must exist or a NonExistentHL7ElementException
     *                           will be thrown
     * @param targetFieldIndex
     * @param sourceSegmentName
     * @param sourceFieldIndex
     * @param defaultSourceSegmentName
     * @param defaultSourceFieldIndex
     * @throws Exception
     */
    public static void copyFullField(Message message, String targetSegmentName, int targetFieldIndex, String sourceSegmentName, int sourceFieldIndex, String defaultSourceSegmentName, int defaultSourceFieldIndex) throws Exception {
        try {
            copyFullField(message, targetSegmentName, targetFieldIndex, sourceSegmentName, sourceFieldIndex);
        } catch (NonExistentHL7ElementException e) {
            // use our default
            copyFullField(message, targetSegmentName, targetFieldIndex, defaultSourceSegmentName, defaultSourceFieldIndex);
        }
    }
    
    
    /**
     * Copies the contents, including all repetitions.  Throws a NonExistentHL7ElementException if the
     * source does not exist or is empty or if the target segment does not exist.  Will copy to target
     * subfield that does not exist as long as an existing segment exists.  If multiple source or target
     * segments then the first one will be used for each.
     * 
     * @param message
     * @param targetSegmentName    Segment name to copy to.  Must exist or a NonExistentHL7ElementException
     *                             will be thrown
     * @param targetFieldIndex     Field index to copy to.  Will be created if it does not exist
     * @param targetSubfieldIndex  Subfield index to copy to.  Will be created if it does not exist
     * @param sourceSegmentName    Segment name to copy from.  Must exist and not be blank or a
     *                             NonExistentHL7ElementException will be thrown.
     * @param sourceFieldIndex     Field index to copy from.  Must exist and not be blank or a
     *                             NonExistentHL7ElementException will be thrown.
     * @param sourceSubfieldIndex  Subfield index to copy from.  Must exist and not be blank or a
     *                             NonExistentHL7ElementException will be thrown.
     * @throws Exception
     */
    public static void copyFullField(Message message, String targetSegmentName, int targetFieldIndex, int targetSubfieldIndex, String sourceSegmentName, int sourceFieldIndex, int sourceSubfieldIndex) throws Exception {
        String copyMessageDisplay = sourceSegmentName + "-" + sourceFieldIndex + "-" + sourceSubfieldIndex +  "->" + targetSegmentName + "-" + targetFieldIndex + "-" + sourceSubfieldIndex;
        // sanity check on segment names
        if (!StringUtils.isAlphanumeric(targetSegmentName) || !StringUtils.isAlphanumeric(sourceSegmentName)) {
            throw new IllegalArgumentException("Segment names must be alphanumeric: Copy: " + copyMessageDisplay);
        }
        
        // get source segment
        HL7Message hl7Message = new HL7Message(message);
        List<Segment> segments = hl7Message.getSegments(sourceSegmentName);
        if (segments.size() == 0) {
            throw new NonExistentHL7ElementException("Source Segment does not exist: Copy: " + copyMessageDisplay);
        }
        Segment sourceSegment = segments.get(0);
        
        // get the source field
        Field sourceField = sourceSegment.getField(sourceFieldIndex);
        if (sourceField == null || sourceField.isEmpty()) {
            throw new NonExistentHL7ElementException("Source Field does not exist: Copy: " + copyMessageDisplay);
        }
        
        // get the source subfield
        Subfield sourceSubField = sourceField.getSubField(sourceSubfieldIndex);
        if (sourceSubField == null || sourceSubField.isEmpty()) {
            throw new NonExistentHL7ElementException("Source Subfield does not exist: Copy: " + copyMessageDisplay);
        }
        
        // get target segment
        segments = hl7Message.getSegments(targetSegmentName);
        if (segments.size() == 0) {
            throw new NonExistentHL7ElementException("Target Segment does not exist: Copy: " + copyMessageDisplay);
        }
        Segment targetSegment = segments.get(0);
        
        // get the target field
        Field targetField = targetSegment.getField(targetFieldIndex);
        if (targetField == null) {
            // add the target field
            targetSegment.addField("", targetFieldIndex);
            targetField = targetSegment.getField(targetFieldIndex);
        }
        
        // get the target subfield and copy
        Subfield targetSubField = targetField.getSubField(targetSubfieldIndex);
        if (targetSubField == null) {
            targetField.addSubField(sourceSubField.value(), 0, targetSubfieldIndex);
        } else {
            targetSubField.setValue(sourceSubField.value());
        }
        
        // reload message
        hl7Message.refreshSourceHL7Message();
        LOG.debug(".copyFullField(): Copied " + copyMessageDisplay);
    }
    
    
    /**
     * Copies the contents, including all repetitions.  Copies with the default source if the source does not
     * exist or is empty.  Throws a NonExistentHL7ElementException if the default source does not exist or is
     * empty or if the target segment does not exist.  Will copy to target subfield that does not exist as long
     * as an existing segment exists.  If multiple source or target segments then the first one will be used for
     * each.
     * 
     * @param message
     * @param targetSegmentName
     * @param targetFieldIndex
     * @param targetSubfieldIndex
     * @param sourceSegmentName
     * @param sourceFieldIndex
     * @param sourceSubfieldIndex
     * @param defaultSourceSegmentName
     * @param defaultSourceFieldIndex
     * @param defaultSourceSubfieldIndex
     * @throws Exception
     */
    public static void copyFullField(
            Message message, String targetSegmentName, int targetFieldIndex, int targetSubfieldIndex,
            String sourceSegmentName, int sourceFieldIndex, int sourceSubfieldIndex,
            String defaultSourceSegmentName, int defaultSourceFieldIndex, int defaultSourceSubfieldIndex) throws Exception
    {
        try {
            copyFullField(message, targetSegmentName, targetFieldIndex, targetSubfieldIndex, sourceSegmentName, sourceFieldIndex, sourceSubfieldIndex);
        } catch (NonExistentHL7ElementException e) {
            // use our default
            copyFullField(message, targetSegmentName, targetFieldIndex, targetSubfieldIndex, defaultSourceSegmentName, defaultSourceFieldIndex, defaultSourceSubfieldIndex);
        }
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
			if(!doesContainSegment(segment.getName(), segmentsToKeep)) {
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
	 * Inserts an empty segment with only the segment name populated.
	 * 
	 * @param message
	 * @param newSegmentName
	 * @param segmentIndex
	 */
	public static Segment insertSegment(Message message, String newSegmentName, int segmentIndex, int id) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		
		return hl7Message.insertSegment(newSegmentName, segmentIndex, id);
	}
	
	
	/**
	 * Appends a new segment at the end of the message
	 * 
	 * @param message
	 * @param newSegmentName
	 * @param segmentIndex
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public static Segment appendSegment(Message message, String newSegmentName, int id) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		
		return hl7Message.appendSegment(newSegmentName, id);
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
	 * Returns the message row index of the last occurence of the supplied segment name.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 * @throws Exception
	 */
	public static Integer getLastSegmentIndex(Message message, String segmentName) throws Exception {
		List<Integer> segmentIndexes = getSegmentIndexes(message, segmentName);
		
		if (segmentIndexes.isEmpty()) {
			return null;
		}
		
		return segmentIndexes.get(segmentIndexes.size()-1);		
	}
	
	
	/**
	 * Returns a segment.
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
	 * Returns the 1st occurrence of a segment.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 * @throws Exception
	 */
	public static Segment getSegment(Message message, String segmentName) throws Exception {		
		return getSegment(message, segmentName, 0);
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
	 * Returns the row index of thge supplied segment.
	 * 
	 * @param message
	 * @param segment
	 * @return
	 * @throws Exception
	 */
	public static Integer getSegmentIndex(Message message, Segment segment) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		return hl7Message.getSegmentIndex(segment);
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
	}
	
	
	/**
	 * Removes a segment
	 * 
	 * @param message
	 * @param segment
	 */
	public static void removeSegment(Message message, Segment segment) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		hl7Message.removeSegment(segment);		
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
	 * Copies the content of one segment to another.
	 * 
	 * @param message
	 * @param sourceIndex
	 * @param targetIndex
	 * @throws Exception
	 */
	public static void copySegment(Message message, int sourceIndex, int targetIndex) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		hl7Message.copySegment(sourceIndex, targetIndex);
	}
	
	
	/**
	 * Inserts a segment at the target index which has identical content to the source segment.
	 * 
	 * @param message
	 * @param sourceIndex
	 * @param targetIndex
	 * @throws Exception
	 */
	public static Segment insertSegment(Message message, Segment source, int targetIndex) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		
		return hl7Message.insertSegment(source, targetIndex);
	}
	
	
	
	/**
	 * Moves a segment from one position to another.
	 * 
	 * @param message
	 * @param currentIndex
	 * @param newIndex
	 * @throws Exception
	 */
	public static void moveSegment(Message message, int currentIndex, int newIndex) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		hl7Message.moveSegment(currentIndex, newIndex);
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

	
	/**
	 * Returns the total number of segments.
	 * 
	 * @param message
	 * @return
	 */
	public static int getTotalSegmentCount(Message message) {
		HL7Message hl7Message = new HL7Message(message);
		return hl7Message.getSegments().size();
	}
	
	
	/**
	 * Returns all matching segments.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 * @throws Exception
	 */
	public static List<Segment> getSegments(Message message, String segmentName) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		return hl7Message.getSegments(segmentName);		
	}

	
	/**
	 * Constructs and returns a CSV class which can be used in the transformation templates.
	 * 
	 * @param classname
	 * @param csvContent
	 * @return
	 */
	public static CSV getCSV(String classname, String csvContent) {
		try {
			Class<?> csvClass = Class.forName(classname);
			Constructor<?> csvClassConstructor = csvClass.getConstructor(String.class);
			CSV csv = (CSV) csvClassConstructor.newInstance(new Object[] {csvContent});
			
			return csv;
		} catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
			LOG.info("Unable to construct csv class: {} ", classname);		
		}
		
		return null;
	}
	
	
	/**
	 * Moves a field from the source to the target.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param sourcePathSpec
	 * @throws Exception
	 */
	public static void move(Message message, String targetPathSpec, String sourcePathSpec) throws Exception {
		copy(message, targetPathSpec, sourcePathSpec);
		clear(message, sourcePathSpec);
	}

	
	/**
	 * Clears a field from all matching segments.
	 */ 
	public static void clearFieldFromAllSegments(Message message, String segment, int fieldIndex) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		hl7Message.clearFieldFromAllSegments(segment, fieldIndex);			
	}
	
	
	/**
	 * Clears all fields from a segment starting at the supplied startingFieldIndex in all matching segments.
	 * 
	 * @param message
	 * @param segment
	 * @param startingFieldIndex
	 */
	public static void clearFieldsFrom(Message message, String segment, int startingFieldIndex) throws Exception {
		clearFieldRange(message, segment, startingFieldIndex, -1);
	}
	
	
	/**
	 * Clears all fields from the supplied startingFieldIndex to the endingFieldIndex in all matching segments.
	 * 
	 * @param message
	 * @param segment
	 * @param startingFieldIndex
	 * @param endingFieldIndex
	 */
	public static void clearFieldRange(Message message, String segment, int startingFieldIndex, int endingFieldIndex) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		hl7Message.clearFieldRange(segment, startingFieldIndex, endingFieldIndex);			
	}

	
	/**
	 * Sets a field value in all matching segments.  All repetitions.
	 */ 
	public static void setFieldInAllSegments(Message message, String segment, int fieldIndex, String value) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		hl7Message.setFieldInAllSegments(segment, fieldIndex, value);			
	}

	
	/**
	 * Sets a sub field value in all matching segments.  All repetitions.
	 */ 
	public static void setSubFieldInAllSegments(Message message, String segment, int fieldIndex, int subFieldIndex, String value) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		hl7Message.setSubFieldInAllSegments(segment, fieldIndex, subFieldIndex, value);			
	}

	
	/**
	 * Returns the number of segment groups for the supplied segment
	 * 
	 * @param message
	 * @param segment
	 * @return
	 */
	public static Integer getNumberOfSegmentGroups(Message message, String segment) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		
		return hl7Message.getNumberOfSegmentGroups(segment);
	}

	
	/**
	 * Returns the start index of the segment group.
	 * 
	 * @param message
	 * @param segment
	 * @param groupNumber
	 * @return
	 */
	public static Integer getStartIndexOfSegmentGroup(Message message, String segment, int groupNumber) throws Exception {
		HL7Message hl7Message = new HL7Message(message);	
		
		return hl7Message.getStartIndexOfSegmentGroup(segment, groupNumber);
	}

	
	/**
	 * Returns the end index of the segment group.
	 * 
	 * @param message
	 * @param segment
	 * @param groupNumber
	 * @return
	 */
	public static Integer getEndIndexOfSegmentGroup(Message message, String segment, int groupNumber) throws Exception {
		HL7Message hl7Message = new HL7Message(message);	
		
		return hl7Message.getEndIndexOfSegmentGroup(segment, groupNumber);
	}

	
	/**
	 * Returns all the segments within a group.
	 * 
	 * @param message
	 * @param segment
	 * @param groupNumber
	 * @return
	 */
	public static List<Segment> getSegmentsWithinGroup(Message message, String segment, int groupNumber) throws Exception {
		HL7Message hl7Message = new HL7Message(message);	
		
		return hl7Message.getSegmentsWithinGroup(segment, groupNumber);
	}

	
	/**
	 * Returns an array of indexes which are the start indexes of each segment group.
	 * 
	 * @param segmentName
	 * @return
	 */
	public static List<Integer> getStartIndexesOfSegmentGroups(Message message, String segment) throws Exception {	
		HL7Message hl7Message = new HL7Message(message);	
		
		return hl7Message.getStartIndexesOfSegmentGroups(segment);		
	}
	
	
	/**
	 * Appends a segment to the end of a group
	 * 
	 * @param message
	 * @param segment
	 * @param groupNumber
	 */
	public static Segment appendSegmentToGroup(Message message, String segment, int groupNumber) throws Exception {
		HL7Message hl7Message = new HL7Message(message);	
		
		return hl7Message.appendSegmentToGroup(segment, groupNumber);		
	}

	
	/**
	 * Does the supplied value appear in the specified field of any matching segment.  All field repetitions are checked.
	 * 
	 * @param message
	 * @param segment
	 * @param value
	 * @return
	 */
	public static boolean doesFieldContainValue(Message message, String segment, int fieldIndex, String value) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		
		return hl7Message.doesFieldContainValue(segment, fieldIndex, value);
	}

	
	/**
	 * Does the supplied value appear in the specified subField of any matching segment.  All field repetitions are checked.
	 * 
	 * @param message
	 * @param segment
	 * @param fieldIndex
	 * @param value
	 * @return
	 */
	public static boolean doesSubFieldContainValue(Message message, String segment, int fieldIndex, int subFieldIndex, String value) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		
		return hl7Message.doesSubFieldContainValue(segment, fieldIndex, subFieldIndex, value);
	}
	
	
	/**
	 * Removes a field repetition where the matchValue matches the subField value.
	 * 
	 * @param message
	 * @param fieldIndex
	 * @param subFieldIndex
	 * @param matchValue
	 */
	public static void removeMatchingFieldRepetitions(Message message, String segment, int fieldIndex, int subFieldIndex, String matchValue) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		
		hl7Message.removeMatchingFieldRepetitions(segment, fieldIndex, subFieldIndex, matchValue);		
	}
	
	
	/**
	 * Removes a field repetition where the matchValue does not match the subField value.
	 * 
	 * @param message
	 * @param fieldIndex
	 * @param subFieldIndex
	 * @param matchValue
	 */
	public static void removeNotMatchingFieldRepetitions(Message message, String segment, int fieldIndex, int subFieldIndex, String matchValue) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		
		hl7Message.removeNotMatchingFieldRepetitions(segment, fieldIndex, subFieldIndex, matchValue);	
	}
}
