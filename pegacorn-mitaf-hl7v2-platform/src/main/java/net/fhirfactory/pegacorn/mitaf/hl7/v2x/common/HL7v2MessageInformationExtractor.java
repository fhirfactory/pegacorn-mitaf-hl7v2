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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.common;

import java.util.Date;

import org.slf4j.Logger;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.datatype.HD;
import ca.uhn.hl7v2.model.v24.datatype.ST;
import ca.uhn.hl7v2.model.v24.datatype.TS;
import ca.uhn.hl7v2.model.v24.datatype.TSComponentOne;
import ca.uhn.hl7v2.model.v24.datatype.VID;
import ca.uhn.hl7v2.model.v24.segment.MSH;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.interfaces.HL7v2xInformationExtractionInterface;


public abstract class HL7v2MessageInformationExtractor implements HL7v2xInformationExtractionInterface {

    protected Logger getLogger(){
        return(specifyLogger());
    }

    abstract protected Logger specifyLogger();

    private HapiContext hapiContext;

    public HL7v2MessageInformationExtractor(){
        hapiContext = new DefaultHapiContext();
    }

    protected HapiContext getHapiContext() {
        return hapiContext;
    }

    @Override
    public String extractMessageID(Message messageAsText) {
    	Terser terser = new Terser(messageAsText);
    	
        getLogger().debug(".extractMessageID(): Entry, messageAsText->{}", messageAsText);
        if(messageAsText != null) {
            try {
            	String messageID = terser.get("MSH-10");
                return (messageID);
            } catch (HL7Exception e) {
                getLogger().warn(".extractMessageID(): Cannot extract MessageControlID, error -> {}", e.getMessage());
                return (null);
            }
        } else {
            return(null);
        }
    }

    @Override
    public String extractMessageID(String messageAsText) {
        getLogger().debug(".extractMessageID(): Entry, messageAsText->{}", messageAsText);
        Message message = convertToHL7v2Message(messageAsText);
        String messageID = extractMessageID(message);
        return(messageID);
    }

    @Override
    public Date extractMessageDate(Message messageAsText) {
        getLogger().debug(".extractMessageDate(): Entry, messageAsText->{}", messageAsText);
        if(messageAsText != null) {
            try {
                MSH messageHeader = (MSH) messageAsText.get("MSH");
                TS messageID = messageHeader.getDateTimeOfMessage();
                TSComponentOne timeOfAnEvent = messageID.getTs1_TimeOfAnEvent();
                Date date = timeOfAnEvent.getValueAsDate();
                return (date);
            } catch (HL7Exception e) {
                getLogger().warn(".extractMessageID(): Cannot extract DateTimeOfMessage, error -> {}", e.getMessage());
                return (null);
            }
        } else {
            return(null);
        }
    }

    @Override
    public Date extractMessageDate(String messageAsText) {
        getLogger().debug(".extractMessageDate(): Entry, messageAsText->{}", messageAsText);
        Message message = convertToHL7v2Message(messageAsText);
        Date messageDate = extractMessageDate(message);
        return (messageDate);
    }

    @Override
    public String extractMessageVersion(Message messageAsText) {
        getLogger().debug(".extractMessageVersion(): Entry, messageAsText->{}", messageAsText);
        if(messageAsText != null) {
            try {
                MSH messageHeader = (MSH) messageAsText.get("MSH");
                VID versionID = messageHeader.getVersionID();
                String versionIDString = versionID.getVersionID().getValue();
                return (versionIDString);
            } catch (HL7Exception e) {
                getLogger().warn(".extractMessageVersion(): Cannot extract MessageVersion, error -> {}", e.getMessage());
                return (null);
            }
        } else {
            return(null);
        }
    }

    @Override
    public String extractMessageVersion(String messageAsText) {
        getLogger().debug(".extractMessageVersion(): Entry, messageAsText->{}", messageAsText);
        Message message = convertToHL7v2Message(messageAsText);
        String messageVersion = extractMessageVersion(message);
        return (messageVersion);
    }

    @Override
    public String extractMessageTrigger(Message message) {
        getLogger().debug(".extractMessageTrigger(): Entry, message->{}", message);
        if(message != null) {
            try {
                MSH messageHeader = (MSH) message.get("MSH");
                String triggerEvent = messageHeader.getMessageType().getTriggerEvent().getValue();
                return (triggerEvent);
            } catch (HL7Exception e) {
                getLogger().warn(".extractMessageTrigger(): Cannot extract Message Trigger Event, error -> {}", e.getMessage());
                return (null);
            }
        } else {
            return(null);
        }
    }

    @Override
    public String extractMessageTrigger(String messageAsText) {
        getLogger().debug(".extractMessageTrigger(): Entry, messageAsText->{}", messageAsText);
        Message message = convertToHL7v2Message(messageAsText);
        String messageTrigger = extractMessageTrigger(message);
        return (messageTrigger);
    }

    @Override
    public String extractMessageType(Message message) {
        getLogger().debug(".extractMessageType(): Entry, message->{}", message);
        if(message != null) {
            try {
                MSH messageHeader = (MSH) message.get("MSH");
                String messageType = messageHeader.getMessageType().getMessageType().getValue();
                return (messageType);
            } catch (HL7Exception e) {
                getLogger().warn(".extractMessageType(): Cannot extract Message Trigger Event, error -> {}", e.getMessage());
                return (null);
            }
        } else {
            return(null);
        }
    }

    @Override
    public String extractMessageType(String messageAsText) {
        getLogger().debug(".extractMessageType(): Entry, messageAsText->{}", messageAsText);
        Message message = convertToHL7v2Message(messageAsText);
        String messageType = extractMessageType(message);
        return (messageType);
    }

    @Override
    public String extractMessageSource(Message message) {
        getLogger().debug(".extractMessageDate(): Entry, message->{}", message);
        if(message != null) {
            try {
                MSH messageHeader = (MSH) message.get("MSH");
                HD sendingApplication = messageHeader.getSendingApplication();
                ST universalID = sendingApplication.getUniversalID();
                return (universalID.getValue());
            } catch (HL7Exception e) {
                getLogger().warn(".extractMessageID(): Cannot extract SendingApplication, error -> {}", e.getMessage());
                return (null);
            }
        } else {
            return(null);
        }
    }

    @Override
    public String extractMessageSource(String messageAsText) {
        getLogger().debug(".extractMessageDate(): Entry, messageAsText->{}", messageAsText);
        Message message = convertToHL7v2Message(messageAsText);
        String messageSource = extractMessageSource(message);
        return (messageSource);
    }

    @Override
    public String convertMessageToString(Message message) {
        String messageAsText = null;
        try {
            messageAsText = message.encode();
        } catch (HL7Exception e) {
            getLogger().warn(".extractMessageID(): Cannot encode Message to String, error -> {}", e.getMessage());
        }
        return(messageAsText);
    }

    @Override
    public Message convertToHL7v2Message(String messageText){
        getLogger().debug(".convertToHL7v2Message(): Entry, messageText->{}", messageText);
        Parser parser = hapiContext.getPipeParser();
        parser.getParserConfiguration().setValidating(false);
        parser.getParserConfiguration().setEncodeEmptyMandatoryFirstSegments(true);
        try {
            Message hl7Msg = parser.parse(messageText);
            return(hl7Msg);
        } catch (HL7Exception e) {
            getLogger().warn(".convertToHL7v2Message(): Cannot parse HL7 Message, error -> {}", e.getMessage());
            return(null);
        }
    }
}
