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

import org.apache.camel.Exchange;
import org.hl7.fhir.r4.model.Media;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v24.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v24.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v24.message.ORU_R01;
import ca.uhn.hl7v2.model.v24.segment.OBX;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.media.factories.MediaFactory;
import net.fhirfactory.pegacorn.internals.hl7v2.interfaces.HL7v2xInformationExtractionInterface;

@ApplicationScoped
public class HL7v2xMessageToFHIRMedia {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xMessageToFHIRMedia.class);

    @Inject
    private MediaFactory mediaFactory;

    @Inject
    private HL7v2xInformationExtractionInterface messageInformationExtractionInterface;

    @PostConstruct
    public void initialise(){
    }
    
    public Media extractMediaResource(String message){
        LOG.debug(".extractMediaResource(): Entry, message->{}", message);
        Message encapsulatedMessage = new ORU_R01();
        try {
			encapsulatedMessage.parse(message);
		} catch (HL7Exception e) {
			LOG.warn(".extractMediaResource(): Message unable to be converted to ORU message->{}, exception->{}", message);
		}
        Media media = extractMediaResource(encapsulatedMessage);
        LOG.debug(".extractMediaResource(): Exit, media->{}", media);
        return(media);
    }

    public Media extractMediaResource(Message message){
        LOG.debug(".extractMediaResource(): Entry, message->{}", message);
        Media media = parseResource(message);
        LOG.debug(".extractMediaResource(): Exit, media->{}", media);
        return(media);
    }

	private Media parseResource(Message message) {
        String messageID = messageInformationExtractionInterface.extractMessageID(message);
        Date messageDate = messageInformationExtractionInterface.extractMessageDate(message);

		Media media = mediaFactory.newMediaResource(messageID, messageDate);
		byte[] data = extractMediaFromORU(message);
		if(data == null) {
			return null;
		}
		media.getContent().setData(data);
		return media;
	}

	private byte[] extractMediaFromORU(Message message) {
		try {
			ORU_R01 oru = (ORU_R01) message;
			ORU_R01_PATIENT_RESULT patientResult = oru.getPATIENT_RESULT();
			ORU_R01_ORDER_OBSERVATION observationOrder = patientResult.getORDER_OBSERVATION();
			ORU_R01_OBSERVATION observation = observationOrder.getOBSERVATION();
			OBX obx = observation.getOBX();
			LOG.trace("OBX: ->{}", obx.encode());
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < obx.getObservationValueReps(); i++) {
				sb.append(obx.getObservationValue(i).encode());
			}
			LOG.trace("OBX output: ->{}", sb.substring(0));
			return sb.substring(0).getBytes();
		
		} catch (ClassCastException e) {
			LOG.warn("Not of the right type!");
		} catch (HL7Exception e) {
			LOG.warn("HL7 could not encode message ->{}", e.getMessage());
		}
		return null;
	}
    
    @VisibleForTesting
    void setMessageInformationExtractionInterface(HL7v2xInformationExtractionInterface messageInformationExtractionInterface) {
    	this.messageInformationExtractionInterface = messageInformationExtractionInterface;
    }
    
    @VisibleForTesting
    void setMediaFactory(MediaFactory mediaFactory) {
    	this.mediaFactory = mediaFactory;
    }

}
