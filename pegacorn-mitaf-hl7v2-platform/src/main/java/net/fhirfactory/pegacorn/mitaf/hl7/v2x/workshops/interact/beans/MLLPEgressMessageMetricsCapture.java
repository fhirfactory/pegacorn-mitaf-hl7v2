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

@ApplicationScoped
public class MLLPEgressMessageMetricsCapture {
    private static final Logger LOG = LoggerFactory.getLogger(MLLPEgressMessageMetricsCapture.class);

    @Inject
    private ProcessingPlantMetricsAgentAccessor processingPlantMetricsAgentAccessor;

    @Inject
    private PetasosITOpsNotificationContentFactory notificationContentFactory;

    //
    // Constructor(s)
    //


    //
    // Getters (and Setters)
    //

    protected Logger getLogger(){
        return(LOG);
    }

    protected ProcessingPlantMetricsAgent getProcessingPlantMetricsAgent(){
        return(processingPlantMetricsAgentAccessor.getMetricsAgent());
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

        //
        // Notifications
        String notificationContent = notificationContentFactory.newNotificationContentFromUoWPayload(payload);
        String actualNotificationContent = null;
        if(StringUtils.isNotEmpty(endpointMetricsAgent.getEndpointDescription()) && StringUtils.isNotEmpty(endpointMetricsAgent.getConnectedSystemName())){
            actualNotificationContent = "Forwarding Message (To-->" + endpointMetricsAgent.getConnectedSystemName() + " via "+ endpointMetricsAgent.getEndpointDescription() +") \n" + notificationContent;
        } else if(StringUtils.isNotEmpty(endpointMetricsAgent.getEndpointDescription())) {
            actualNotificationContent = "Forwarding Message (To-->" + endpointMetricsAgent.getEndpointDescription() +") \n" + notificationContent;
        } else {
            actualNotificationContent = "Forwarding Message \n" + notificationContent;
        }

        //
        // Endpoint Notification
        endpointMetricsAgent.sendITOpsNotification(actualNotificationContent);

        //
        // Add some ProcessingPlant Notifications
        // getProcessingPlantMetricsAgent().sendITOpsNotification(actualNotificationContent);
        //
        // Add some WUP notifications
        // metricsAgent.sendITOpsNotification(actualNotificationContent);

        getLogger().debug(".capturePreSendMetricDetail(): Exit, uow->{}", uow);
        return(uow);
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
        String notificationContent = null;
        if(uow.getProcessingOutcome().equals(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS)){
            UoWPayload actualPayload = null;
            for(UoWPayload currentEgressPayload: uow.getEgressContent().getPayloadElements()){
                actualPayload = currentEgressPayload;
                break;
            }
            if(StringUtils.isNotEmpty(endpointMetricsAgent.getEndpointDescription()) && StringUtils.isNotEmpty(endpointMetricsAgent.getConnectedSystemName())){
                notificationContent = "Acknowledgement Received (From-->" + endpointMetricsAgent.getConnectedSystemName() + " via "+ endpointMetricsAgent.getEndpointDescription() +") \n" + actualPayload.getPayload();
            } else if(StringUtils.isNotEmpty(endpointMetricsAgent.getEndpointDescription())) {
                notificationContent = "Acknowledgement Received (From-->" + endpointMetricsAgent.getEndpointDescription() +") \n" + actualPayload.getPayload();
            } else {
                notificationContent = "Acknowledgement Received \n" + actualPayload.getPayload();
            }
        } else {
            notificationContent = "Message Failed, Error->{}" + uow.getFailureDescription();
        }

        //
        // Endpoint Notification
        endpointMetricsAgent.sendITOpsNotification(notificationContent);

        //
        // Add some ProcessingPlant Notifications
        // getProcessingPlantMetricsAgent().sendITOpsNotification(actualNotificationContent);
        //
        // Add some WUP notifications
        // metricsAgent.sendITOpsNotification(actualNotificationContent);

        getLogger().debug(".capturePostSendMetricDetail(): Exit, uow->{}", uow);
        return(uow);
    }

    public Object captureTimeoutException(Object info, Exchange camelExchange){
        getLogger().debug(".captureTimeoutException(): Entry, info->{}", info);
        EndpointMetricsAgent endpointMetricsAgent = camelExchange.getProperty(PetasosPropertyConstants.ENDPOINT_METRICS_AGENT_EXCHANGE_PROPERTY, EndpointMetricsAgent.class);
        //
        // Notifications
        String notificationContent = null;
        if(StringUtils.isNotEmpty(endpointMetricsAgent.getEndpointDescription()) && StringUtils.isNotEmpty(endpointMetricsAgent.getConnectedSystemName())){
            notificationContent = "Timeout Error (To-->" + endpointMetricsAgent.getConnectedSystemName() + " via " + endpointMetricsAgent.getEndpointDescription() +") \n" + info.toString();
        } else if(StringUtils.isNotEmpty(endpointMetricsAgent.getEndpointDescription())) {
            notificationContent = "Timeout Error (To-->" + endpointMetricsAgent.getEndpointDescription() +") \n" +  info.toString();
        } else {
            notificationContent = "Timeout Error \n" +  info.toString();
        }

        //
        // Endpoint Notification
        endpointMetricsAgent.sendITOpsNotification(notificationContent);
        //
        // Processing PlantNotification
        getProcessingPlantMetricsAgent().sendITOpsNotification(notificationContent);

        getLogger().debug(".captureTimeoutException(): Exit, info->{}", info);
        return(info);
    }

    public Object captureMLLPAckException(Object info, Exchange camelExchange){
        getLogger().debug(".captureMLLPAckException(): Entry, info->{}", info);
        EndpointMetricsAgent endpointMetricsAgent = camelExchange.getProperty(PetasosPropertyConstants.ENDPOINT_METRICS_AGENT_EXCHANGE_PROPERTY, EndpointMetricsAgent.class);
        //
        // Notifications
        String notificationContent = null;
        if(StringUtils.isNotEmpty(endpointMetricsAgent.getEndpointDescription()) && StringUtils.isNotEmpty(endpointMetricsAgent.getConnectedSystemName())){
            notificationContent = "ACK Error (To-->" + endpointMetricsAgent.getConnectedSystemName() + " via " + endpointMetricsAgent.getEndpointDescription() +") \n" + info.toString();
        } else if(StringUtils.isNotEmpty(endpointMetricsAgent.getEndpointDescription())) {
            notificationContent = "ACK Error (To-->" + endpointMetricsAgent.getEndpointDescription() +") \n" +  info.toString();
        } else {
            notificationContent = "ACK Error \n" +  info.toString();
        }

        //
        // Endpoint Notification
        endpointMetricsAgent.sendITOpsNotification(notificationContent);
        //
        // Processing PlantNotification
        getProcessingPlantMetricsAgent().sendITOpsNotification(notificationContent);

        getLogger().debug(".captureMLLPAckException(): Exit, info->{}", info);
        return(info);
    }

    public Object captureConnectionException(Object info, Exchange camelExchange){
        getLogger().debug(".captureConnectionException(): Entry, info->{}", info);
        EndpointMetricsAgent endpointMetricsAgent = camelExchange.getProperty(PetasosPropertyConstants.ENDPOINT_METRICS_AGENT_EXCHANGE_PROPERTY, EndpointMetricsAgent.class);
        //
        // Notifications
        String notificationContent = null;
        if(StringUtils.isNotEmpty(endpointMetricsAgent.getEndpointDescription()) && StringUtils.isNotEmpty(endpointMetricsAgent.getConnectedSystemName())){
            notificationContent = "Connection Error (To-->" + endpointMetricsAgent.getConnectedSystemName() + " via " + endpointMetricsAgent.getEndpointDescription() +") \n" + info.toString();
        } else if(StringUtils.isNotEmpty(endpointMetricsAgent.getEndpointDescription())) {
            notificationContent = "Connection Error (To-->" + endpointMetricsAgent.getEndpointDescription() +") \n" +  info.toString();
        } else {
            notificationContent = "Connection Error \n" +  info.toString();
        }

        //
        // Endpoint Notification
        endpointMetricsAgent.sendITOpsNotification(notificationContent);
        //
        // Processing PlantNotification
        getProcessingPlantMetricsAgent().sendITOpsNotification(notificationContent);

        getLogger().debug(".captureConnectionException(): Exit, info->{}", info);
        return(info);
    }
}
