package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans;

/**
 * Is the filter occurring pre or post transformation.  We need to know this so we know 
 * what filter class to use.
 * 
 * @author Brendan Douglas
 *
 */
public enum FilterType {
	PRE_TRANSFORMATION,
	POST_TRANSFORMATION;
}
