package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans;

import ca.uhn.fhir.parser.IParser;
import net.fhirfactory.pegacorn.components.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.components.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.components.dataparcel.valuesets.DataParcelDirectionEnum;
import net.fhirfactory.pegacorn.components.dataparcel.valuesets.PolicyEnforcementPointApprovalStatusEnum;
import net.fhirfactory.pegacorn.internals.PegacornReferenceProperties;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.FHIRElementTopicFactory;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.communication.extensions.CommunicationPayloadTypeExtensionEnricher;
import net.fhirfactory.pegacorn.petasos.model.configuration.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.petasos.model.task.PetasosTaskOld;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.util.FHIRContextUtility;
import org.apache.camel.Exchange;
import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class FHIRCommunicationToUoW {
    private static final Logger LOG = LoggerFactory.getLogger(FHIRCommunicationToUoW.class);

    private IParser fhirParser;

    @PostConstruct
    public void initialise(){
        fhirParser = fhirContextUtility.getJsonParser().setPrettyPrint(true);
    }

    @Inject
    private FHIRContextUtility fhirContextUtility;

    @Inject
    private FHIRElementTopicFactory fhirTopicFactory;

    @Inject
    private PegacornReferenceProperties pegacornReferenceProperties;

    @Inject
    private CommunicationPayloadTypeExtensionEnricher payloadTypeExtensionEnricher;

    public UoW packageCommunicationResource(Communication communication, Exchange camelExchange){
        LOG.debug(".packageCommunicationResource(): Entry");
        PetasosTaskOld wupTransportPacket = camelExchange.getProperty(PetasosPropertyConstants.WUP_TRANSPORT_PACKET_EXCHANGE_PROPERTY_NAME, PetasosTaskOld.class);
        UoW uowFromExchange = wupTransportPacket.getPayload();
        LOG.trace(".packageCommunicationResource(): Converting communication (FHIR::Communication) to a JSON String");
        String communicationAsString = fhirParser.encodeResourceToString(communication);
        LOG.trace(".packageCommunicationResource(): Generating a new DataParcelManifest from the communication (FHIR::Communication) object");
        DataParcelManifest manifest = new DataParcelManifest();
        DataParcelTypeDescriptor parcelContainerDescriptor = fhirTopicFactory.newTopicToken(ResourceType.Communication.name(), pegacornReferenceProperties.getPegacornDefaultFHIRVersion());
        LOG.trace(".packageCommunicationResource(): Extracting content type (Extension) from the Communication Payload");
        DataParcelTypeDescriptor parcelContentDescriptor = payloadTypeExtensionEnricher.extractPayloadTypeExtension(communication.getPayloadFirstRep());
        LOG.trace(".packageCommunicationResource(): Setting the Manifest details");
        manifest.setContainerDescriptor(parcelContainerDescriptor);
        manifest.setContentDescriptor(parcelContentDescriptor);
        manifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INBOUND_DATA_PARCEL);
        manifest.setEnforcementPointApprovalStatus(PolicyEnforcementPointApprovalStatusEnum.POLICY_ENFORCEMENT_POINT_APPROVAL_NEGATIVE);
        manifest.setInterSubsystemDistributable(true);
        manifest.setIntendedTargetSystem(uowFromExchange.getPayloadTopicID().getIntendedTargetSystem());
        manifest.setSourceSystem(uowFromExchange.getPayloadTopicID().getSourceSystem());
        LOG.trace(".packageCommunicationResource(): Inserting details into the UoW");
        UoWPayload egressPayload = new UoWPayload();
        egressPayload.setPayload(communicationAsString);
        egressPayload.setPayloadManifest(manifest);
        uowFromExchange.getEgressContent().addPayloadElement(egressPayload);
        LOG.trace(".packageCommunicationResource(): Setting the 'Success' outcome");
        uowFromExchange.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);
        LOG.debug(".packageCommunicationResource(): Exit, uow->{}", uowFromExchange);
        return(uowFromExchange);
    }
}
