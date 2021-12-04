package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation;

/**
 * Lookup a value from a lookup table.
 * 
 * @author Brendan Douglas
 *
 */
public interface LookupTable {
	
	/**
	 * @param value
	 * @return
	 */
	String lookup(String value) throws Exception;
}
