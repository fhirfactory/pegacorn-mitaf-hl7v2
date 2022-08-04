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

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Media;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.ORU_R01;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.media.factories.MediaFactory;
import net.fhirfactory.pegacorn.internals.hl7v2.helpers.MediaPipeParser;
import net.fhirfactory.pegacorn.internals.hl7v2.interfaces.HL7v2xInformationExtractionInterface;

@ApplicationScoped
public class HL7v2xMessageToFHIRMedia {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xMessageToFHIRMedia.class);

    @Inject
    private MediaFactory mediaFactory;
    
    @Inject
    private MediaPipeParser mediaParser;

    @Inject
    private HL7v2xInformationExtractionInterface messageInformationExtractionInterface;

    @PostConstruct
    public void initialise(){
    }
    
    public Media extractNextMediaResource(String message){
        LOG.debug(".extractMediaResource(): Entry, message->{}", message);
        Message encapsulatedMessage = new ORU_R01();
        try {
			encapsulatedMessage.parse(message);
		} catch (HL7Exception e) {
			LOG.warn(".extractMediaResource(): Message unable to be converted to ORU message->{}, exception->{}", message);
		}
        Media media = parseMessage(message);
        LOG.debug(".extractMediaResource(): Exit, media->{}", media);
        return(media);
    }


	private Media parseMessage(String message) {
        String messageID = messageInformationExtractionInterface.extractMessageID(message);
        Date messageDate = messageInformationExtractionInterface.extractMessageDate(message);

		Media media = mediaFactory.newMediaResource(messageID, messageDate);
		byte[] data = extractMediaFromORU(message);
		if(data == null || data.length == 0) {
			return null;
		}
		media.getContent().setData(data);
		String type = extractContentTypeFromORU(message);
		media.getContent().setContentType(type );
		return media;
	}

	private String extractContentTypeFromORU(String message) {
		String obxSegment = mediaParser.extractNextAttachmentSegment(message);
		if(StringUtils.isEmpty(obxSegment)) {
			return null; //No media to extract
		}
		String[] segments = mediaParser.breakSegmentIntoChunks(obxSegment);
		String obx5 = segments[5];
		String prefix = obx5.split("\\^Base64\\^")[0];
		prefix = mediaParser.hl7ToContentType(prefix);
		return prefix;
	}
	
	private byte[] extractMediaFromORU(String message) {	
			String obxSegment = mediaParser.extractNextAttachmentSegment(message);
			if(StringUtils.isEmpty(obxSegment)) {
				return null; //No media to extract
			}
			String[] segments = mediaParser.breakSegmentIntoChunks(obxSegment);
			String obx5 = segments[5];
			String suffix = obx5.split("\\^Base64\\^")[1];
			return suffix.getBytes();
		
	}

    @VisibleForTesting
    void setMessageInformationExtractionInterface(HL7v2xInformationExtractionInterface messageInformationExtractionInterface) {
    	this.messageInformationExtractionInterface = messageInformationExtractionInterface;
    }
    
    @VisibleForTesting
    void setMediaFactory(MediaFactory mediaFactory) {
    	this.mediaFactory = mediaFactory;
    }

    @VisibleForTesting
	void setMediaParser(MediaPipeParser mediaParser) {
		this.mediaParser = mediaParser;
	}

}
