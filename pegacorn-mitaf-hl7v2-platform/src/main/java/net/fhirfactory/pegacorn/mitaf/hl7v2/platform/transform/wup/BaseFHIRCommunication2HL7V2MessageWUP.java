package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.wup;

import net.fhirfactory.pegacorn.wups.archetypes.petasosenabled.messageprocessingbased.core.MOAStandardWUP;

import javax.inject.Inject;


/**
 * Base class for all Mitaf WUPs to transform FHIR Communication resource to a
 * HL7 v2 message.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseFHIRCommunication2HL7V2MessageWUP extends MOAStandardWUP {
	private String WUP_VERSION = "1.0.0";

	@Inject
	protected HL7V2XTopicIDBuilder hl7v2xTopicIDBuilder;

	@Inject
	protected LadonComponentNames ladonComponentNames;

	@Inject
	protected AETHERCoreSubsystemComponentNames aetherSolutionSystemNames;

	@Override
	protected String specifyWUPVersion() {
		return (WUP_VERSION);
	}

	@Override
	protected String specifyWUPWorkshop() {
		return (DefaultWorkshopSetEnum.TRANSFORM_WORKSHOP.getWorkshop());
	}

	protected abstract BaseCommunication2HL7v2Message getCommunication2HL7V2Message();

	@Override
	public void configure() throws Exception {
		getLogger().info(this.getClass().getName() + ":: ingresFeed() --> {}", this.ingresFeed());
		getLogger().info(this.getClass().getName() + ":: egressFeed() --> {}", this.egressFeed());

		fromWithStandardExceptionHandling(this.ingresFeed()).routeId(getNameSet().getRouteCoreWUP())
		        .bean(getCommunication2HL7V2Message().getClass(), "extractMessage").to(this.egressFeed());

	}
}
