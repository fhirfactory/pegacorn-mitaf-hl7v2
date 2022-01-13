/*
 * Copyright (c) 2021 Mark A. Hunter
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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.interfaces.oam.notifications.PetasosITOpsNotificationBrokerInterface;
import net.fhirfactory.pegacorn.core.interfaces.topology.ProcessingPlantInterface;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelDirectionEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelNormalisationStatusEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelValidationStatusEnum;
import net.fhirfactory.pegacorn.core.model.petasos.participant.ProcessingPlantPetasosParticipantNameHolder;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.HL7V2XTopicFactory;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.model.HL7v2VersionEnum;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.HL7MessageUtils;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgent;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgentAccessor;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.WorkUnitProcessorMetricsAgent;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class HL7v2MessageEncapsulator {

    private HapiContext context;
    private DateTimeFormatter timeFormatter;

    @Inject
    private HL7V2XTopicFactory topicFactory;

    @Inject
    private ProcessingPlantInterface processingPlant;

    @Inject
    private ProcessingPlantPetasosParticipantNameHolder participantNameHolder;

    @Inject
    private ProcessingPlantMetricsAgentAccessor processingPlantMetricsAgentAccessor;

    //
    // Constructor(s)
    //

    public HL7v2MessageEncapsulator() {
        context = new DefaultHapiContext();
        timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").withZone(ZoneId.of(PetasosPropertyConstants.DEFAULT_TIMEZONE));
    }

    //
    // Abstract methods
    //

    public abstract boolean triggerIsSupported(String trigger);
    public abstract HL7v2VersionEnum getSupportedVersion();
    public abstract DataParcelTypeDescriptor createDataParcelTypeDescriptor(String messageEventType, String messageTriggerEvent);
    protected abstract Logger specifyLogger();

    //
    // Getters (and Setters)
    //

    protected Logger getLogger(){
        return(specifyLogger());
    }

    protected HL7V2XTopicFactory getTopicFactory(){
        return(topicFactory);
    }

    protected DateTimeFormatter getTimeFormatter(){
        return(timeFormatter);
    }

    //
    // Business Functions
    //

    public UoW encapsulateMessage(Message message, Exchange exchange, String sourceSystem, String intendedTargetSystem, String parcelDiscriminatorType, String parcelDiscriminatorValue){
        getLogger().debug(".encapsulateMessage(): Entry, message --> {}", message.toString());

        //
        // Because auditing is not running yet
        // Remove once Auditing is in place
        //
        //getLogger().info("IncomingMessage-----------------------------------------------------------------");
        getLogger().warn("IncomingMessage->{}", message); // Log at WARN level so always seen in TEST
        //getLogger().info("IncomingMessage-----------------------------------------------------------------");
        //
        //
        //

        //
        // add to Processing Plant metrics
        getMetricsAgent().incrementIngresMessageCount();

        //
        // add to WUP Metrics
        WorkUnitProcessorMetricsAgent metricsAgent = exchange.getProperty(PetasosPropertyConstants.WUP_METRICS_AGENT_EXCHANGE_PROPERTY, WorkUnitProcessorMetricsAgent.class);
        metricsAgent.incrementIngresMessageCount();
        metricsAgent.touchLastActivityInstant();



        try {
            getLogger().trace(".encapsulateMessage(): Extracting header details" );
            String messageTriggerEvent = exchange.getMessage().getHeader("CamelMllpTriggerEvent", String.class);
            getLogger().trace(".encapsulateMessage(): message::messageTriggerEvent --> {}", messageTriggerEvent);
            String messageEventType = exchange.getMessage().getHeader("CamelMllpEventType", String.class);
            getLogger().trace(".encapsulateMessage(): message::messageEventType --> {}", messageEventType);
            String messageVersion = exchange.getMessage().getHeader("CamelMllpVersionId", String.class);
            getLogger().trace(".encapsulateMessage(): message::MessageVersion --> {}", messageVersion);
            String messageTimeStamp = exchange.getMessage().getHeader("CamelMllpTimestamp", String.class);
            String portValue = exchange.getProperty(PetasosPropertyConstants.WUP_INTERACT_PORT_VALUE, String.class);

            //
            // Add some notifications
            String notificationContent;
            try{
                List<Segment> messageHeaders = HL7MessageUtils.getAllSegments(message, "MSH");
                List<Segment> pidSegments = HL7MessageUtils.getAllSegments(message, "PID");
                String messageHeaderSegment = messageHeaders.get(0).encode();
                String pidSegment = "No PID Segment";
                if(!pidSegments.isEmpty()) {
                    pidSegment = pidSegments.get(0).encode();
                }
                notificationContent = "---" + "\n" +
                        "*MLLP Receiver*" + "\n" +
                        "Message Received (" + getTimeFormatter().format(Instant.now()) + ")" + "\n" +
                        messageHeaderSegment + "\n" +
                        pidSegment + "\n" +
                        "---";
            } catch (Exception encodingException) {
                notificationContent = "Received MLLP Message --> " + messageEventType + "^" + messageTriggerEvent + "(" + messageVersion + "): Timestamp->" + messageTimeStamp;
            }
            metricsAgent.sendITOpsNotification(notificationContent);

            //
            // Now actually process the UoW/Message
            UoWProcessingOutcomeEnum outcomeEnum;
            String outcomeDescription;
            if(messageVersion.equalsIgnoreCase(getSupportedVersion().getVersionText())){
                outcomeEnum = UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS;
                outcomeDescription = "All Good!";
            } else {
                outcomeEnum = UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED;
                outcomeDescription = "Wrong Version of Message, expected ("+getSupportedVersion().getVersionText()+"), got ("+messageVersion+")!";
                getLogger().info(".encapsulateMessage(): " + outcomeDescription);
            }
            getLogger().trace(".encapsulateMessage(): message::messageVersion --> {}", messageVersion );
//            String stringMessage = message.encode();
            message.getParser().getParserConfiguration().setValidating(false);
//            message.getParser().getParserConfiguration().setEncodeEmptyMandatoryFirstSegments(true);
//            getLogger().info(".encapsulateMessage(): Structure --> {}", message);
            getLogger().trace(".encapsulateMessage(): Attempting to decode!");
            String encodedString = message.encode();
            getLogger().trace(".encapsulateMessage(): Decoded, encodedString --> {}", encodedString);
            getLogger().trace(".encapsulateMessage(): Creating Data Parcel Descriptor (messageDescriptor)");
            DataParcelTypeDescriptor messageDescriptor = createDataParcelTypeDescriptor(messageEventType, messageTriggerEvent );
            if(!StringUtils.isEmpty(parcelDiscriminatorType)) {
                messageDescriptor.setDataParcelDiscriminatorType(parcelDiscriminatorType);
            }
            if(!StringUtils.isEmpty(parcelDiscriminatorValue)){
                messageDescriptor.setDataParcelDiscriminatorValue(parcelDiscriminatorValue);
            }
            getLogger().trace(".encapsulateMessage(): messageDescriptor created->{}", messageDescriptor);
            getLogger().trace(".encapsulateMessage(): Creating Data Parcel Manifest (messageManifest)");
            DataParcelManifest messageManifest = new DataParcelManifest();
            messageManifest.setContentDescriptor(messageDescriptor);
            if(!StringUtils.isEmpty(sourceSystem)) {
                if(sourceSystem.contentEquals(DataParcelManifest.WILDCARD_CHARACTER)){
                    messageManifest.setSourceSystem(null);
                } else {
                    messageManifest.setSourceSystem(sourceSystem);
                }
            }
            if(!StringUtils.isEmpty(intendedTargetSystem)) {
                if(intendedTargetSystem.contentEquals(DataParcelManifest.WILDCARD_CHARACTER)){
                    messageManifest.setSourceSystem(null);
                } else {
                    messageManifest.setIntendedTargetSystem(intendedTargetSystem);
                }
            }
            messageManifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_FALSE);
            messageManifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATED_FALSE);
            messageManifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INFORMATION_FLOW_INBOUND_DATA_PARCEL);
            messageManifest.setSourceProcessingPlantParticipantName(participantNameHolder.getSubsystemParticipantName());
            getLogger().trace(".encapsulateMessage(): messageManifest created->{}", messageManifest);
            getLogger().trace(".encapsulateMessage(): Creating Egress Payload (newPayload)");
            UoWPayload newPayload = new UoWPayload();
            newPayload.setPayload(encodedString);
            newPayload.setPayloadManifest(messageManifest);
            getLogger().trace(".encapsulateMessage(): newPayload created->{}", newPayload);
            String sourceId = processingPlant.getSubsystemParticipantName() + ":" + portValue + ":" + messageEventType + "-" + messageTriggerEvent ;
            String transactionId = messageEventType + "-" + messageTriggerEvent + "-" + messageTimeStamp;
            getLogger().trace(".encapsulateMessage(): creating a new Unit of Work (newUoW)");
            UoW newUoW = new UoW(newPayload);
            newUoW.getEgressContent().addPayloadElement(newPayload);
            newUoW.setProcessingOutcome(outcomeEnum);
            newUoW.setFailureDescription(outcomeDescription);
            getLogger().debug(".encapsulateMessage(): Exit, newUoW created ->{}", newUoW);
            return(newUoW);
        } catch (Exception ex) {
            getLogger().warn(".encapsulateMessage(): Exception occurred", ex);
            UoWPayload newPayload = new UoWPayload();
            if(message != null){
                newPayload.setPayload(message.toString());
                newPayload.setPayloadManifest(topicFactory.newBadDataParcelManifest());
            } else {
                newPayload.setPayload("Unable to decipher input message");
                newPayload.setPayloadManifest(topicFactory.newBadDataParcelManifest());
            }
            UoW newUoW = new UoW();
            newUoW.setIngresContent(newPayload);
            newUoW.setFailureDescription(ex.toString());
            newUoW.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED);
            getLogger().debug(".encapsulateMessage(): Exit, newUoW created ->{}", newUoW);
            return(newUoW);
        }
    }

    //
    // Getters (and Setters)
    //

    protected ProcessingPlantMetricsAgent getMetricsAgent(){
        return(processingPlantMetricsAgentAccessor.getMetricsAgent());
    }
}
