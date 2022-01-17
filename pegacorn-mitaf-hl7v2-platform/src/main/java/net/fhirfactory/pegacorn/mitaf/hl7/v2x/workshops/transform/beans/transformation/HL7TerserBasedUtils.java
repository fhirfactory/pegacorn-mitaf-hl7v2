package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * Utilities that parse/query a HL7 document using a HL7 libnary/terser.
 * 
 * @author Brendan Douglas
 *
 */
class HL7TerserBasedUtils {
    private static final Logger LOG = LoggerFactory.getLogger(HL7MessageUtils.class);
    
    
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
		Terser terser = new Terser(message);
		
		Segment segment = terser.getSegment("PID");
		int numberOfRepeitions = segment.getField(3).length;
		
		for (int i = 0; i < numberOfRepeitions; i++) {
			String identifierType = terser.get("/PID-3(" + i + ")-4-1");
			
			if (identifierType != null && identifierType.equals(identifier)) {
				((AbstractSegment)segment).removeRepetition(3, i);
			}
		}
		
		message.parse(message.toString());
	}
	
	
	/**
	 * Returns all 
	 * 
	 * @param message
	 * @param identifierTypes
	 */
	public static String getIdentifierValue(Message message, String identifier) throws Exception  {
		Terser terser = new Terser(message);
		
		Segment segment = terser.getSegment("PID");
		int numberOfRepeitions = segment.getField(3).length;
		
		for (int i = 0; i < numberOfRepeitions; i++) {
			String identifierType = terser.get("/PID-3(" + i + ")-4-1");
			
			if (identifierType != null && identifierType.equals(identifier)) {
				return terser.get("/PID-3(" + i + ")-1-1");
			}
		}
		
		return "";
	}
	
	
	/**
	 * Returns a list of identifiers in the PID segment.
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static List<String> getPatientIdentifierCodes(Message message) throws Exception {
		List<String>identifiers = new ArrayList<>();
		
		Terser terser = new Terser(message);
		
		Segment segment = terser.getSegment("PID");
		int numberOfRepeitions = segment.getField(3).length;
		
		for (int i = 0; i < numberOfRepeitions; i++) {
			String identifier = terser.get("/PID-3(" + i + ")-4-1");
			
			if (identifier != null) {
				identifiers.add(identifier);
			}
		}	
		
		return identifiers;
	}
	
	
	public static boolean isType(Message message, String messageType) throws Exception {
		if (messageType.endsWith("_*")) {
			return message.getName().substring(0, 3).equals(messageType.substring(0, 3));
		}
		
		return message.getName().equals(messageType);
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
	 * Uses a lookup table to change a fields value.
	 * 
	 * @param targetPathSpec
	 * @param lookupTable
	 * @throws HL7Exception
	 */
	public static void lookup(Message message, String targetPathSpec, String lookupTableClassName) throws Exception {	
		
		try {
			Terser terser = new Terser(message);
			
			String existingValue = terser.get(targetPathSpec);
		
			// Use reflection to instantiate the appropriate lookup table class
			Class<?> lookupTableClass = Class.forName(lookupTableClassName);
			Constructor<?> lookupTableConstructor = lookupTableClass.getConstructor();
			LookupTable lookupTable = (LookupTable) lookupTableConstructor.newInstance();
			
			String transformedValue = lookupTable.lookup(existingValue);
			terser.set(targetPathSpec, transformedValue);
		} catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
			LOG.info("Unable to construct lookup class: {} ", lookupTableClassName);
		} 
	}

	
	/**
	 * Calls a Java class to set the target path value.
	 * 
	 * @param targetPathSpec
	 * @param transformationClass
	 */
	public static void updateFieldFromCode(Message message, String targetPathSpec, String fieldTransformationClassName) throws Exception {
		Terser terser = new Terser(message);
		
		try {
			// Use reflection to instantiate the appropriate code transformation class
			Class<?> fieldTransformationClass = Class.forName(fieldTransformationClassName);
			Constructor<?> fieldTransformationClassConstructor = fieldTransformationClass.getConstructor();
			FieldCodeTransformation transformation = (FieldCodeTransformation) fieldTransformationClassConstructor.newInstance();
			
			String transformedValue = transformation.execute(message);
			terser.set(targetPathSpec, transformedValue);
		} catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
			LOG.info("Unable to construct lookup class: {} ", fieldTransformationClassName);
		}
	}

	
	/**
	 * Calls a Java class to set the target path value.
	 * 
	 * @param targetPathSpec
	 * @param transformationClass
	 */
	public static void updateMessageFromCode(Message message, String transformationClassName) throws Exception {
		
		try {
			// Use reflection to instantiate the appropriate code transformation class
			Class<?> transformationClass = Class.forName(transformationClassName);
			Constructor<?> transformationClassConstructor = transformationClass.getConstructor();
			MessageCodeTransformation transformation = (MessageCodeTransformation) transformationClassConstructor.newInstance();
			
			transformation.execute(message);
		} catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
			LOG.info("Unable to construct lookup class: {} ", transformationClassName);
		}
	}

	
	/**
	 * Clear a field value.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @throws HL7Exception
	 */
	public static void clear(Message message, String targetPathSpec) throws Exception {
		Terser terser = new Terser(message);	
		
		String val = terser.get(targetPathSpec);
		System.out.println(val);
			
		terser.set(targetPathSpec, "");
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
		
		AbstractSegment segment = (AbstractSegment)terser.getSegment(sourcePathSpec);
		
		segment.clear();
		
		// Update the message object with the changes.
		message.parse(message.toString());
	}
	
	
	/**
	 * Removes all segments matching the segment name no matter where they appear in the message.  Please note the segment name is not a path spec.
	 * 
	 * @param message
	 * @param segmentName
	 * @throws HL7Exception
	 */
	public static void removeAllSegments(Message message, String segmentName) throws Exception {	
		Terser terser = new Terser(message);
		
		SegmentFinder finder = terser.getFinder();
		
		while(true) {
			try {
				String name = finder.iterate(true, false); // iterate segments only.  The first true = segments.
				
				if (name.startsWith(segmentName)) {

					for (Structure structure : finder.getCurrentChildReps()) {
						AbstractSegment segment = (AbstractSegment)structure;
						segment.clear();
					}
				}
			} catch(HL7Exception e) {
				break;
			}
		}
		
		// Update the message object with the changes.
		message.parse(message.toString());
	}
	
	
	/**
	 * Sets the segments to send.  All other segments are removed.  
	 * 
	 * @param message
	 * @param requiredSegments
	 */
	public static void setSegmentsToKeep(Message message, String ... setSegmentsToKeep) throws Exception {	
		Terser terser = new Terser(message);
		
		SegmentFinder finder = terser.getFinder();
		
		while(true) {
			try {
				String name = finder.iterate(true, false); // iterate segments only.  The first true = segments.
				
				if (!doesContainSegment(message, name, setSegmentsToKeep)) {
					
					for (Structure structure : finder.getCurrentChildReps()) {
						AbstractSegment segment = (AbstractSegment)structure;
						segment.clear();
					}
				}
			} catch(HL7Exception e) {
				break;
			}
		}
		
		// Update the message object with the changes.
		message.parse(message.toString());
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

	
	private static boolean doesContainSegment(Message message, String segment, String[] requiredSegments) {
		for (String requiredSegment : requiredSegments) {
			if (segment.startsWith(requiredSegment)) {
				return true;
			}
		}
		
		return false;
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
	 * Executes an action for each segment which matches the segment name.
	 * 
	 * @param segment
	 * @param action
	 */
	public static void forEachSegment(Message message, String segmentName, String actionClassName) throws Exception {
		try {
			
			// Use reflection to instantiate the appropriate segment action class.
			Class<?> actionClass = Class.forName(actionClassName);
			Constructor<?> actionClassConstructor = actionClass.getConstructor();
			SegmentAction segmentAction = (SegmentAction) actionClassConstructor.newInstance();
			
			for (Segment segment : getAllSegments(message, segmentName)) {
				segmentAction.execute(segment);
			}
		} catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
			LOG.info("Unable to construct lookup class: {} ", actionClassName);
		}	
	}

	
	/**
	 * Executes an action for a single segment.
	 * 
	 * @param segment
	 * @param action
	 */
	public static void segmentAction(Message message, String sourcePathSpec, String actionClassName) throws Exception {
		try {
			
			Terser terser = new Terser(message);
			AbstractSegment segment = (AbstractSegment)terser.getSegment(sourcePathSpec);
			
			// Use reflection to instantiate the appropriate segment action class.
			Class<?> actionClass = Class.forName(actionClassName);
			Constructor<?> actionClassConstructor = actionClass.getConstructor();
			SegmentAction segmentAction = (SegmentAction) actionClassConstructor.newInstance();
			
			segmentAction.execute(segment);
		} catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
			throw new HL7Exception("Unable to construct segment action class", e);
		}		
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
}
