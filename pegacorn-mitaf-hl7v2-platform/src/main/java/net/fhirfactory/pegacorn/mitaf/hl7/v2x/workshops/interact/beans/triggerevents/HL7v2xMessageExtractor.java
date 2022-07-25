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
import org.hl7.fhir.r4.model.Media;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import net.fhirfactory.pegacorn.core.interfaces.media.PetasosMediaServiceAgentInterface;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.internals.hl7v2.helpers.MediaPipeParser;
import net.fhirfactory.pegacorn.internals.hl7v2.triggerevents.valuesets.HL7v2SegmentTypeEnum;

@Dependent
public class HL7v2xMessageExtractor {
	private static final Logger LOG = LoggerFactory.getLogger(HL7v2xMessageExtractor.class);

	@Inject
	private MediaPipeParser mediaParser;

	@Inject
	private PetasosMediaServiceAgentInterface mediaAgent;

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



    //
	// Business Methods
	//

	public String convertToMessage(UoW incomingUoW, Exchange camelExchange) {
		getLogger().debug(".convertToMessage(): Entry, incomingUoW->{}", incomingUoW);

		String messageAsString = incomingUoW.getIngresContent().getPayload();
		messageAsString = checkForAndReinsertMediaObjects(messageAsString);
		getLogger().info("OutgoingMessage-----------------------------------------------------------------");
		getLogger().info("OutgoingMessage->{}", messageAsString);
		getLogger().info("OutgoingMessage-----------------------------------------------------------------");

		getLogger().debug(".convertToMessage(): Exit, messageAsString->{}", messageAsString);
		return (messageAsString);
	}

	@VisibleForTesting
	String checkForAndReinsertMediaObjects(String messageAsString) {
		//If media objects have been loaded into our system
		if(mediaParser.hasMatchingPatternInSegmentType(messageAsString, 
				"<fhir-resource>", HL7v2SegmentTypeEnum.OBX)) {
			
			//Put the media objects back into the message
			String segment = mediaParser.extractNextAlteredSegment(messageAsString);
			String mediaId = mediaParser.extractIdFromAlteredSegment(segment);
			Media media = mediaAgent.loadMedia(mediaId);
//			mediaParser.media.getContent()
		}
		return (messageAsString);
	}
}
