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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import net.fhirfactory.pegacor.internals.hl7v2.helpers.HL7v2xMessageInformationExtractor;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.HL7V2XTopicFactory;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
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
import net.fhirfactory.pegacorn.core.model.topology.endpoints.base.IPCTopologyEndpoint;
import net.fhirfactory.pegacorn.core.model.topology.nodes.WorkUnitProcessorSoftwareComponent;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.HL7MessageUtils;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.EndpointMetricsAgent;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgent;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgentAccessor;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.WorkUnitProcessorMetricsAgent;

@ApplicationScoped
public class HL7v2xMessageEncapsulator  {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xMessageEncapsulator.class);

    private HapiContext context;
    private DateTimeFormatter timeFormatter;
    private boolean initialised;
    private boolean includeFullHL7MessageInLog;
    private Integer maxHL7MessageSize;

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

    public HL7v2xMessageEncapsulator() {
        context = new DefaultHapiContext();
        timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.of(PetasosPropertyConstants.DEFAULT_TIMEZONE));
        maxHL7MessageSize = 64000;
        includeFullHL7MessageInLog = false;
        initialised = false;
    }

    //
    // Post Construct
    //

    @PostConstruct
    public void initialise(){
        getLogger().debug(".initialise(): Entry");
        if(initialised){
            getLogger().debug(".initialise(): Nothing to do, already initialised!");
        } else {
            getLogger().info(".initialise(): Start");
            getLogger().info(".initialise(): [Check if Full HL7 Message to be included in Log] Start");
            String includeMessageString  = getProcessingPlant().getMeAsASoftwareComponent().getOtherConfigurationParameter(PetasosPropertyConstants.INCLUDE_FULL_HL7_MESSAGE_IN_LOG);
            if(StringUtils.isNotEmpty(includeMessageString)){
                if(includeMessageString.equalsIgnoreCase("true")){
                    setIncludeFullHL7MessageInLog(true);
                }
            }
            getLogger().info(".initialise(): [Check if Full HL7 Message to be included in Log] include->{}", isIncludeFullHL7MessageInLog());
            getLogger().info(".initialise(): [Check if Full HL7 Message to be included in Log] Finish");
            getLogger().info(".initialise(): [Check Size Of HL7 Message to be included in Log] Start");
            String messageMaximumSize  = getProcessingPlant().getMeAsASoftwareComponent().getOtherConfigurationParameter(PetasosPropertyConstants.MAXIMUM_HL7_MESSAGE_SIZE_IN_LOG);
            if(StringUtils.isNotEmpty(messageMaximumSize)){
                Integer messageMaxSize = Integer.getInteger(messageMaximumSize);
                if(messageMaxSize != null){
                    setMaxHL7MessageSize(messageMaxSize);
                }
            }
            getLogger().info(".initialise(): [Check Size Of HL7 Message to be included in Log] MaximumSize->{}", getMaxHL7MessageSize());
            getLogger().info(".initialise(): [Check Size Of HL7 Message to be included in Log] Finish");
            getLogger().info(".initialise(): Finish");
            this.initialised = true;
        }
        getLogger().debug(".initialise(): Exit");
    }


    //
    // Getters (and Setters)
    //

    protected HL7V2XTopicFactory getTopicFactory(){
        return(topicFactory);
    }

    protected DateTimeFormatter getTimeFormatter(){
        return(timeFormatter);
    }

    protected Logger getLogger(){
        return(LOG);
    }

    protected ProcessingPlantInterface getProcessingPlant(){
        return(processingPlant);
    }

    protected boolean isIncludeFullHL7MessageInLog() {
        return includeFullHL7MessageInLog;
    }

    protected void setIncludeFullHL7MessageInLog(boolean includeFullHL7MessageInLog) {
        this.includeFullHL7MessageInLog = includeFullHL7MessageInLog;
    }

    protected Integer getMaxHL7MessageSize() {
        return maxHL7MessageSize;
    }

    protected void setMaxHL7MessageSize(Integer maxHL7MessageSize) {
        this.maxHL7MessageSize = maxHL7MessageSize;
    }

    //
    // Business Functions
    //


    public UoW encapsulateMessage(String hl7MessageString, Exchange exchange, String sourceSystem, String intendedTargetSystem, String parcelDiscriminatorType, String parcelDiscriminatorValue,
            String messageTriggerEvent, String messageEventType, String messageVersion, String messageTimestamp) {

        HL7v2xMessageInformationExtractor extractor = new HL7v2xMessageInformationExtractor();
        Message message = extractor.convertToHL7v2Message(hl7MessageString);

        return this.encapsulateMessage(message, exchange, sourceSystem, intendedTargetSystem, parcelDiscriminatorType, parcelDiscriminatorValue, messageTriggerEvent, messageEventType, messageVersion, messageTimestamp);
    }

    public UoW encapsulateMessage(Message message, Exchange exchange, String sourceSystem, String intendedTargetSystem, String parcelDiscriminatorType, String parcelDiscriminatorValue) {
        LOG.trace(".encapsulateMessage(): Extracting header details");
        String messageTriggerEvent = exchange.getMessage().getHeader("CamelMllpTriggerEvent", String.class);
        LOG.trace(".encapsulateMessage(): message::messageTriggerEvent --> {}", messageTriggerEvent);
        String messageEventType = exchange.getMessage().getHeader("CamelMllpEventType", String.class);
        LOG.trace(".encapsulateMessage(): message::messageEventType --> {}", messageEventType);
        String messageVersion = exchange.getMessage().getHeader("CamelMllpVersionId", String.class);
        LOG.trace(".encapsulateMessage(): message::MessageVersion --> {}", messageVersion);
        String messageTimeStamp = exchange.getMessage().getHeader("CamelMllpTimestamp", String.class);

        return this.encapsulateMessage(message, exchange, sourceSystem, intendedTargetSystem, parcelDiscriminatorType, parcelDiscriminatorValue, messageTriggerEvent, messageEventType, messageVersion, messageTimeStamp);
    }

    private UoW encapsulateMessage(Message message, Exchange exchange, String sourceSystem, String intendedTargetSystem, String parcelDiscriminatorType, String parcelDiscriminatorValue,
            String messageTriggerEvent, String messageEventType, String messageVersion, String messageTimestamp) {

        LOG.debug(".encapsulateMessage(): Entry, message->{}", message);

        //
        // add to Processing Plant metrics
        getProcessingPlantMetricsAgent().incrementIngresMessageCount();

        //
        // add to WUP Metrics
        WorkUnitProcessorMetricsAgent metricsAgent = exchange.getProperty(PetasosPropertyConstants.WUP_METRICS_AGENT_EXCHANGE_PROPERTY, WorkUnitProcessorMetricsAgent.class);
        metricsAgent.incrementIngresMessageCount();
        metricsAgent.touchLastActivityInstant();
        //
        // Add to Endpoint Metrics
        EndpointMetricsAgent endpointMetricsAgent = exchange.getProperty(PetasosPropertyConstants.ENDPOINT_METRICS_AGENT_EXCHANGE_PROPERTY, EndpointMetricsAgent.class);
        endpointMetricsAgent.incrementIngresMessageCount();
        endpointMetricsAgent.touchLastActivityInstant();


        try {
            String portValue = exchange.getProperty(PetasosPropertyConstants.ENDPOINT_PORT_VALUE, String.class);

            //
            // Add some notifications
            String targetPort = exchange.getProperty(PetasosPropertyConstants.ENDPOINT_PORT_VALUE, String.class);
            String notificationContent;


            String mshSegment = null;
            String pidSegment = null;
            try{
                List<Segment> messageHeaders = HL7MessageUtils.getAllSegments(message, "MSH");
                List<Segment> pidSegments = HL7MessageUtils.getAllSegments(message, "PID");
                mshSegment = messageHeaders.get(0).encode();
                pidSegment = "No PID Segment";
                if(!pidSegments.isEmpty()) {
                    pidSegment = pidSegments.get(0).encode();
                }
            } catch (Exception encodingException) {
                mshSegment = "MSH: unknown";
                pidSegment = "PID: Unknown";
            }

            String portDescription = null;
            try{
                WorkUnitProcessorSoftwareComponent workUnitProcessorSoftwareComponent = exchange.getProperty(PetasosPropertyConstants.WUP_TOPOLOGY_NODE_EXCHANGE_PROPERTY_NAME, WorkUnitProcessorSoftwareComponent.class);
                IPCTopologyEndpoint ingresEndpoint = workUnitProcessorSoftwareComponent.getIngresEndpoint();
                portDescription = ingresEndpoint.getParticipantDisplayName();
            } catch(Exception ex){
                if(StringUtils.isNotEmpty(targetPort)){
                    portDescription = "ServerPort:" + targetPort;
                } else{
                    portDescription = "ServerPort: Unknown";
                }
            }

            //
            // Send ITOps Console Notification about Message Arrival
            sendMessageReceivedConsoleNotification(portDescription, message, mshSegment, pidSegment, endpointMetricsAgent);

            //
            // Now actually process the UoW/Message
            UoWProcessingOutcomeEnum outcomeEnum;
            String outcomeDescription;

            //
            // Only use the first version subfield.
            String messageVersionFirstField = messageVersion;
            int indexOfFieldSeperator = messageVersion.indexOf("^");

            if (indexOfFieldSeperator != -1) {
                messageVersionFirstField = messageVersion.substring(0, indexOfFieldSeperator);
            }

            outcomeEnum = UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS;
            outcomeDescription = "All Good!";
            message.getParser().getParserConfiguration().setValidating(false);
            LOG.trace(".encapsulateMessage(): Attempting to decode!");
            String encodedString = message.encode();
            LOG.trace(".encapsulateMessage(): Decoded, encodedString --> {}", encodedString);

            //
            // Create the DataParcelManifest for the message
            LOG.trace(".encapsulateMessage(): Creating Data Parcel Descriptor (messageDescriptor)");
            DataParcelTypeDescriptor messageDescriptor = createDataParcelTypeDescriptor(messageEventType, messageTriggerEvent, messageVersionFirstField );
            if(!StringUtils.isEmpty(parcelDiscriminatorType)) {
                messageDescriptor.setDataParcelDiscriminatorType(parcelDiscriminatorType);
            }
            if(!StringUtils.isEmpty(parcelDiscriminatorValue)){
                messageDescriptor.setDataParcelDiscriminatorValue(parcelDiscriminatorValue);
            }
            LOG.trace(".encapsulateMessage(): messageDescriptor created->{}", messageDescriptor);
            LOG.trace(".encapsulateMessage(): Creating Data Parcel Manifest (messageManifest)");
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
            messageManifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATED_TRUE);
            messageManifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INFORMATION_FLOW_INBOUND_DATA_PARCEL);
            messageManifest.setSourceProcessingPlantParticipantName(participantNameHolder.getSubsystemParticipantName());
            LOG.trace(".encapsulateMessage(): messageManifest created->{}", messageManifest);

            //
            // Populate the UoWPayload
            LOG.trace(".encapsulateMessage(): Creating Egress Payload (newPayload)");
            UoWPayload newPayload = new UoWPayload();
            newPayload.setPayload(encodedString);
            newPayload.setPayloadManifest(messageManifest);
            LOG.trace(".encapsulateMessage(): newPayload created->{}", newPayload);
            String sourceId = processingPlant.getSubsystemParticipantName() + ":" + portValue + ":" + messageEventType + "-" + messageTriggerEvent ;
            String transactionId = messageEventType + "-" + messageTriggerEvent + "-" + messageTimestamp;

            //
            // Create the UoW
            LOG.trace(".encapsulateMessage(): creating a new Unit of Work (newUoW)");
            UoW newUoW = new UoW(newPayload);
            newUoW.getEgressContent().addPayloadElement(newPayload);
            newUoW.setProcessingOutcome(outcomeEnum);
            newUoW.setFailureDescription(outcomeDescription);

            //
            // All Done!
            LOG.debug(".encapsulateMessage(): Exit, newUoW created ->{}", newUoW);
            return(newUoW);
        } catch (Exception ex) {
            LOG.warn(".encapsulateMessage(): Exception occurred", ex);
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
            LOG.debug(".encapsulateMessage(): Exit, newUoW created ->{}", newUoW);
            return(newUoW);
        }
    }

    protected void sendMessageReceivedConsoleNotification(String portDescription, Message message, String mshSegment, String pidSegment, EndpointMetricsAgent endpointMetricsAgent){
        getLogger().debug(".sendMessageReceivedConsoleNotification(): Entry, portDescription->{}, message->{}", portDescription, message);
        StringBuilder notificationContentBuilder = new StringBuilder();
        notificationContentBuilder.append("Ingres-Message((" + portDescription +")(" + getTimeFormatter().format(Instant.now()) + ")){");
        if(includeFullHL7MessageInLog){
            String displayedMessage = null;
            try {
                String actualMessage = message.encode();

                if (actualMessage.length() > getMaxHL7MessageSize()) {
                    displayedMessage = actualMessage.substring(0, getMaxHL7MessageSize());
                } else {
                    displayedMessage = actualMessage;
                }
            } catch (Exception ex){
                displayedMessage = "Cannot Parse Message Content";
                getLogger().warn(".sendMessageReceivedConsoleNotification(): Cannot parse message, error->{}, stackTrace->{}", ExceptionUtils.getMessage(ex), ExceptionUtils.getStackTrace(ex));
            }
            notificationContentBuilder.append(displayedMessage);
        } else {
            notificationContentBuilder.append(mshSegment);
            notificationContentBuilder.append(" ::: ");
            notificationContentBuilder.append(pidSegment);
        }
        notificationContentBuilder.append("}");

        StringBuilder formattedNotificationContent = new StringBuilder();
        formattedNotificationContent.append("<table>");
        formattedNotificationContent.append("<tr>");
        formattedNotificationContent.append("<th> From </th><th>" + portDescription + " ("+ getTimeFormatter().format(Instant.now()) + ") </th");
        formattedNotificationContent.append("</tr>");
        formattedNotificationContent.append("<tr>");
        formattedNotificationContent.append("<td> Metadata </td><td>" + mshSegment + "\n" + pidSegment + "</td>");
        formattedNotificationContent.append("</tr>");
        formattedNotificationContent.append("</table>");

        endpointMetricsAgent.sendITOpsNotification(notificationContentBuilder.toString(),formattedNotificationContent.toString());

        getLogger().debug(".sendMessageReceivedConsoleNotification(): Exit");
    }

    //
    // Getters (and Setters)
    //

    protected ProcessingPlantMetricsAgent getProcessingPlantMetricsAgent(){
        return(processingPlantMetricsAgentAccessor.getMetricsAgent());
    }


    public boolean triggerIsSupported(String trigger) {
        return true;
    }


    public DataParcelTypeDescriptor createDataParcelTypeDescriptor(String messageEventType, String messageTriggerEvent, String version) {
        DataParcelTypeDescriptor descriptor = getTopicFactory().newDataParcelDescriptor(messageEventType, messageTriggerEvent, version);
        return (descriptor);
    }
}
