package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

/**
 * Utils used in the transformation and filtering of HL7 messages.
 * 
 * @author Brendan Douglas
 *
 */
public class MessageUtils {
	
	/**
	 * Returns true if a segment exists in a messahe, otherwise false.
	 * 
	 * @param message
	 * @param segment
	 * @return
	 */
	public static boolean doesSegmentExist(Message message, String segment) {
		 String regex = segment + "\\|";
		 Pattern pattern = Pattern.compile(regex);
		 Matcher matcher = pattern.matcher(message.toString());
		 
		 return matcher.find();
	}
	
	
	/**
	 * Removes all segments which are not in the required segment list
	 * 
	 * @param message
	 * @param requiredSegments
	 * @throws HL7Exception
	 */
	public static void removeNotRequiredSegments(Message message, List<String> requiredSegments) throws HL7Exception {		
		String segments[] = message.toString().split("\r");
		
		List<Integer>indexesToKeep = new ArrayList<>();
		
		// If a segment needs to be kept adds its array index to the list of indexes to keep.
		for (int i = 0; i < segments.length; i++) {
			String segment = segments[i];
			
			for (String requiredSegment : requiredSegments) {
				if (segment.startsWith(requiredSegment)) {
					indexesToKeep.add(i);
					break;
				}
			}
		}
		
		
		// Conntruct a new message from the array elements which have an index in the to keep list.
		StringBuilder sb = new StringBuilder();
		
		for (Integer indexToKeep : indexesToKeep) {
			if (sb.length() > 0) {
				sb.append("\r");
			}
				
			sb.append(segments[indexToKeep]);
		}
		
		message.parse(sb.toString());
	}
}
