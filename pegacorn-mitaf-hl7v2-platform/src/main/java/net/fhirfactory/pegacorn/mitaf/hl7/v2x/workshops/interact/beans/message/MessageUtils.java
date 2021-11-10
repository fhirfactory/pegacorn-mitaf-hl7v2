package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
