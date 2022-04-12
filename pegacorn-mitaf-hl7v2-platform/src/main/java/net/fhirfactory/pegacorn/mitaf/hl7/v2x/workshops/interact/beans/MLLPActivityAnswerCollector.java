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

import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelExternallyDistributableStatusEnum;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.petasos.core.tasks.accessors.PetasosFulfillmentTaskSharedInstance;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgent;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgentAccessor;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.core.model.petasos.task.PetasosFulfillmentTask;

@ApplicationScoped
public class MLLPActivityAnswerCollector {
    private static final Logger LOG = LoggerFactory.getLogger(MLLPActivityAnswerCollector.class);

    @Inject
    private ProcessingPlantMetricsAgentAccessor processingPlantMetricsAgentAccessor;

    //
    // Getters (and Setters)
    //

    protected ProcessingPlantMetricsAgent getProcessingPlantMetricsAgent(){
        return(processingPlantMetricsAgentAccessor.getMetricsAgent());
    }

    public UoW extractUoWAndAnswer(Message answer, Exchange camelExchange){
        LOG.debug(".extractUoWAndAnswer(): Entry, answer->{}", answer);

        // We embed the fulfillmentTask within the exchange as part of Petasos framework
        PetasosFulfillmentTaskSharedInstance fulfillmentTask = (PetasosFulfillmentTaskSharedInstance) camelExchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY);
        //
        // The UoW is extracted from the fulfillmentTask.
        UoW uow = SerializationUtils.clone(fulfillmentTask.getTaskWorkItem());


        DataParcelManifest egressManifest = SerializationUtils.clone(uow.getPayloadTopicID());
        DataParcelTypeDescriptor egressContentDescriptor = egressManifest.getContentDescriptor();
        egressContentDescriptor.setDataParcelDiscriminatorType("Activity-Message-Exchange");
        egressContentDescriptor.setDataParcelDiscriminatorValue("External-MLLP");
        egressManifest.setContentDescriptor(egressContentDescriptor);
        egressManifest.setExternallyDistributable(DataParcelExternallyDistributableStatusEnum.DATA_PARCEL_EXTERNALLY_DISTRIBUTABLE_FALSE);
        String acknowledgeString = (String)camelExchange.getMessage().getHeader("CamelMllpAcknowledgementString");
        // Because auditing is not running yet
        // Remove once Auditing is in place
        //
        //getLogger().info("IncomingMessage-----------------------------------------------------------------");
        LOG.warn("Acknowledgement Message->{}", acknowledgeString); // Log at WARN level so always seen in TEST
        //getLogger().info("IncomingMessage-----------------------------------------------------------------");
        //
        //
        //
        UoWPayload egressPayload = new UoWPayload();
        egressPayload.setPayload(acknowledgeString);
        egressPayload.setPayloadManifest(egressManifest);
        uow.getEgressContent().addPayloadElement(egressPayload);
        uow.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);

        LOG.debug(".extractUoWAndAnswer(): Exit, uow->{}", uow);
        return(uow);
    }
}
