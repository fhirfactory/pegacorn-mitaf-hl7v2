package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.beans;

import java.io.IOException;

import javax.inject.Inject;

import net.fhirfactory.pegacorn.components.dataparcel.DataParcelToken;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration.BaseHL7UpdateConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration.ConfigurationType;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration.ConfigurationUtil;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration.Direction;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.hl7.MitafHL7Message;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.hl7v2.HL7Exception;
import net.fhirfactory.pegacorn.petasos.model.topics.TopicToken;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.util.FHIRContextUtility;

/**
 * 
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseCommunication2HL7v2Message {
	public abstract Logger getLogger();

	@Inject
	protected FHIRContextUtility fhirContextUtility;

	public UoW extractMessage(UoW incomingUoW) throws HL7Exception, IOException {
		getLogger().debug(".convertFHIR2CSV(): Entry, incomingUoW (UoW) --> {}", incomingUoW);
		//
		// First, let's convert from a String to a Bundle
		//
		IParser fhirResourceParser = fhirContextUtility.getJsonParser().setPrettyPrint(true);
		Bundle payloadBundle = fhirResourceParser.parseResource(Bundle.class, incomingUoW.getIngresContent().getPayload());
		Communication communication = null;
		for (Bundle.BundleEntryComponent entry : payloadBundle.getEntry()) {
			if (entry.getResource().getResourceType().equals(ResourceType.Communication)) {
				communication = (Communication) entry.getResource();
				getLogger().trace(".convertFHIR2CSV(): Extracted DocumentReference element from FHIR::Bundle");
				break;
			}
		}
		Communication.CommunicationPayloadComponent communicationPayload = communication.getPayloadFirstRep();
		String message = communicationPayload.getContentStringType().getValue();
		
		// Apply any required transformations to the message.
		transform(message);
		
		UoWPayload payload = new UoWPayload();
		DataParcelToken newToken = incomingUoW.getPayloadTopicID();
		newToken.removeDiscriminator();
		newToken.addDiscriminator("Target", getTarget());
		payload.setPayload(message);
		payload.setPayloadTopicID(newToken);
		incomingUoW.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);
		incomingUoW.getEgressContent().addPayloadElement(payload);
		return (incomingUoW);
	}
	
	
	/**
	 * Transform the HL7 message using the configured transformations.
	 * 
	 * @param message
	 * @throws HL7Exception
	 */
	private void transform(String message) throws HL7Exception, IOException {
		MitafHL7Message hl7Message = new MitafHL7Message(message);
		
		// We now have the message so we can now apply any transformations to the HL7 message.
		BaseHL7UpdateConfiguration config = (BaseHL7UpdateConfiguration) ConfigurationUtil.getConfiguration(ConfigurationType.UPDATE_HL7, getConfigurationLocation(), Direction.EGRES, hl7Message.getMessage().getName());
		
		hl7Message.update(config);
		message = hl7Message.toString();	
	}
	

	public abstract String getTarget();
	
	
	protected abstract String getConfigurationLocation();
}
