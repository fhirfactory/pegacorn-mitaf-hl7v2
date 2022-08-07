/*
 * Copyright (c) 2021 Mark A. Hunter
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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.mllp;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.ACK;
import ca.uhn.hl7v2.model.v24.segment.MSA;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.caches.ProcessingPlantAsynchronousCacheDM;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.HL7v2MessageAsTextToHL7V2xMessage;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MLLPAsynchronousMessageACKCollector {
    private static final Logger LOG = LoggerFactory.getLogger(MLLPAsynchronousMessageACKCollector.class);

    @Inject
    private ProcessingPlantAsynchronousCacheDM asynchronousACKCacheDM;

    @Inject
    private HL7v2MessageAsTextToHL7V2xMessage hL7v2MessageAsTextToHL7V2xMessage;

    public UoW extractAndSaveACKMessage(UoW incomingUoW, Exchange camelExchange) throws HL7Exception {
        LOG.warn(".extractAndSaveACKMessage(): Entry, incomingUoW->{}", incomingUoW);
        String messageAsString = incomingUoW.getIngresContent().getPayload();        
        Message message = hL7v2MessageAsTextToHL7V2xMessage.convertToMessage(incomingUoW, camelExchange);        
        ACK ackMessage = (ACK) message;        
        MSA msa = ackMessage.getMSA();

        String messageControlId = msa.getMessageControlID().getValueOrEmpty();
        asynchronousACKCacheDM.addAckMessage(messageControlId + "-ACK", messageAsString);
        LOG.warn("Add ACK message to asynchronous ACK cache: messageControlId->{}, ackMessage->{}", messageControlId, messageAsString);

        return (incomingUoW);
    }
}
