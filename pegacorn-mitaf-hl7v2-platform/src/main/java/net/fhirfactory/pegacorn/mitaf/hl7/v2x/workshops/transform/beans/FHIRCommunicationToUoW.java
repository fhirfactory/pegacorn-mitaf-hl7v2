/*
 * Copyright (c) 2021 ACT Health
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans;

import ca.uhn.fhir.parser.IParser;
import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.constants.systemwide.PegacornReferenceProperties;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelDirectionEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.PolicyEnforcementPointApprovalStatusEnum;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.FHIRElementTopicFactory;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.communication.extensions.CommunicationPayloadTypeExtensionEnricher;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
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
        UoW uowFromExchange = camelExchange.getProperty(PetasosPropertyConstants.WUP_CURRENT_UOW_EXCHANGE_PROPERTY_NAME, UoW.class);
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
        manifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INFORMATION_FLOW_INBOUND_DATA_PARCEL);
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
