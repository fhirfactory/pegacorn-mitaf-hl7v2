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

import ca.uhn.hl7v2.model.Message;

import net.fhirfactory.pegacorn.internals.hl7v2.helpers.HL7v2xMessageInformationExtractor;
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
import net.fhirfactory.pegacorn.petasos.core.tasks.accessors.PetasosFulfillmentTaskSharedInstance;
import net.fhirfactory.pegacorn.petasos.wup.helper.EgressActivityFinalisationRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MLLPAsynchronousMessageFinaliser {

    private static final Logger LOG = LoggerFactory.getLogger(MLLPAsynchronousMessageFinaliser.class);

    @Inject
    private ProcessingPlantAsynchronousCacheDM asynchronousACKCacheDM;

    @Inject
    private HL7v2xMessageInformationExtractor hL7v2MessageExtractor;

    @Inject
    private EgressActivityFinalisationRegistration egressActivityFinalisationRegistration;

    @Inject
    private MLLPActivityAuditTrail mllpAuditTrail;

    @Inject
    private MLLPEgressMessageMetricsCapture metricsCapture;

    public String extractUoW(String answer, Exchange camelExchange) {
        
        String messageControlId = hL7v2MessageExtractor.extractMessageID(answer);
        asynchronousACKCacheDM.addAckMessage(messageControlId + "-MSG", answer);
        
        return answer;
    }
    
    public UoW extractUoWAndAnswer(Message answer, Exchange camelExchange) {
        LOG.debug(".extractUoWAndAnswer(): Entry, answer->{}", answer);
        PetasosFulfillmentTaskSharedInstance fulfillmentTask = camelExchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY, PetasosFulfillmentTaskSharedInstance.class);
        UoW uow = fulfillmentTask.getTaskWorkItem();
        UoWPayload payload = new UoWPayload();

        String messageAsString = uow.getIngresContent().getPayload();
        String messageControlId = hL7v2MessageExtractor.extractMessageID(messageAsString);

        String acknowledgementMessage = asynchronousACKCacheDM.getAckMessage(messageControlId + "-ACK");
        LOG.warn("Get ACK message from asynchronous ACK cache: messageControlId->{}, ackMessage->{}", messageControlId, acknowledgementMessage);

        UoWProcessingOutcomeEnum outcome = null;

        if (acknowledgementMessage != null) {
            outcome = UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS;
        } else {
            // Acknnowledgment message not received in the time alloted, finalise UoW outcome as failed.
            outcome = UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED;
        }

        DataParcelManifest payloadTopicID = SerializationUtils.clone(uow.getPayloadTopicID());
        DataParcelTypeDescriptor descriptor = payloadTopicID.getContentDescriptor();
        descriptor.setDataParcelDiscriminatorType("Activity-Message-Exchange");
        descriptor.setDataParcelDiscriminatorValue("External-MLLP");
        String acknowledgeString = acknowledgementMessage;

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

        // Remove ACK entry from cache, finished processing.
        asynchronousACKCacheDM.removeAckMessage(messageControlId);

        LOG.debug(".extractUoWAndAnswer(): Exit, uow->{}", uow);
        LOG.warn(".extractUoWAndAnswer(): Exit, Acknowledgement Message->{}", acknowledgeString);

        return (uow);
    }
}
