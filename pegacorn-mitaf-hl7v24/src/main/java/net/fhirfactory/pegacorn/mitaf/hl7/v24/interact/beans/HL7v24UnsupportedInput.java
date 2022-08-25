package net.fhirfactory.pegacorn.mitaf.hl7.v24.interact.beans;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.AcknowledgmentCode;
import ca.uhn.hl7v2.ErrorCode;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

public class HL7v24UnsupportedInput {
    
    public static final String EXCHANGE_PROP_UNSUPPORTED_ERROR = "unsupportedErrorMessage";
    
    private static final Logger LOG = LoggerFactory.getLogger(HL7v24UnsupportedInput.class); 

    public Message buildUnsupportedNACK(Message message, Exchange exchange) throws HL7Exception, IOException {
        LOG.debug(".buildUnsupportedNACK(): Entry.  message -> {}", message);
        // get the error message
        Object errorMessageHeader = exchange.getProperty(EXCHANGE_PROP_UNSUPPORTED_ERROR);
        String errorMessage;
        if (errorMessageHeader != null) {
            errorMessage = errorMessageHeader.toString();
        } else {
            errorMessage = "Unspecified Error (this should never happen)";
        }
        HL7Exception unsupportedHL7 = new HL7Exception(errorMessage, ErrorCode.UNSUPPORTED_MESSAGE_TYPE);
        Message nack = message.generateACK(AcknowledgmentCode.AE, unsupportedHL7);
        LOG.debug(".buildUnsupportedNACK(): Entry.  nack message -> {}", nack);
        return nack;
    }
}
