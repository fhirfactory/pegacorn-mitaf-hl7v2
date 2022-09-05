package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import org.apache.commons.lang3.StringUtils;

/**
 * HL7 message escape characters.  When a reserved character is used in an HL7 message it needs
 * to be escaped.
 * 
 * @author Brendan Douglas
 *
 */
public enum HL7EscapeCharacterEnum {
	ESCAPE_CHARACTER("\\E\\", "\\"),
	FIELD_SEPARATOR_CHARACTER("\\F\\", "|"),
	SUBFIELD_SEPARATOR_CHARACTER("\\S\\", "^"),
	REPETITION_SEPARATOR_CHARACTER("\\R\\", "~"),
	SUB_SUB_SEPARATOR_CHARACTER("\\T\\", "&");
	
	private String escapeSequence;
	private String convertedCharacter;
	
	HL7EscapeCharacterEnum(String escapeSequence, String convertedCharacter) {
		this.escapeSequence = escapeSequence;
		this.convertedCharacter = convertedCharacter;
	}
	
	public static String replace(String text) {
		for (HL7EscapeCharacterEnum escapeCharacterEnumValue : values()) {
			text =  StringUtils.replace(text,escapeCharacterEnumValue.escapeSequence, escapeCharacterEnumValue.convertedCharacter);
		}
		
		return text;
	}
}
