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

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.primitive.CommonTS;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.interfaces.HL7v2xInformationExtractionInterface;

@ApplicationScoped
public class HL7v2xMessageInformationExtractor implements HL7v2xInformationExtractionInterface {

    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xMessageInformationExtractor.class);

    private HapiContext hapiContext;

    public HL7v2xMessageInformationExtractor(){
        hapiContext = new DefaultHapiContext();
    }

    protected HapiContext getHapiContext() {
        return hapiContext;
    }

    @Override
    public String extractMessageID(Message message) {
    	Terser terser = new Terser(message);
    	
        LOG.debug(".extractMessageID(): Entry, messageAsText->{}", message);
        if(message != null) {
            try {
            	return terser.get("MSH-10");
            } catch (HL7Exception e) {
                LOG.warn(".extractMessageID(): Cannot extract MessageControlID, error -> {}", e.getMessage());
                return (null);
            }
        } else {
            return(null);
        }
    }

    @Override
    public String extractMessageID(String messageAsText) {
        LOG.debug(".extractMessageID(): Entry, messageAsText->{}", messageAsText);
        Message message = convertToHL7v2Message(messageAsText);
        String messageID = extractMessageID(message);
        return(messageID);
    }

    @Override
    public Date extractMessageDate(Message message) {
        LOG.debug(".extractMessageDate(): Entry, messageAsText->{}", message);
        
    	Terser terser = new Terser(message);
        
        if(message != null) {
            try {
            	String messageDate = terser.get("MSH-7-1");
            	CommonTS commonTs = new CommonTS(messageDate);
            	return commonTs.getValueAsDate();
            } catch (HL7Exception e) {
                LOG.warn(".extractMessageID(): Cannot extract DateTimeOfMessage, error -> {}", e.getMessage());
                return (null);
            }
        } else {
            return(null);
        }
    }

    @Override
    public Date extractMessageDate(String messageAsText) {
        LOG.debug(".extractMessageDate(): Entry, messageAsText->{}", messageAsText);
        Message message = convertToHL7v2Message(messageAsText);
        Date messageDate = extractMessageDate(message);
        return (messageDate);
    }

    @Override
    public String extractMessageVersion(Message message) {
        LOG.debug(".extractMessageVersion(): Entry, messageAsText->{}", message);
        
    	Terser terser = new Terser(message);
        
        if(message != null) {
            try {
                return terser.get("MSH-12-1");
            } catch (HL7Exception e) {
                LOG.warn(".extractMessageVersion(): Cannot extract MessageVersion, error -> {}", e.getMessage());
                return (null);
            }
        } else {
            return(null);
        }
    }

    @Override
    public String extractMessageVersion(String messageAsText) {
        LOG.debug(".extractMessageVersion(): Entry, messageAsText->{}", messageAsText);
        Message message = convertToHL7v2Message(messageAsText);
        String messageVersion = extractMessageVersion(message);
        return (messageVersion);
    }

    @Override
    public String extractMessageTrigger(Message message) {
        LOG.debug(".extractMessageTrigger(): Entry, message->{}", message);
        
    	Terser terser = new Terser(message);
        
        if(message != null) {
            try {
                return terser.get("MSH-9-2");
            } catch (HL7Exception e) {
                LOG.warn(".extractMessageTrigger(): Cannot extract Message Trigger Event, error -> {}", e.getMessage());
                return (null);
            }
        } else {
            return(null);
        }
    }

    @Override
    public String extractMessageTrigger(String messageAsText) {
        LOG.debug(".extractMessageTrigger(): Entry, messageAsText->{}", messageAsText);
        Message message = convertToHL7v2Message(messageAsText);
        String messageTrigger = extractMessageTrigger(message);
        return (messageTrigger);
    }

    @Override
    public String extractMessageType(Message message) {
        LOG.debug(".extractMessageType(): Entry, message->{}", message);
        
    	Terser terser = new Terser(message);
        
        if(message != null) {
            try {
                return terser.get("MSH-9-1");
            } catch (HL7Exception e) {
                LOG.warn(".extractMessageType(): Cannot extract Message Trigger Event, error -> {}", e.getMessage());
                return (null);
            }
        } else {
            return(null);
        }
    }

    @Override
    public String extractMessageType(String messageAsText) {
        LOG.debug(".extractMessageType(): Entry, messageAsText->{}", messageAsText);
        Message message = convertToHL7v2Message(messageAsText);
        String messageType = extractMessageType(message);
        return (messageType);
    }

    @Override
    public String extractMessageSource(Message message) {
        LOG.debug(".extractMessageDate(): Entry, message->{}", message);
        
    	Terser terser = new Terser(message);
        
        if(message != null) {
            try {
                return terser.get("MSH-3-2");
            } catch (HL7Exception e) {
                LOG.warn(".extractMessageID(): Cannot extract SendingApplication, error -> {}", e.getMessage());
                return (null);
            }
        } else {
            return(null);
        }
    }

    @Override
    public String extractMessageSource(String messageAsText) {
        LOG.debug(".extractMessageDate(): Entry, messageAsText->{}", messageAsText);
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
            LOG.warn(".extractMessageID(): Cannot encode Message to String, error -> {}", e.getMessage());
        }
        return(messageAsText);
    }

    @Override
    public Message convertToHL7v2Message(String messageText){
        LOG.debug(".convertToHL7v2Message(): Entry, messageText->{}", messageText);
        Parser parser = hapiContext.getPipeParser();
        parser.getParserConfiguration().setValidating(false);
        parser.getParserConfiguration().setEncodeEmptyMandatoryFirstSegments(true);
        try {
            Message hl7Msg = parser.parse(messageText);
            return(hl7Msg);
        } catch (HL7Exception e) {
            LOG.warn(".convertToHL7v2Message(): Cannot parse HL7 Message, error -> {}", e.getMessage());
            return(null);
        }
    }
}
