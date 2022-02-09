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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class MLLPEgressMessageMetricsCapture {
    private static final Logger LOG = LoggerFactory.getLogger(MLLPEgressMessageMetricsCapture.class);

    private DateTimeFormatter timeFormatter;

    @Inject
    private ProcessingPlantMetricsAgentAccessor processingPlantMetricsAgentAccessor;

    @Inject
    private PetasosITOpsNotificationContentFactory notificationContentFactory;

    @Inject
    private HL7v2xTaskMetadataExtractor hl7v2xTaskMetadataExtractor;

    //
    // Constructor(s)
    //

    public MLLPEgressMessageMetricsCapture(){
        timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.SSS").withZone(ZoneId.of(PetasosPropertyConstants.DEFAULT_TIMEZONE));
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
        // Do some Endpiint Metrics
        EndpointMetricsAgent endpointMetricsAgent = camelExchange.getProperty(PetasosPropertyConstants.ENDPOINT_METRICS_AGENT_EXCHANGE_PROPERTY, EndpointMetricsAgent.class);
        endpointMetricsAgent.touchLastActivityInstant();
        endpointMetricsAgent.incrementEgressSendAttemptCount();

        //
        // Notifications
        boolean isHL7v2Message = hl7v2xTaskMetadataExtractor.isHL7V2Payload(uow.getIngresContent());
        if(isHL7v2Message){
            String messageHeaderSegment = hl7v2xTaskMetadataExtractor.getMSH(uow.getIngresContent().getPayload());
            String patientIdentifierSegment = hl7v2xTaskMetadataExtractor.getPID(uow.getIngresContent().getPayload());
            sendTrafficNotification(endpointMetricsAgent, messageHeaderSegment, patientIdentifierSegment);
        }

        getLogger().debug(".capturePreSendMetricDetail(): Exit, uow->{}", uow);
        return(uow);
    }

    protected void sendTrafficNotification(EndpointMetricsAgent endpointMetricsAgent, String msh, String pid){
        getLogger().debug(".sendACKNotification(): Entry");

        String target = getConnectedSystemName(endpointMetricsAgent);
        String endpointDescription = getEndpointDescription(endpointMetricsAgent);

        StringBuilder formattedMessageBuilder = new StringBuilder();

        formattedMessageBuilder.append("<table style='width:100%'>");
        formattedMessageBuilder.append("<tr>");
        formattedMessageBuilder.append("<td> Sending Egress Message ");
        formattedMessageBuilder.append("(" + getTimeFormatter().format(Instant.now()) + ")");
        formattedMessageBuilder.append("</td>");
        formattedMessageBuilder.append("</tr>");
        formattedMessageBuilder.append("<tr>");
        formattedMessageBuilder.append("<td>");
        formattedMessageBuilder.append("To: ");
        formattedMessageBuilder.append(target);
        formattedMessageBuilder.append(" via ");
        formattedMessageBuilder.append(endpointDescription);
        formattedMessageBuilder.append("</td>");
        formattedMessageBuilder.append("</tr>");
        formattedMessageBuilder.append("<tr>");
        formattedMessageBuilder.append("<td>");
        formattedMessageBuilder.append(msh + "\n" + pid);
        formattedMessageBuilder.append("</td>");
        formattedMessageBuilder.append("</tr>");
        formattedMessageBuilder.append("</table>");

        String formattedMessage = formattedMessageBuilder.toString();

        StringBuilder unformattedMessageBuilder = new StringBuilder();
        unformattedMessageBuilder.append("Sending Egress Message ");
        unformattedMessageBuilder.append("(" + getTimeFormatter().format(Instant.now()) + ") \n");
        unformattedMessageBuilder.append("To: ");
        unformattedMessageBuilder.append(target);
        unformattedMessageBuilder.append(" via ");
        unformattedMessageBuilder.append(endpointDescription + "\n");
        formattedMessageBuilder.append(msh + "\n" + pid + "\n");
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
            // Do some Procesing Plant Metrics
            getProcessingPlantMetricsAgent().incrementEgressMessageCount();
            getProcessingPlantMetricsAgent().touchLastActivityInstant();

            //
            // Do some WUP Metrics
            metricsAgent.touchLastActivityInstant();
            metricsAgent.incrementEgressMessageCount();

            //
            // Do some Endpoint Metrics
            endpointMetricsAgent.touchLastActivityInstant();
            endpointMetricsAgent.incrementEgressMessageCount();
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
        String endpointDescription = getEndpointDescription(endpointMetricsAgent);

        StringBuilder formattedMessageBuilder = new StringBuilder();

        formattedMessageBuilder.append("<table style='width:100%'>");
        formattedMessageBuilder.append("<tr>");
        formattedMessageBuilder.append("<td> Egress Message");
        if(success) {
            formattedMessageBuilder.append(" Acknowledgement ");
        } else {
            formattedMessageBuilder.append(" ERROR ");
        }
        formattedMessageBuilder.append("(" + getTimeFormatter().format(Instant.now()) + ")");
        formattedMessageBuilder.append("</td>");
        formattedMessageBuilder.append("</tr>");
        formattedMessageBuilder.append("<tr>");
        formattedMessageBuilder.append("<td>");
        formattedMessageBuilder.append("From: ");
        formattedMessageBuilder.append(target);
        formattedMessageBuilder.append(" via ");
        formattedMessageBuilder.append(endpointDescription);
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

    protected void sendExceptionNotification(String error, Exchange camelExchange){
        getLogger().debug(".captureConnectionException(): Entry, error->{}", error);
        EndpointMetricsAgent endpointMetricsAgent = camelExchange.getProperty(PetasosPropertyConstants.ENDPOINT_METRICS_AGENT_EXCHANGE_PROPERTY, EndpointMetricsAgent.class);

        //
        // Notifications
        String target = getConnectedSystemName(endpointMetricsAgent);
        String endpointDescription = getEndpointDescription(endpointMetricsAgent);
        String unformattedMessage = createUnformattedMessage(target, endpointDescription, error);
        String formattedMessage = createFormattedMessage(target,endpointDescription, error);

        //
        // Endpoint Notification
        endpointMetricsAgent.sendITOpsNotification(unformattedMessage, formattedMessage);
        //
        // Processing PlantNotification
        getProcessingPlantMetricsAgent().sendITOpsNotification(unformattedMessage, formattedMessage);

        getLogger().debug(".captureConnectionException(): Exit...");
    }

    protected String createFormattedMessage(String connectedSystemName, String endpointDescription, String error){
        getLogger().debug(".createFormattedMessage(): Entry, connectedSystemName->{}, endpointDescription->{}, error->{}", connectedSystemName,endpointDescription, error);
        StringBuilder messagageBuilder = new StringBuilder();

        messagageBuilder.append("<b> Egress Message ERROR ("+getTimeFormatter().format(Instant.now())+") </b> \n" );
        messagageBuilder.append("<table style='width:100%'>");
        messagageBuilder.append("<tr>");
        messagageBuilder.append("<td>"+error+"</td>");
        messagageBuilder.append("<td>To</td>");
        messagageBuilder.append("<td>"+connectedSystemName+"</td>");
        messagageBuilder.append("<td>via</td>");
        messagageBuilder.append("<td>"+endpointDescription+"</td>");
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

    protected String getEndpointDescription(EndpointMetricsAgent metricsAgent){
        if(StringUtils.isNotEmpty(metricsAgent.getEndpointDescription())){
            return(metricsAgent.getEndpointDescription());
        } else {
            return("Not Specified");
        }
    }
}
