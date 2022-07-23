package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import java.io.Serializable;
import java.util.stream.Collectors;

/**
 * An MSH segment.
 * 
 * @author Brendan Douglas
 *
 */
public class MSHSegment extends Segment implements Serializable {
	private static final long serialVersionUID = -8797054428191615724L;
		
	public MSHSegment(String segment, HL7Message message) {
		this.message = message;
		
		String[] splitSegmentFields = segment.split("\\|");
				
		fields.add(new Field(splitSegmentFields[0], true, this));
		
		int startIndex = 1;
			
		startIndex = 2;
		fields.add(new Field("|", false, this));
		fields.add(new Field(splitSegmentFields[1],false, this));

		
		for (int i = startIndex; i < splitSegmentFields.length; i++) {
			String value = splitSegmentFields[i];
			Field field = new Field(value, true, this);
			fields.add(field);
		}
	}
	
	
	@Override
	public String toString() {	
		String segment = fields.stream().skip(3).map(Field::toString).collect(Collectors.joining("|"));
		
		return "MSH|^~\\&|"+segment;
	}
	
	
	/**
	 * Returns the type of the message. MSH-9.
	 * 
	 * @return
	 * @throws Exception
	 */
	public Field getMessageTypeField() throws Exception {
		return this.getField(9);
	}
	
	
	/**
	 * Changes the version of this message.
	 * 
	 * @param newVersion
	 * @throws Exception
	 */
	public void changeMessageVersion(String newVersion) throws Exception {
		getField(12).setValue(newVersion);
	}
}
