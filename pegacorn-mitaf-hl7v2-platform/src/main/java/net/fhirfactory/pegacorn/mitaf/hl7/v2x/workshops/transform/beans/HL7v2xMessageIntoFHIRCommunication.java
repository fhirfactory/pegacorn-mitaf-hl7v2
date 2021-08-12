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

import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.HL7V2XTopicFactory;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.communication.extensions.CommunicationPayloadTypeExtensionEnricher;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.communication.factories.CommunicationFactory;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.interfaces.HL7v2xInformationExtractionInterface;
import net.fhirfactory.pegacorn.petasos.model.configuration.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;
import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;

@ApplicationScoped
public class HL7v2xMessageIntoFHIRCommunication {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xMessageIntoFHIRCommunication.class);

    protected Logger getLogger(){return(LOG);}

    @Inject
    private CommunicationFactory communicationFactory;

    @Inject
    private HL7v2xInformationExtractionInterface messageInformationExtractionInterface;

    @Inject
    private CommunicationPayloadTypeExtensionEnricher payloadTypeExtensionEnricher;

    @Inject
    private HL7V2XTopicFactory hl7v2TopicFactory;

    public Communication encapsulateMessage(Message message, Exchange exchange){
        getLogger().debug(".encapsulateMessage(): Entry, message->{}", message);
        if(message == null){
            getLogger().error(".encapsulateMessage(): Exit, message is null!");
        }
        String messageID = messageInformationExtractionInterface.extractMessageID(message);
        Date messageDate = messageInformationExtractionInterface.extractMessageDate(message);
        String source = messageInformationExtractionInterface.extractMessageSource(message);


        getLogger().trace(".buildDefaultCommunicationMessage(): Add Id value (from the m.room.message::event_id");
        Communication newCommunication = communicationFactory.newCommunicationResource(messageID, messageDate);
        newCommunication.setStatus(Communication.CommunicationStatus.COMPLETED);
        getLogger().trace("buildDefaultCommunicationMessage(): Set the FHIR::Communication.CommunicationPriority to ROUTINE (we make no distinction - all are real-time)");
        newCommunication.setPriority(Communication.CommunicationPriority.ROUTINE);
        //
        // Add payload
        //
        Communication.CommunicationPayloadComponent payload = new Communication.CommunicationPayloadComponent();
        String messageAsTxt = messageInformationExtractionInterface.convertMessageToString(message);
        StringType messageStringType = new StringType(messageAsTxt);
        payload.setContent(messageStringType);
        String messageType = messageInformationExtractionInterface.extractMessageType(message);
        String messageTrigger = messageInformationExtractionInterface.extractMessageTrigger(message);
        String messageVersion = messageInformationExtractionInterface.extractMessageVersion(message);
        DataParcelTypeDescriptor parcelTypeDescriptor = hl7v2TopicFactory.newDataParcelDescriptor(messageType, messageTrigger, messageVersion);

        UoW uowFromExchange = exchange.getProperty(PetasosPropertyConstants.WUP_CURRENT_UOW_EXCHANGE_PROPERTY_NAME, UoW.class);
        DataParcelManifest manifestFromUoW = uowFromExchange.getPayloadTopicID();
        DataParcelTypeDescriptor descriptorFromUoW = manifestFromUoW.getContentDescriptor();
        if(descriptorFromUoW.hasDataParcelDiscriminatorType()){
            parcelTypeDescriptor.setDataParcelDiscriminatorType(SerializationUtils.clone(descriptorFromUoW.getDataParcelDiscriminatorType()));
        }
        if(descriptorFromUoW.hasDataParcelDiscriminatorValue()){
            parcelTypeDescriptor.setDataParcelDiscriminatorValue(SerializationUtils.clone(descriptorFromUoW.getDataParcelDiscriminatorValue()));
        }
        payloadTypeExtensionEnricher.injectPayloadTypeExtension(payload,parcelTypeDescriptor );
        newCommunication.getPayload().add(payload);
        getLogger().debug(".encapsulateMessage(): Exit, newCommunication->{}", newCommunication);
        return (newCommunication);
    }
}
