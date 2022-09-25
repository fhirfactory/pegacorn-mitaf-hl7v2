package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import org.apache.commons.lang3.StringUtils;

/**
 * HL7 Escape Sequence enum.
 * 
 * @author Brendan Douglas
 *
 */
public enum EscapeSequenceEnum {
	FIELD_SEPARATOR("\\F\\","|"),
	SUB_FIELD_SEPARATOR("\\S\\","^"),
	FIELD_REPETITION_SEPARATOR("\\R\\","~"),
	SUB_SUB_FIELD_SEPARATOR("\\T\\","&"),
	SLASH("\\E\\","\\");
	
	
	private String escapeSequence;
	private String character;
	
	EscapeSequenceEnum(String escapeSequence, String character) {
		this.escapeSequence = escapeSequence;
		this.character = character;
	}
	
	
	public String getEscapeSequence() {
		return escapeSequence;
	}
	
	
	public String getCharacter() {
		return character;
	} 
	
	
	/**
	 * Returns the provide text escaped.
	 * 
	 * @param text
	 * @return
	 */
	public static String escape(String text) {
		String escapedText = text;
		
		for (EscapeSequenceEnum escapeSequenceEnum : EscapeSequenceEnum.values()) {
			escapedText = StringUtils.replace(escapedText, escapeSequenceEnum.character, escapeSequenceEnum.escapeSequence);
		}
		
		return escapedText;
	}
	
	
	/**
	 * Returns the provided text unescaped.
	 * 
	 * @param text
	 * @return
	 */
	public static String unescape(String text) {
		String unescapedText = text;
		
		for (EscapeSequenceEnum escapeSequenceEnum : EscapeSequenceEnum.values()) {
			unescapedText = StringUtils.replace(unescapedText, escapeSequenceEnum.escapeSequence, escapeSequenceEnum.character);
		}
		
		return unescapedText;
	}
}