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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents;


import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Media;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.interfaces.media.PetasosMediaServiceAgentInterface;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.interact.StandardInteractClientTopologyEndpointPort;
import net.fhirfactory.pegacorn.internals.hl7v2.helpers.MediaPipeParser;
import net.fhirfactory.pegacorn.internals.hl7v2.segments.helpers.ZDESegmentHelper;
import net.fhirfactory.pegacorn.internals.hl7v2.triggerevents.valuesets.HL7v2SegmentTypeEnum;

@Dependent
public class HL7v2xMessageExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xMessageExtractor.class);

    @Inject
    private MediaPipeParser mediaParser;

    @Inject
    private PetasosMediaServiceAgentInterface mediaAgent;

    @Inject
    private ZDESegmentHelper zdeSegmentHelper;


    //
    // Constructor(s)
    //

    public HL7v2xMessageExtractor(){
    }

    //
    // Getters (and Setters)
    //

    protected Logger getLogger(){
        return(LOG);
    }

    protected ZDESegmentHelper getZDESegmentHelper(){
        return(zdeSegmentHelper);
    }

    //
    // Business Methods
    //

    public String convertToMessage(UoW incomingUoW, Exchange camelExchange) {
        getLogger().debug(".convertToMessage(): Entry, incomingUoW->{}", incomingUoW);

        String messageAsString = incomingUoW.getIngresContent().getPayload();

        getLogger().info("OutgoingMessage--->>>" + messageAsString + "<<<---");

        //
        // Strip the ZDE segment (or not)

        StandardInteractClientTopologyEndpointPort endpointPort = (StandardInteractClientTopologyEndpointPort) camelExchange.getProperty(PetasosPropertyConstants.ENDPOINT_TOPOLOGY_NODE_EXCHANGE_PROPERTY);
        if(endpointPort == null){
            getLogger().error(".convertToMessage(): endpointPort not found in exchange!");
        }

        // See if the flag is set to leave the ZDE segment in
        String forwardZDESegmentFlag = endpointPort.getOtherConfigurationParameter(PetasosPropertyConstants.FORWARD_ZDE_SEGMENT);
        getLogger().debug(".convertToMessage(): forwardZDESegmentFlag-->{}", forwardZDESegmentFlag);
        boolean forwardSegment = false;
        if(StringUtils.isNotEmpty(forwardZDESegmentFlag)){
            if(StringUtils.equalsIgnoreCase(forwardZDESegmentFlag, "true")){
                forwardSegment = true;
            }
        }
        getLogger().debug(".convertToMessage(): forwardSegment-->{}", forwardSegment);
        // Populate output string based on whether the zde inclusion flag is set
        String outputMessageAsString = null;
        if(forwardSegment){
            // we are to forward zde segments (if present), so merely copy the payload and forward
            outputMessageAsString = SerializationUtils.clone(messageAsString);
        } else {
            // we have to strip out the zde segment
            outputMessageAsString = zdeSegmentHelper.removeZDESegmentsIfPresent(messageAsString);
        }
        outputMessageAsString = checkForAndReinsertMediaObjects(outputMessageAsString);
        getLogger().debug(".convertToMessage(): Exit, outputMessageAsString->{}", outputMessageAsString);
        return (outputMessageAsString);
    }

    //
    // Business Methods
    //


    @VisibleForTesting
    String checkForAndReinsertMediaObjects(String messageAsString) {
        getLogger().debug(".checkForAndReinsertMediaObjects(): Entry, message->{}", messageAsString);
        //If media objects have been loaded into our system
        if(mediaParser.hasMatchingPatternInSegmentType(messageAsString, 
                "<fhir-resource>", HL7v2SegmentTypeEnum.OBX)) {
            getLogger().debug(".checkForAndReinsertMediaObjects(): Found fhir-resource in OBX");
            //Put the media objects back into the message
            //FIXME needs a loop and iteration for multiple media references.  These are not currently handled
            String segment = mediaParser.extractNextAlteredSegment(messageAsString);
            String mediaId = mediaParser.extractIdFromAlteredSegment(segment);
            if (mediaId != null) {
                getLogger().debug(".checkForAndReinsertMediaObjects(): Found fhir-resource in OBX: mediaId->{}", mediaId);
                Media media = mediaAgent.loadMedia(mediaId);
                if(media != null) {
                    getLogger().debug(".checkForAndReinsertMediaObjects(): Replacing media: mediaId->{}", mediaId);
                     String alteredMessage = mediaParser.replaceAlteredSegment(messageAsString, media);
                     if (alteredMessage.equals(messageAsString)) {
                         // nothing done
                         getLogger().error(".checkForAndReinsertMediaObjects(): failed to replace media reference for mediaId->{} in fragment->{}", mediaId, messageAsString);
                     }
                     messageAsString = alteredMessage;
                } else {
                    getLogger().error(".checkForAndReinsertMediaObjects(): Media should have returned! mediaId->{}", mediaId);
                }
            } else {
                //TODO we really need to retry or mark this as an unrecoverable error or something here as sending out with the link does not work
                getLogger().error(".checkForAndReinsertMediaObjects(): No media Id found in segment->{}", segment);
            }
        }
        getLogger().debug(".checkForAndReinsertMediaObjects(): Exit, message->{}", messageAsString);
        return (messageAsString);
    }
}
