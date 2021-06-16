package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.wup;


import net.fhirfactory.pegacorn.wups.archetypes.petasosenabled.messageprocessingbased.core.MOAStandardWUP;

/**
 * Base class for all Mitaf WUPs to transform HL7 v2 messages to a FHIR
 * Communication resource.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL7V2Message2FHIRCommunicationWUP extends MOAStandardWUP {
	private String WUP_VERSION = "1.0.0";

}
