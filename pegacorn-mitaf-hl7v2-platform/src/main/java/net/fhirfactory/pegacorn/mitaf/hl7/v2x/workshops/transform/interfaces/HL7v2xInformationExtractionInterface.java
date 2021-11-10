package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.interfaces;

import ca.uhn.hl7v2.model.Message;

import java.util.Date;

public interface HL7v2xInformationExtractionInterface {
    public String extractMessageID(Message message);
    public String extractMessageID(String messageAsText);
    public Date extractMessageDate(Message message);
    public Date extractMessageDate(String messageAsText);
    public String extractMessageSource(Message message);
    public String extractMessageSource(String messageAsText);
    public String extractMessageVersion(Message message);
    public String extractMessageVersion(String messageAsText);
    public String convertMessageToString(Message message);
    public Message convertToHL7v2Message(String messageAsText);
    public String extractMessageTrigger(Message message);
    public String extractMessageTrigger(String messageAsText);
    public String extractMessageType(Message message);
    public String extractMessageType(String messageAsText);
}
