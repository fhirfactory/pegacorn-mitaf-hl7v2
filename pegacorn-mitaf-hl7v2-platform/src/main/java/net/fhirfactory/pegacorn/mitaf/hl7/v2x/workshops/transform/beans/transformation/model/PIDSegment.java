package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A PID segment.
 * 
 * @author Brendan Douglas
 *
 */
public class PIDSegment extends Segment implements Serializable {
	private static final long serialVersionUID = -8797054428191615724L;
	
	
	public PIDSegment(String segment, HL7Message message) {
		super(segment, message);
	}
	

	/**
	 * Removes a patient identifier from the PID segment.
	 * 
	 * @param message
	 * @param identifier
	 * @throws Exception
	 */
	public void removePatientIdentifierField(String identifier) throws Exception  {	
		removeMatchingFieldRepetitions(3, 5, identifier);
	}

	
	/**
	 * Gets a patient identifier value from the PID segment
	 * 
	 * @param identifier
	 * @return
	 * @throws Exception
	 */
	public String getPatientIdentifierValue(String identifier) throws Exception  {	
		FieldRepetition fieldRepetition = this.getFieldRepetitionMatchingValue(3, 5, identifier);
		
		if (fieldRepetition != null) {
			return fieldRepetition.getSubField(1).value();
		}
		
		return "";
	}
	
	
	/**
	 * Returns a list of patient identifiers in the PID segment.
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<String> getPatientIdentifierCodes() throws Exception {	
		
		List<String>identifiers = new ArrayList<>();
		
		for (FieldRepetition fieldRepetition : getField(3).getRepetitions()) {
			identifiers.add(fieldRepetition.getSubField(5).value());
		}
		
		
		return identifiers;
	}
	
	
	/**
	 * Removes patient identifiers which do not match the identifier to keep.
	 * 
	 * @param identifierToKeep
	 * @throws Exception
	 */
	public void removeOtherPatientIdentifierFields( String identifierToKeep) throws Exception  {	
		removeNotMatchingFieldRepetitions(3,5, identifierToKeep);
	}
}
