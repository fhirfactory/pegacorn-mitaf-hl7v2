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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans;

import ca.uhn.hl7v2.model.Message;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.caches.ProcessingPlantAsynchronousCacheDM;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.common.HL7v2MessageInformationExtractor;
import net.fhirfactory.pegacorn.petasos.wup.helper.EgressActivityFinalisationRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MLLPAsynchronousMessageFinaliser {

    private static final Logger LOG = LoggerFactory.getLogger(MLLPAsynchronousMessageFinaliser.class);

    @Inject
    private ProcessingPlantAsynchronousCacheDM asynchronousACKCacheDM;

    @Inject
    private HL7v2MessageInformationExtractor hL7v2MessageExtractor;

    @Inject
    private EgressActivityFinalisationRegistration egressActivityFinalisationRegistration;

    @Inject
    private MLLPActivityAuditTrail mllpAuditTrail;

    public void awaitACKAndFinaliseMessage(Message answer, Exchange camelExchange) {
        LOG.debug(".awaitACKAndFinaliseMessage(): Entry, answer->{}", answer);

        CompletableFuture<UoW> future = new CompletableFuture<>();
        future.completeAsync(() -> {
            UoW uow = (UoW) camelExchange.getProperty(PetasosPropertyConstants.WUP_CURRENT_UOW_EXCHANGE_PROPERTY_NAME);
            String messageAsString = uow.getIngresContent().getPayload();
            String messageControlId = hL7v2MessageExtractor.extractMessageID(messageAsString);

            String acknowledgementMessage = asynchronousACKCacheDM.getAckMessage(messageControlId);
            LOG.warn("Get ACK message from asynchronous ACK cache: messageControlId->{}, ackMessage->{}", messageControlId, acknowledgementMessage);
            
            if (acknowledgementMessage != null) {
                uow = extractUoWAndAnswer(acknowledgementMessage, camelExchange, UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);
            } else {
                // Acknnowledgment message not received in the time alloted, finalise UoW outcome as failed.
                uow = extractUoWAndAnswer(acknowledgementMessage, camelExchange, UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED);
            }

            uow = mllpAuditTrail.logMLLPActivity(uow, camelExchange, "MLLPEgress", null);
            egressActivityFinalisationRegistration.registerActivityFinishAndFinalisation(uow, camelExchange, messageControlId);

            // Remove ACK entry from cache, finished processing.
            asynchronousACKCacheDM.removeAckMessage(messageControlId);
            
            return uow;
        }, CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS));

        LOG.debug(".awaitACKAndFinaliseMessage(): Exit, answer->{}", answer);
    }

    public UoW extractUoWAndAnswer(String answer, Exchange camelExchange, UoWProcessingOutcomeEnum outcome) {
        LOG.debug(".extractUoWAndAnswer(): Entry, answer->{}", answer);
        UoW uow = (UoW) camelExchange.getProperty(PetasosPropertyConstants.WUP_CURRENT_UOW_EXCHANGE_PROPERTY_NAME);
        UoWPayload payload = new UoWPayload();
        DataParcelManifest payloadTopicID = SerializationUtils.clone(uow.getPayloadTopicID());
        DataParcelTypeDescriptor descriptor = payloadTopicID.getContentDescriptor();
        descriptor.setDataParcelDiscriminatorType("Activity-Message-Exchange");
        descriptor.setDataParcelDiscriminatorValue("External-MLLP");
        String acknowledgeString = answer;

        // put in cache ...
        // Because auditing is not running yet
        // Remove once Auditing is in place
        //
        //getLogger().info("IncomingMessage-----------------------------------------------------------------");
        LOG.warn("Acknowledgement Message->{}", acknowledgeString); // Log at WARN level so always seen in TEST
        //getLogger().info("IncomingMessage-----------------------------------------------------------------");
        //
        //
        //
        payload.setPayload(acknowledgeString);
        payload.setPayloadManifest(payloadTopicID);
        uow.getEgressContent().addPayloadElement(payload);
        uow.setProcessingOutcome(outcome);

        LOG.debug(".extractUoWAndAnswer(): Exit, uow->{}", uow);
        return (uow);
    }
}
