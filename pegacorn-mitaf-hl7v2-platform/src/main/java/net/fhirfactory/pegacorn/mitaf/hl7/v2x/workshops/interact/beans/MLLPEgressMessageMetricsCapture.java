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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans;

import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.interfaces.topology.ProcessingPlantInterface;
import net.fhirfactory.pegacorn.core.model.petasos.oam.notifications.valuesets.PetasosComponentITOpsNotificationTypeEnum;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.petasos.core.tasks.factories.metadata.HL7v2xTaskMetadataExtractor;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.EndpointMetricsAgent;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgent;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgentAccessor;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.WorkUnitProcessorMetricsAgent;
import net.fhirfactory.pegacorn.petasos.oam.notifications.PetasosITOpsNotificationContentFactory;
import org.apache.camel.Exchange;
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
public class MLLPEgressMessageMetricsCapture {
    private static final Logger LOG = LoggerFactory.getLogger(MLLPEgressMessageMetricsCapture.class);

    private DateTimeFormatter timeFormatter;
    private boolean initialised;
    private boolean includeFullHL7MessageInLog;
    private Integer maxHL7MessageSize;

    private static final Integer DEFAULT_MAX_MESSAGE_LENGTH = 64000;

    @Inject
    private ProcessingPlantMetricsAgentAccessor processingPlantMetricsAgentAccessor;

    @Inject
    private PetasosITOpsNotificationContentFactory notificationContentFactory;

    @Inject
    private HL7v2xTaskMetadataExtractor hl7v2xTaskMetadataExtractor;

    @Inject
    private ProcessingPlantInterface processingPlant;

    //
    // Constructor(s)
    //

    public MLLPEgressMessageMetricsCapture(){
        timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.of(PetasosPropertyConstants.DEFAULT_TIMEZONE));
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
                } else {
                    setMaxHL7MessageSize(DEFAULT_MAX_MESSAGE_LENGTH);
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

    protected Logger getLogger(){
        return(LOG);
    }

    protected ProcessingPlantMetricsAgent getProcessingPlantMetricsAgent(){
        return(processingPlantMetricsAgentAccessor.getMetricsAgent());
    }

    protected DateTimeFormatter getTimeFormatter(){
        return(this.timeFormatter);
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
    // Business Methods
    //

    public UoW capturePreSendMetricDetail(UoW uow, Exchange camelExchange){
        getLogger().debug(".capturePreSendMetricDetail(): Entry, uow->{}", uow);

        UoWPayload payload = uow.getIngresContent();

        //
        // Do some WUP Metrics
        WorkUnitProcessorMetricsAgent metricsAgent = camelExchange.getProperty(PetasosPropertyConstants.WUP_METRICS_AGENT_EXCHANGE_PROPERTY, WorkUnitProcessorMetricsAgent.class);
        metricsAgent.touchLastActivityInstant();

        //
        // Do some Endpoint Metrics
        try {
            EndpointMetricsAgent endpointMetricsAgent = camelExchange.getProperty(PetasosPropertyConstants.ENDPOINT_METRICS_AGENT_EXCHANGE_PROPERTY, EndpointMetricsAgent.class);
            endpointMetricsAgent.touchLastActivityInstant();
            endpointMetricsAgent.incrementEgressSendAttemptCount();

            boolean isHL7v2Message = hl7v2xTaskMetadataExtractor.isHL7V2Payload(payload);
            if (isHL7v2Message) {
                String messageHeaderSegment = hl7v2xTaskMetadataExtractor.getMSH(payload.getPayload());
                String patientIdentifierSegment = hl7v2xTaskMetadataExtractor.getPID(payload.getPayload());
                if(StringUtils.isNotEmpty(payload.getPayload())) {
                    sendTrafficNotification(endpointMetricsAgent, messageHeaderSegment, patientIdentifierSegment, payload.getPayload());
                } else {
                    sendTrafficNotification(endpointMetricsAgent, messageHeaderSegment, patientIdentifierSegment, "Unable to Extract Payload Content");
                }
            }
        } catch(Exception ex){
            getLogger().warn(".capturePreSendMetricDetail(): Unable to access Metrics Agent, error message->{}, stack trace->{}", ExceptionUtils.getMessage(ex), ExceptionUtils.getStackTrace(ex));
        }

        getLogger().debug(".capturePreSendMetricDetail(): Exit, uow->{}", uow);
        return(uow);
    }

    protected void sendTrafficNotification(EndpointMetricsAgent endpointMetricsAgent, String msh, String pid, String message){
        getLogger().debug(".sendACKNotification(): Entry");

        String target = getConnectedSystemName(endpointMetricsAgent);
        String endpointDisplayName = getEndpointDisplayName(endpointMetricsAgent);

        StringBuilder formattedMessageBuilder = new StringBuilder();

        formattedMessageBuilder.append("<table>");
        formattedMessageBuilder.append("<tr>");
        formattedMessageBuilder.append("<td> To </td>");
        formattedMessageBuilder.append("<td>" + target + " via " + endpointDisplayName + " (" + getTimeFormatter().format(Instant.now()) + ") </td>");
        formattedMessageBuilder.append("</tr>");
        formattedMessageBuilder.append("<tr>");
        formattedMessageBuilder.append("<td>Metadata</td><td>");
        formattedMessageBuilder.append(msh + "\n" + pid);
        formattedMessageBuilder.append("</td>");
        formattedMessageBuilder.append("</tr>");
        formattedMessageBuilder.append("</table>");

        String formattedMessage = formattedMessageBuilder.toString();

        StringBuilder unformattedMessageBuilder = new StringBuilder();
        unformattedMessageBuilder.append("-------------------------------------------------------");
        unformattedMessageBuilder.append("Sending Egress Message ");
        unformattedMessageBuilder.append("(" + getTimeFormatter().format(Instant.now()) + ") \n");
        unformattedMessageBuilder.append("To: ");
        unformattedMessageBuilder.append(target);
        unformattedMessageBuilder.append(" via ");
        unformattedMessageBuilder.append(endpointDisplayName + "\n");
        if(isIncludeFullHL7MessageInLog()) {
            String displayedMessage = null;
            if(message.length() > getMaxHL7MessageSize()){
                displayedMessage = message.substring(0, getMaxHL7MessageSize());
            } else {
                displayedMessage = message;
            }
            unformattedMessageBuilder.append("::: Message{" + displayedMessage + "}");
        } else {
            unformattedMessageBuilder.append(":::{"+msh+"\n"+pid+"}");
        }
        String unformattedMessage = unformattedMessageBuilder.toString();

        //
        // Endpoint Notification
        endpointMetricsAgent.sendITOpsNotification(unformattedMessage, formattedMessage);

        getLogger().debug(".sendACKNotification(): Exit ...");
    }

    public UoW capturePostSendMetricDetail(UoW uow, Exchange camelExchange){
        getLogger().debug(".capturePostSendMetricDetail(): Entry, uow->{}", uow);

        WorkUnitProcessorMetricsAgent metricsAgent = camelExchange.getProperty(PetasosPropertyConstants.WUP_METRICS_AGENT_EXCHANGE_PROPERTY, WorkUnitProcessorMetricsAgent.class);
        EndpointMetricsAgent endpointMetricsAgent = camelExchange.getProperty(PetasosPropertyConstants.ENDPOINT_METRICS_AGENT_EXCHANGE_PROPERTY, EndpointMetricsAgent.class);

        if(uow.getProcessingOutcome().equals(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS)){
            //
            // Do some Processing Plant Metrics
            getProcessingPlantMetricsAgent().incrementEgressMessageSuccessCount();
            getProcessingPlantMetricsAgent().touchLastActivityInstant();

            //
            // Do some WUP Metrics
            metricsAgent.touchLastActivityInstant();
            metricsAgent.incrementEgressMessageAttemptCount();

            //
            // Do some Endpoint Metrics
            endpointMetricsAgent.touchLastActivityInstant();
            endpointMetricsAgent.incrementEgressMessageSuccessCount();
        }

        //
        // Notifications
        switch(uow.getProcessingOutcome()){
            case UOW_OUTCOME_SUCCESS:{
                UoWPayload actualPayload = null;
                for(UoWPayload currentEgressPayload: uow.getEgressContent().getPayloadElements()){
                    actualPayload = currentEgressPayload;
                    break;
                }
                sendACKNotification(true, actualPayload.getPayload(), endpointMetricsAgent);
                break;
            }
            case UOW_OUTCOME_FAILED:
                sendACKNotification(false, uow.getFailureDescription(), endpointMetricsAgent);
                metricsAgent.incrementEgressMessageFailureCount();
                break;
            case UOW_OUTCOME_INCOMPLETE:
                break;
            case UOW_OUTCOME_NO_PROCESSING_REQUIRED:
                break;
            case UOW_OUTCOME_FILTERED:
                break;
            case UOW_OUTCOME_DISCARD:
                break;
            case UOW_OUTCOME_NOTSTARTED:
                break;
        }

        getLogger().debug(".capturePostSendMetricDetail(): Exit, uow->{}", uow);
        return(uow);
    }

    protected void sendACKNotification(Boolean success, String acknowledgementPayload, EndpointMetricsAgent endpointMetricsAgent){
        getLogger().debug(".sendACKNotification(): Entry, success->{}, acknowledgementPayload->{}", success, acknowledgementPayload);

        String target = getConnectedSystemName(endpointMetricsAgent);
        String endpointDescription = getEndpointDisplayName(endpointMetricsAgent);

        StringBuilder formattedMessageBuilder = new StringBuilder();

        formattedMessageBuilder.append("<table>");
        formattedMessageBuilder.append("<tr>");
        formattedMessageBuilder.append("<td> Outcome </td><td>");
        if(success) {
            formattedMessageBuilder.append(" Acknowledgement ");
        } else {
            formattedMessageBuilder.append("<font color=red> ERROR </font>");
        }
        formattedMessageBuilder.append("  (" + getTimeFormatter().format(Instant.now()) + ")");
        formattedMessageBuilder.append("</td>");
        formattedMessageBuilder.append("</tr>");
        formattedMessageBuilder.append("</table>");

        String formattedMessage = formattedMessageBuilder.toString();

        StringBuilder unformattedMessageBuilder = new StringBuilder();
        unformattedMessageBuilder.append("Egress Message ");
        if(success) {
            unformattedMessageBuilder.append(" Acknowledgement ");
        } else {
            unformattedMessageBuilder.append(" ERROR ");
        }
        unformattedMessageBuilder.append("(" + getTimeFormatter().format(Instant.now()) + ") \n");
        unformattedMessageBuilder.append("From: ");
        unformattedMessageBuilder.append(target);
        unformattedMessageBuilder.append(" via ");
        unformattedMessageBuilder.append(endpointDescription + "\n");
        if(success){
            if(isIncludeFullHL7MessageInLog()) {
                String displayedMessage = null;
                if(acknowledgementPayload.length() > getMaxHL7MessageSize()){
                    displayedMessage = acknowledgementPayload.substring(0, getMaxHL7MessageSize());
                } else {
                    displayedMessage = acknowledgementPayload;
                }
                unformattedMessageBuilder.append("::: Message{" + displayedMessage + "}");
            }
        } else {
            unformattedMessageBuilder.append("::: Error{"+acknowledgementPayload+"}");
        }

        String unformattedMessage = unformattedMessageBuilder.toString();

        //
        // Endpoint Notification
        endpointMetricsAgent.sendITOpsNotification(unformattedMessage, formattedMessage);

        if(!success){
            getProcessingPlantMetricsAgent().sendITOpsNotification(unformattedMessage, formattedMessage);
        }
        getLogger().debug(".sendACKNotification(): Exit ...");
    }

    public Object captureTimeoutException(Object info, Exchange camelExchange){
        getLogger().debug(".captureTimeoutException(): Entry, info->{}", info);

        sendExceptionNotification("Timeout Error", camelExchange);

        getLogger().debug(".captureTimeoutException(): Exit, info->{}", info);
        return(info);
    }

    public Object captureMLLPAckException(Object info, Exchange camelExchange){
        getLogger().debug(".captureMLLPAckException(): Entry, info->{}", info);

        sendExceptionNotification("Acknowledgment Error", camelExchange);

        getLogger().debug(".captureMLLPAckException(): Exit, info->{}", info);
        return(info);
    }

    public Object captureConnectionException(Object info, Exchange camelExchange){
        getLogger().debug(".captureConnectionException(): Entry, info->{}", info);

        sendExceptionNotification("Connection Error", camelExchange);

        getLogger().debug(".captureConnectionException(): Exit, info->{}", info);
        return(info);
    }

    public Object captureGeneralException(Object info, Exchange camelExchange){
        getLogger().debug(".captureConnectionException(): Entry, info->{}", info);

        sendExceptionNotification("General Exception", camelExchange);

        getLogger().debug(".captureConnectionException(): Exit, info->{}", info);
        return(info);
    }

    protected void sendExceptionNotification(String error, Exchange camelExchange){
        getLogger().debug(".captureConnectionException(): Entry, error->{}", error);
        EndpointMetricsAgent endpointMetricsAgent = camelExchange.getProperty(PetasosPropertyConstants.ENDPOINT_METRICS_AGENT_EXCHANGE_PROPERTY, EndpointMetricsAgent.class);

        endpointMetricsAgent.incrementEgressMessageFailureCount();

        //
        // Notifications
        String target = getConnectedSystemName(endpointMetricsAgent);
        String endpointDescription = getEndpointDisplayName(endpointMetricsAgent);
        String unformattedMessage = createUnformattedMessage(target, endpointDescription, error);
        String formattedMessage = createFormattedMessage(target, endpointDescription, error);

        String notificationHeading = endpointDescription + ": " + error;

        //
        // Endpoint Notification
        endpointMetricsAgent.sendITOpsNotification(unformattedMessage, formattedMessage, PetasosComponentITOpsNotificationTypeEnum.FAILURE_NOTIFICATION_TYPE, notificationHeading);
        //
        // Processing PlantNotification
        getProcessingPlantMetricsAgent().sendITOpsNotification(unformattedMessage, formattedMessage);

        getLogger().debug(".captureConnectionException(): Exit...");
    }

    protected String createFormattedMessage(String connectedSystemName, String endpointDescription, String error){
        getLogger().debug(".createFormattedMessage(): Entry, connectedSystemName->{}, endpointDescription->{}, error->{}", connectedSystemName,endpointDescription, error);
        StringBuilder messagageBuilder = new StringBuilder();

        messagageBuilder.append("<table>");
        messagageBuilder.append("<tr>");
        messagageBuilder.append("<td> To </td>");
        messagageBuilder.append("<td>" + connectedSystemName + " via " + endpointDescription + "  (" + getTimeFormatter().format(Instant.now()) + ") </td>");
        messagageBuilder.append("</tr>");
        messagageBuilder.append("<tr>");
        messagageBuilder.append("<td> Outcome </td><td><font color=red> "+error+"</font></td>");
        messagageBuilder.append("</tr>");
        messagageBuilder.append("</table>");

        String message = messagageBuilder.toString();
        getLogger().debug(".createFormattedMessage(): Exit, message->{}", message);
        return(message);
    }

    protected String createUnformattedMessage(String connectedSystemName, String endpointDescription, String error){
        getLogger().debug(".createUnformattedMessage(): Entry, connectedSystemName->{}, endpointDescription->{}, error->{}", connectedSystemName,endpointDescription, error);
        StringBuilder messagageBuilder = new StringBuilder();

        messagageBuilder.append("Egress Message ERROR ("+getTimeFormatter().format(Instant.now())+")\n" );
        messagageBuilder.append("## "+ error );
        messagageBuilder.append(": To: ");
        messagageBuilder.append(connectedSystemName);
        messagageBuilder.append(": via: ");
        messagageBuilder.append(endpointDescription);
        messagageBuilder.append("\n");

        String message = messagageBuilder.toString();
        return(message);
    }

    protected String getConnectedSystemName(EndpointMetricsAgent metricsAgent){
        if(StringUtils.isNotEmpty(metricsAgent.getConnectedSystemName())){
            return(metricsAgent.getConnectedSystemName());
        } else {
            return("Not Specified");
        }
    }

    protected String getEndpointDisplayName(EndpointMetricsAgent metricsAgent){
        if(StringUtils.isNotEmpty(metricsAgent.getEndpointDisplayName())){
            return(metricsAgent.getEndpointDisplayName());
        } else {
            return("Not Specified");
        }
    }
}
