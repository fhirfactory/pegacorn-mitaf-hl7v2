/*
 * Copyright (c) 2022 Mark A. Hunter
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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.mllp;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.interfaces.topology.ProcessingPlantInterface;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelNormalisationStatusEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelTypeEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelValidationStatusEnum;
import net.fhirfactory.pegacorn.core.model.petasos.participant.ProcessingPlantPetasosParticipantNameHolder;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.base.IPCTopologyEndpoint;
import net.fhirfactory.pegacorn.core.model.topology.nodes.WorkUnitProcessorSoftwareComponent;
import net.fhirfactory.pegacorn.internals.hl7v2.helpers.UltraDefensivePipeParser;
import net.fhirfactory.pegacorn.internals.hl7v2.triggerevents.valuesets.HL7v2SegmentTypeEnum;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.datatypes.MLLPMessageActivityParcel;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.EndpointMetricsAgent;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgent;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgentAccessor;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.WorkUnitProcessorMetricsAgent;
import org.apache.camel.Exchange;
import org.apache.camel.component.mllp.MllpConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


@ApplicationScoped
public class MLLPMessageIngresProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MLLPMessageIngresProcessor.class);

    private HapiContext context;
    private DateTimeFormatter timeFormatter;
    private boolean initialised;
    private boolean includeFullHL7MessageInLog;
    private Integer maxHL7MessageSize;

    @Inject
    private ProcessingPlantInterface processingPlant;

    @Inject
    private ProcessingPlantPetasosParticipantNameHolder participantNameHolder;

    @Inject
    private ProcessingPlantMetricsAgentAccessor processingPlantMetricsAgentAccessor;

    @Inject
    private UltraDefensivePipeParser defensivePipeParser;

    //
    // Constructor(s)
    //

    public MLLPMessageIngresProcessor() {
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

    protected ProcessingPlantMetricsAgent getProcessingPlantMetricsAgent(){
        return(this.processingPlantMetricsAgentAccessor.getMetricsAgent());
    }

    //
    // Business Functions
    //

    public MLLPMessageActivityParcel captureMLLPMessage(String messageString,
                                                        Exchange exchange,
                                                        String sourceSystem,
                                                        String intendedTargetSystem,
                                                        String parcelDiscriminatorType,
                                                        String parcelDiscriminatorValue) {

        LOG.debug(".captureMLLPMessage(): Entry, messageString->{}", messageString);

        // -------------------------------------------------
        LOG.warn("Incoming Message->{}", messageString);
        // -------------------------------------------------

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

        //
        // Add some notifications
        String targetPort = exchange.getProperty(PetasosPropertyConstants.ENDPOINT_PORT_VALUE, String.class);
//            String notificationContent;


        String mshSegment = null;
        String pidSegment = null;
        try{
            mshSegment = defensivePipeParser.extractSegment(messageString, HL7v2SegmentTypeEnum.MSH);
            pidSegment = defensivePipeParser.extractSegment(messageString, HL7v2SegmentTypeEnum.PID);
            if(StringUtils.isEmpty(pidSegment)) {
                pidSegment = "PID: Unknown";
            }
            if(StringUtils.isEmpty(mshSegment)){
                mshSegment = "MSH: unknown";
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
        sendMessageReceivedConsoleNotification(portDescription, messageString, mshSegment, pidSegment, endpointMetricsAgent);

        UoWPayload mllpPayload = new UoWPayload();
        DataParcelTypeDescriptor contentDescriptor = new DataParcelTypeDescriptor();
        contentDescriptor.setDataParcelDefiner("Apache Camel");
        contentDescriptor.setDataParcelCategory("MLLP");
        contentDescriptor.setDataParcelSubCategory("Consumer");
        contentDescriptor.setDataParcelResource("stringPayload");
        contentDescriptor.setVersion("3.17.x"); // TODO should derive this from library or such
        // Build the manifest
        DataParcelManifest manifest = new DataParcelManifest();
        manifest.setContentDescriptor(contentDescriptor);
        if(StringUtils.isNotEmpty(sourceSystem)){
            manifest.setSourceSystem(sourceSystem);
        }
        if(StringUtils.isNotEmpty(parcelDiscriminatorType)){
            contentDescriptor.setDataParcelDiscriminatorType(parcelDiscriminatorType);
        }
        if(StringUtils.isNotEmpty(parcelDiscriminatorValue)){
            contentDescriptor.setDataParcelDiscriminatorValue(parcelDiscriminatorValue);
        }
        manifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATED_FALSE);
        manifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_FALSE);
        manifest.setDataParcelType(DataParcelTypeEnum.GENERAL_DATA_PARCEL_TYPE);
        manifest.setInterSubsystemDistributable(false);

        boolean failed = false;
        String failedMessage = null;
        if(StringUtils.isEmpty(messageString)){
            mllpPayload.setPayload("Empty Message");
            failedMessage = "Empty Message";
            failed = true;
        } else {
            mllpPayload.setPayload(messageString);
        }
        mllpPayload.setPayloadManifest(manifest);

        // Create the UoW
        UoW uow = new UoW(mllpPayload);
        if(failed){
            uow.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED);
            uow.setFailureDescription(failedMessage);
        } else {
            uow.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);
        }

        // Extract ALL MLLP Metadata (if available)
        MLLPMessageActivityParcel messageActivity = extractMLLPMessageDetailsFromExchange(exchange);


        messageActivity.setUow(uow);

        getLogger().debug(".captureMLLPMessage(): Exit, messageActivity->{}", messageActivity);
        return (messageActivity);
    }


    protected MLLPMessageActivityParcel extractMLLPMessageDetailsFromExchange(Exchange exchange){
        getLogger().debug(".extractMLLPMessageDetailsFromExchange(): Entry");

        String mllpConnectionLocalAddress = exchange.getMessage().getHeader(MllpConstants.MLLP_LOCAL_ADDRESS, String.class);
        String mllpConnectionRemoteAddress = exchange.getMessage().getHeader(MllpConstants.MLLP_REMOTE_ADDRESS, String.class);
        String mllpAcknowledgement = exchange.getMessage().getHeader(MllpConstants.MLLP_ACKNOWLEDGEMENT_STRING, String.class);
        String mllpAcknowledgementType = exchange.getMessage().getHeader(MllpConstants.MLLP_ACKNOWLEDGEMENT_TYPE, String.class);
        String mllpSendingApplication = exchange.getMessage().getHeader(MllpConstants.MLLP_SENDING_APPLICATION, String.class);
        String mllpSendingFacility = exchange.getMessage().getHeader(MllpConstants.MLLP_SENDING_FACILITY, String.class);
        String mllpReceivingFacility = exchange.getMessage().getHeader(MllpConstants.MLLP_RECEIVING_FACILITY, String.class);
        String mllpReceivingApplication = exchange.getMessage().getHeader(MllpConstants.MLLP_RECEIVING_APPLICATION, String.class);
        String mllpTimestamp = exchange.getMessage().getHeader(MllpConstants.MLLP_TIMESTAMP, String.class);
        String mllpSecurity = exchange.getMessage().getHeader(MllpConstants.MLLP_SECURITY, String.class);
        String mllpTriggerEvent = exchange.getMessage().getHeader(MllpConstants.MLLP_TRIGGER_EVENT, String.class);
        String mllpEventType = exchange.getMessage().getHeader(MllpConstants.MLLP_EVENT_TYPE, String.class);
        String mllpVersion = exchange.getMessage().getHeader(MllpConstants.MLLP_VERSION_ID, String.class);
        String mllpMessageType = exchange.getMessage().getHeader(MllpConstants.MLLP_MESSAGE_TYPE, String.class);
        String mllpMessageControlId = exchange.getMessage().getHeader(MllpConstants.MLLP_MESSAGE_CONTROL, String.class);
        String mllpMessageProcessingId = exchange.getMessage().getHeader(MllpConstants.MLLP_PROCESSING_ID, String.class);
        String mllpCharSet = exchange.getMessage().getHeader(MllpConstants.MLLP_CHARSET, String.class);

        MLLPMessageActivityParcel messageActivity = new MLLPMessageActivityParcel();
        if(StringUtils.isNotEmpty(mllpConnectionLocalAddress)){
            messageActivity.setMllpConnectionLocalAddress(mllpConnectionLocalAddress);
        }
        if(StringUtils.isNotEmpty(mllpConnectionRemoteAddress)){
            messageActivity.setMllpConnectionRemoteAddress(mllpConnectionRemoteAddress);
        }
        if(StringUtils.isNotEmpty(mllpAcknowledgement)){
            messageActivity.setMllpAcknowledgement(mllpAcknowledgement);
        }
        if(StringUtils.isNotEmpty(mllpAcknowledgementType)){
            messageActivity.setMllpAcknowledgementType(mllpAcknowledgementType);
        }
        if(StringUtils.isNotEmpty(mllpSendingApplication)){
            messageActivity.setMllpSendingApplication(mllpReceivingApplication);
        }
        if(StringUtils.isNotEmpty(mllpSendingFacility)){
            messageActivity.setMllpSendingFacility(mllpReceivingFacility);
        }
        if(StringUtils.isNotEmpty(mllpTimestamp)){
            messageActivity.setMllpTimestamp(mllpTimestamp);
        }
        if(StringUtils.isNotEmpty(mllpSecurity)){
            messageActivity.setMllpSecurity(mllpSecurity);
        }
        if(StringUtils.isNotEmpty(mllpTriggerEvent)){
            messageActivity.setMllpTriggerEvent(mllpTriggerEvent);
        }
        if(StringUtils.isNotEmpty(mllpEventType)){
            messageActivity.setMllpEventType(mllpEventType);
        }
        if(StringUtils.isNotEmpty(mllpVersion)){
            messageActivity.setMllpVersionId(mllpVersion);
        }
        if(StringUtils.isNotEmpty(mllpMessageType)){
            messageActivity.setMllpMessageType(mllpMessageType);
        }
        if(StringUtils.isNotEmpty(mllpMessageControlId)){
            messageActivity.setMllpMessageControlId(mllpMessageControlId);
        }
        if(StringUtils.isNotEmpty(mllpMessageProcessingId)){
            messageActivity.setMllpProcessingId(mllpMessageProcessingId);
        }
        if(StringUtils.isNotEmpty(mllpCharSet)){
            messageActivity.setMllpCharSet(mllpCharSet);
        }

        getLogger().debug(".extractMLLPMessageDetailsFromExchange(): Exit, messageActivity->{}", messageActivity);
        return(messageActivity);
    }

    protected void sendMessageReceivedConsoleNotification(String portDescription, String messageString, String mshSegment, String pidSegment, EndpointMetricsAgent endpointMetricsAgent){
        getLogger().debug(".sendMessageReceivedConsoleNotification(): Entry, portDescription->{}, messageString->{}", portDescription, messageString);
        StringBuilder notificationContentBuilder = new StringBuilder();
        notificationContentBuilder.append("Ingres-Message((" + portDescription +")(" + getTimeFormatter().format(Instant.now()) + ")){");
        if(includeFullHL7MessageInLog){
            String displayedMessage = null;
            try {
                if (messageString.length() > getMaxHL7MessageSize()) {
                    displayedMessage = messageString.substring(0, getMaxHL7MessageSize());
                } else {
                    displayedMessage = messageString;
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
}
