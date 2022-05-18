package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation;

/**
 * 
 */
public class NonExistentHL7ElementException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public NonExistentHL7ElementException(String message) {
        super(message);
    }
}
