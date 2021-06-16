package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.interact.wup;

import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.HL7V2XTopicFactory;
import net.fhirfactory.pegacorn.workshops.InteractWorkshop;
import net.fhirfactory.pegacorn.wups.archetypes.petasosenabled.messageprocessingbased.InteractEgressMessagingGatewayWUP;

import javax.inject.Inject;

/**
 * Base class for all Mitaf Egress WUPs.
 * 
 * @author Brendan Douglas
 * @author Mark Hunter
 *
 */
public abstract class BaseHL7v2MessageEgressWUP extends InteractEgressMessagingGatewayWUP {

	private String WUP_VERSION = "1.0.0";

	@Inject
	private InteractWorkshop interactWorkshop;

	@Inject
	protected HL7V2XTopicFactory hl7v2xTopicIDBuilder;

	@Inject
	protected MITaFHL7v2GatewayComponentNames mitafComponentNames;

}
