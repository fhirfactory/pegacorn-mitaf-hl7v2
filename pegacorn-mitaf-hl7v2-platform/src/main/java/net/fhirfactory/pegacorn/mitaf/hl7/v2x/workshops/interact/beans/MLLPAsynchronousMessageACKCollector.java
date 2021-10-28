package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.ACK;
import ca.uhn.hl7v2.model.v24.segment.MSA;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import org.apache.camel.Exchange;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.HL7v2MessageAsTextToHL7V2xMessage;
import net.fhirfactory.pegacorn.petasos.core.common.resilience.processingplant.cache.ProcessingPlantAsynchronousCacheDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MLLPAsynchronousMessageACKCollector {
    private static final Logger LOG = LoggerFactory.getLogger(MLLPAsynchronousMessageACKCollector.class);

    @Inject
    private ProcessingPlantAsynchronousCacheDM asynchronousACKCacheDM;

    @Inject
    private HL7v2MessageAsTextToHL7V2xMessage hL7v2MessageAsTextToHL7V2xMessage;

    public UoW extractAndSaveACKMessage(UoW incomingUoW, Exchange camelExchange) {
        LOG.warn(".extractAndSaveACKMessage(): Entry, incomingUoW->{}", incomingUoW);
        String messageAsString = incomingUoW.getIngresContent().getPayload();        
        Message message = hL7v2MessageAsTextToHL7V2xMessage.convertToMessage(incomingUoW, camelExchange);        
        ACK ackMessage = (ACK) message;        
        MSA msa = ackMessage.getMSA();

        String messageControlId = msa.getMessageControlID().getValueOrEmpty();
        asynchronousACKCacheDM.addAckMessage(messageControlId, messageAsString);
        LOG.warn("Add ACK message to asynchronous ACK cache: messageControlId->{}, ackMessage->{}", messageControlId, messageAsString);

        return (incomingUoW);
    }
}
