/*
 * Copyright (c) 2022 ACT Health
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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.exceptions;

import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.model.petasos.oam.notifications.valuesets.PetasosComponentITOpsNotificationTypeEnum;
import net.fhirfactory.pegacorn.internals.hl7v2.segments.ZDESegment;
import net.fhirfactory.pegacorn.internals.hl7v2.segments.ZDESegmentSet;
import net.fhirfactory.pegacorn.internals.hl7v2.segments.factories.ZDESegmentFactory;
import net.fhirfactory.pegacorn.internals.hl7v2.segments.helpers.ZDESegmentHelper;
import net.fhirfactory.pegacorn.petasos.oam.metrics.collectors.ProcessingPlantMetricsAgent;
import net.fhirfactory.pegacorn.petasos.oam.metrics.collectors.ProcessingPlantMetricsAgentAccessor;
import net.fhirfactory.pegacorn.petasos.oam.metrics.collectors.WorkUnitProcessorMetricsAgent;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public abstract class TransformationSoftFailureExceptionHandlerBase {
    private DateTimeFormatter timeFormatter;

    @Inject
    private ProcessingPlantMetricsAgentAccessor processingPlantMetricsAgentAccessor;

    @Inject
    private ZDESegmentHelper zdeSegmentHelper;

    @Inject
    private ZDESegmentFactory zdeSegmentFactory;

    //
    // Constructor(s)
    //

    public TransformationSoftFailureExceptionHandlerBase(){
        timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.of(PetasosPropertyConstants.DEFAULT_TIMEZONE));
    }

    //
    // Getters (and Setters)
    //

    abstract protected Logger getLogger();

    protected DateTimeFormatter getTimeFormatter(){
        return(timeFormatter);
    }

    protected ProcessingPlantMetricsAgent getProcessingPlantMetricsAgent(){
        return(processingPlantMetricsAgentAccessor.getMetricsAgent());
    }

    protected ZDESegmentHelper getZDESegmentHelper(){
        return(zdeSegmentHelper);
    }

    protected ZDESegmentFactory getZdeSegmentFactory(){
        return(zdeSegmentFactory);
    }

    //
    // Business Methods
    //


    //
    // ITOps Notifications
    //

    protected void sendExceptionNotification(String error, Exchange camelExchange){
        getLogger().debug(".captureConnectionException(): Entry, error->{}", error);
        WorkUnitProcessorMetricsAgent metricsAgent = camelExchange.getProperty(PetasosPropertyConstants.WUP_METRICS_AGENT_EXCHANGE_PROPERTY, WorkUnitProcessorMetricsAgent.class);

        metricsAgent.incrementEgressMessageFailureCount();

        //
        // Notifications

        String notificationHeading = metricsAgent.getWUPMetricsData().getParticipantName() + ": " + error;

        String unformattedMessage = createUnformattedMessage(metricsAgent.getWUPMetricsData().getParticipantName(), error);
        String formattedMessage = createFormattedMessage(metricsAgent.getWUPMetricsData().getParticipantName(), error);

        //
        // Endpoint Notification
        metricsAgent.sendITOpsNotification(unformattedMessage, formattedMessage, PetasosComponentITOpsNotificationTypeEnum.FAILURE_NOTIFICATION_TYPE, notificationHeading);
        //
        // Processing PlantNotification
        getProcessingPlantMetricsAgent().sendITOpsNotification(unformattedMessage, formattedMessage);

        getLogger().debug(".captureConnectionException(): Exit...");
    }

    protected String createUnformattedMessage(String participantName, String error){
        getLogger().debug(".createUnformattedMessage(): Entry, participantName->{}, error->{}",  participantName, error);
        StringBuilder messagageBuilder = new StringBuilder();

        messagageBuilder.append(participantName + "("+getTimeFormatter().format(Instant.now())+")\n" );
        messagageBuilder.append("ERROR "+ error );
        messagageBuilder.append("\n");

        String message = messagageBuilder.toString();
        return(message);

    }

    protected String createFormattedMessage(String participantName, String error){
        getLogger().debug(".createFormattedMessage(): Entry, participantName->{}, error->{}", participantName, error);
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("<table>");
        messageBuilder.append("<tr>");
        messageBuilder.append("<th><font color=red> Exception </font></th><th>" + getTimeFormatter().format(Instant.now()) + "</th>");
        messageBuilder.append("</tr>");
        messageBuilder.append("<tr>");
        messageBuilder.append("<td>Location</td><td>" + participantName + "</td>");
        messageBuilder.append("</tr>");
        messageBuilder.append("<tr>");
        messageBuilder.append("<td> Message </td><td><font color=red> "+error+"</font></td>");
        messageBuilder.append("</tr>");
        messageBuilder.append("</table>");

        String message = messageBuilder.toString();
        getLogger().debug(".createFormattedMessage(): Exit, message->{}", message);
        return(message);
    }

    protected String addZDESegment(String payload, String errorMessage, String location){
        getLogger().debug(".addZDESegment(): Entry, payload->{}", payload);
        if(StringUtils.isEmpty(payload) || StringUtils.isEmpty(errorMessage) || StringUtils.isEmpty(location)){
            getLogger().debug(".addZDESegment(): Either the payload, errorMessage or location are empty, returning original payload");
            return(payload);
        }
        ZDESegment newSegment = new ZDESegment();
        newSegment.setSetId(0);
        newSegment.setSource(location);
        String tidiedUpErrorMessage = errorMessage.replace("\n",", ").replace("\r", ", ");
        newSegment.setComment(tidiedUpErrorMessage);
        newSegment.setCommentType("Soft-Failure Condition Trigger");
        ZDESegmentSet segmentSet = new ZDESegmentSet();
        segmentSet.addErrorNote(newSegment);
        String updatedPayload = getZDESegmentHelper().addZDESegmentSet(payload, segmentSet);
        getLogger().debug(".addZDESegment(): Exit, updatedPayload->{}", updatedPayload);
        return(updatedPayload);
    }

}

