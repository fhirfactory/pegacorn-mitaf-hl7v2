package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.util.SegmentFinder;
import ca.uhn.hl7v2.util.Terser;

/**
 * Utilities that parse/query a HL7 document using a HL7 library/terser.
 * 
 * @author Brendan Douglas
 *
 */
@Deprecated
class HL7TerserBasedUtils {
    private static final Logger LOG = LoggerFactory.getLogger(HL7MessageUtils.class);

    
	/**
	 * Returns a patient identifier. 
	 * 
	 * @param message
	 * @param identifierTypes
	 */
	public static String getPatientIdentifierValue(Message message, String identifier, String pidSegmentPath) throws Exception  {
		Terser terser = new Terser(message);
		
		Segment segment = terser.getSegment(pidSegmentPath);
		int numberOfRepeitions = segment.getField(3).length;
		
		for (int i = 0; i < numberOfRepeitions; i++) {
			String identifierType = terser.get(pidSegmentPath + "-3(" + i + ")-5-1");
			
			if (identifierType != null && identifierType.equals(identifier)) {
				return terser.get(pidSegmentPath + "-3(" + i + ")-1-1");
			}
		}
		
		return "";
	}
	
	
	public static void removePatientIdentifierField(Message message, String identifier, String pidSegmentPath) throws Exception  {
		Terser terser = new Terser(message);
		
		Segment segment = terser.getSegment(pidSegmentPath);
		int numberOfRepeitions = segment.getField(3).length;
		
		for (int i = 0; i < numberOfRepeitions; i++) {
			String identifierType = terser.get(pidSegmentPath + "-3(" + i + ")-5-1");
			
			if (identifierType != null && identifierType.equals(identifier)) {
				((AbstractSegment)segment).removeRepetition(3, i);
			}
		}
		
		message.parse(message.toString());		
	}
	
	
	public static void removePatientIdentifierTypeCode(Message message, String identifier, String pidSegmentPath) throws Exception  {
		Terser terser = new Terser(message);
		
		Segment segment = terser.getSegment(pidSegmentPath);
		int numberOfRepeitions = segment.getField(3).length;
		
		for (int i = 0; i < numberOfRepeitions; i++) {
			String identifierType = terser.get(pidSegmentPath + "-3(" + i + ")-5-1");
			
			if (identifierType != null && identifierType.equals(identifier)) {
				clear(message, pidSegmentPath + "-3(" + i + ")-5-1");
			}
		}
		
		message.parse(message.toString());		
	}
	
	
	/**
	 * Returns a list of identifiers in the PID segment.
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static List<String> getPatientIdentifierCodes(Message message, String pidSegmentPath) throws Exception {
		List<String>identifiers = new ArrayList<>();
		
		Terser terser = new Terser(message);
		
		Segment segment = terser.getSegment(pidSegmentPath);
		int numberOfRepeitions = segment.getField(3).length;
		
		for (int i = 0; i < numberOfRepeitions; i++) {
			String identifier = terser.get(pidSegmentPath + "-3(" + i + ")-5-1");
			
			if (identifier != null) {
				identifiers.add(identifier);
			}
		}	
		
		return identifiers;
	}

	
	/**
	 * Removes patient identifier which do not match the idetifier to keep.
	 * 
	 * @param message
	 * @param identifierToKeep
	 * @param pidSegmentPath
	 * @throws Exception
	 */
	public static void removeOtherPatientIdentifierFields(Message message, String identifierToKeep, String pidSegmentPath) throws Exception  {
		List<String>patientIdentifierCodes = getPatientIdentifierCodes(message, pidSegmentPath);
		
		for (String patientIdentifierCode : patientIdentifierCodes) {
			if (!patientIdentifierCode.equals(identifierToKeep)) {
				removePatientIdentifierField(message, patientIdentifierCode, pidSegmentPath);
			}
		}	
	}

	
	/**
	 * Returns the number of repetitions of a field.
	 * 
	 * @param message
	 * @param pathSpec
	 * @return
	 */
	public static int getNumberOfRepetitions(Message message, String segmentPathSpec, int fieldIndex) throws Exception {
		Terser terser = new Terser(message);
		
		Segment segment = terser.getSegment(segmentPathSpec);
		return segment.getField(fieldIndex).length;
	}

	
	/**
	 * is the message of the supplied type.  The messageType can contain a wildcard eg. ADT_* for all ADT messages or no wildcard eg. ADT_A60.
	 * 
	 * @param message
	 * @param messageType
	 * @return
	 * @throws Exception
	 */
	public static boolean isType(Message message, String messageType) throws Exception {
		Terser terser = new Terser(message);
		
		String type = terser.get("/MSH-9-3");

		if (StringUtils.isBlank(type)) {
			type = terser.get("/MSH-9-1") + "_" + terser.get("/MSH-9-2");
		}
		
		
		if (messageType.endsWith("_*")) {	
			return type.substring(0, 3).equals(messageType.substring(0, 3));
		}
		
		return type.equals(messageType);
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
		Terser terser = new Terser(message);
		terser.set(targetPathSpec, value);
	}

	
	/**
	 * Set a field value from another field.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param sourcePathSpec
	 * @throws HL7Exception
	 */
	public static void copy(Message message, String targetPathSpec, String sourcePathSpec, boolean copyIfSourceIsBlank, boolean copyIfTargetIsBlank) throws Exception {	
		Terser terser = new Terser(message);
		
		String sourceValue = terser.get(sourcePathSpec);
		String targetValue = terser.get(targetPathSpec);
		
		if (!copyIfSourceIsBlank && StringUtils.isBlank(sourceValue)) {
			return;
		}
		
		if (!copyIfTargetIsBlank && StringUtils.isBlank(targetValue)) {
			return;
		}
		
		terser.set(targetPathSpec, sourceValue);	
	}

	
	/**
	 * Copies from the source field to the target, only if the source field contains a value.
	 * 
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param sourcePathSpec
	 * @param copyIfSourceIsBlank
	 * @param copyIfTargetIsBlank
	 * @throws Exception
	 */
	public static void copyIfSourceExists(Message message, String targetPathSpec, String sourcePathSpec, boolean copyIfSourceIsBlank, boolean copyIfTargetIsBlank) throws Exception {	
		copy(message, targetPathSpec, sourcePathSpec, false, true);
	}

	
	/**
	 * Copies from one field to another.  If the source value is null a default value is used.
	 * 
	 * @param message
	 * @param sourcePathSpec
	 * @param targetPathSpec
	 * @param defaultIfSourceIsNull
	 * @throws Exception
	 */
	public static void copy(Message message, String targetPathSpec, String sourcePathSpec, String defaultSourcepathSpec) throws Exception {
		Terser terser = new Terser(message);
		
		String sourceValue = terser.get(sourcePathSpec);
		
		if (sourceValue == null) {
			sourceValue = terser.get(defaultSourcepathSpec);
		}
		
		terser.set(targetPathSpec, sourceValue);			
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
		Terser terser = new Terser(message);
		
		String sourceValue = terser.get(sourcePathSpec);	
		int indexOfSeperator = StringUtils.indexOf(sourceValue, seperator);
		
		if (indexOfSeperator == -1) {
			terser.set(targetPathSpec, sourceValue);
		} else {
			terser.set(targetPathSpec, StringUtils.substring(sourceValue, 0, indexOfSeperator));
		}
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
		Terser terser = new Terser(message);
		
		String sourceValue = terser.get(sourcePathSpec);	
		int indexOfSeperator = StringUtils.indexOf(sourceValue, seperator);
		
		if (indexOfSeperator == -1) {
			terser.set(targetPathSpec, sourceValue);
		} else {
			terser.set(targetPathSpec, StringUtils.substring(sourceValue, indexOfSeperator + 1, sourceValue.length()));
		}
	}

	
	/**
	 * Appends the supplied text to the value at the targetPathSpec.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param textToAppend
	 */
	public static void append(Message message, String targetPathSpec, String textToAppend) throws Exception {
		String targetValue = get(message, targetPathSpec);	
		set(message, targetPathSpec, targetValue + textToAppend);		
	}

	
	/**
	 * Prepends the supplied text to the value at the targetPathSpec.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param textToPrepend
	 */
	public static void prepend(Message message, String targetPathSpec, String textToPrepend) throws Exception {
		String targetValue = get(message, targetPathSpec);	
		set(message, targetPathSpec, textToPrepend + targetValue);
	}
	
	
	/**
	 * Clear a field value.  This clears ALL subfields.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @throws HL7Exception
	 */
	public static void clear(Message message, String targetPathSpec) throws Exception {
		Terser terser = new Terser(message);	
			
		terser.set(targetPathSpec, "");
		
		// Clear any subfields. //TODO get the number of sub fields and sub components if possible.  30 subfields and 5 sub components should be OK for now.
		for (int i = 1; i <= 30; i++) {
			for (int j = 1; j <= 5; j++ ) {
			    terser.set(targetPathSpec + "-" + i + "-" + j, "");
			}
		}
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
		Terser terser = new Terser(message);
		
		try {
			AbstractSegment segment = (AbstractSegment)terser.getSegment(sourcePathSpec);

			segment.clear();
		} catch(HL7Exception e ) {
			LOG.warn("Segment to delete does not exist: {}", sourcePathSpec);
		}
		
		// Update the message object with the changes.
		message.parse(message.toString());
	}
	
	
	/**
	 * Concatenate field values.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param seperator
	 * @param sourcePathSpecs
	 */
	public static void concatenate(Message message, String targetPathSpec, String seperator, String ... sourcePathSpecs) throws Exception {
		Terser terser = new Terser(message);
		
		StringBuilder sb = new StringBuilder();
		
		for (String sourcePathSpec : sourcePathSpecs) {
			if (sb.length() > 0) {
				sb.append(seperator);
			}
			
			String sourceFieldValue = terser.get(sourcePathSpec);
			sb.append(sourceFieldValue);
		}
		
		terser.set(targetPathSpec, sb.toString());
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
		Terser terser = new Terser(message);
		
		String sourceText = terser.get(sourcePathSpec);
		
		for (int i = 0; i < sourcePathSpecs.length; i++) {
			String sourceValue = terser.get(sourcePathSpecs[i]);
			
			StringUtils.replace(sourceText, "[" + sourcePathSpecs[i] + "]", sourceValue);
		}
		
		terser.set(targetPathSpec, sourceText);
	}
	
	
	/**
	 * Returns a list of all matching segments.  Please note the segment name is not a path spec.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 */
	public static List<Segment>getAllSegments(Message message, String segmentName) throws Exception {
		Terser terser = new Terser(message);
		
		List<Segment>segments = new ArrayList<>();
		
		SegmentFinder finder = terser.getFinder();
		
		while(true) {
			try {
				String name = finder.iterate(true, false); // iterate segments only.  The first true = segments.
				
				if (name.startsWith(segmentName)) {
					
					for (Structure structure : finder.getCurrentChildReps()) {
						segments.add((Segment)structure);
					}
				}
			} catch(HL7Exception e) {
				break;
			}
		}	
		
		return segments;
	}
}
