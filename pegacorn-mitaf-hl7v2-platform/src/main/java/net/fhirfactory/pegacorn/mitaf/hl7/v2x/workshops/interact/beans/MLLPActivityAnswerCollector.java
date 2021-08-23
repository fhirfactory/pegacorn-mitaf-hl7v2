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

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.components.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.components.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.petasos.model.configuration.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWProcessingOutcomeEnum;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MLLPActivityAnswerCollector {
    private static final Logger LOG = LoggerFactory.getLogger(MLLPActivityAnswerCollector.class);

    private HapiContext context = new DefaultHapiContext();

    public UoW extractUoWAndAnswer(Message answer, Exchange exchange){
        LOG.debug(".extractUoWAndAnswer(): Entry, answer->{}", answer);
        UoW uow = (UoW)exchange.getProperty(PetasosPropertyConstants.WUP_CURRENT_UOW_EXCHANGE_PROPERTY_NAME);
        UoWPayload payload = new UoWPayload();
        DataParcelManifest payloadTopicID = SerializationUtils.clone(uow.getPayloadTopicID());
        DataParcelTypeDescriptor descriptor = payloadTopicID.getContentDescriptor();
        descriptor.setDataParcelDiscriminatorType("Activity-Message-Exchange");
        descriptor.setDataParcelDiscriminatorValue("External-MLLP");
        String acknowledgeString = (String)exchange.getMessage().getHeader("CamelMllpAcknowledgementString");
        //
        // Because auditing is not running yet
        // Remove once Auditing is in place
        //
        //LOG.info("ResponseMessage-----------------------------------------------------------------");
        LOG.warn("ResponseMessage->{}", acknowledgeString); // Log at WARN level so always seen in TEST
        //LOG.info("ResponseMessage-----------------------------------------------------------------");
        //
        //
        //
        payload.setPayload(acknowledgeString);
        payload.setPayloadManifest(payloadTopicID);
        uow.getEgressContent().addPayloadElement(payload);
        uow.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);
        LOG.debug(".extractUoWAndAnswer(): Exit, uow->{}", uow);
        return(uow);
    }
}
