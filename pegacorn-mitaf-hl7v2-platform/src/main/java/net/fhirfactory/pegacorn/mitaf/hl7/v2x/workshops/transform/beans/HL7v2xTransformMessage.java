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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans;

import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelNormalisationStatusEnum;
import net.fhirfactory.pegacorn.core.model.petasos.task.PetasosFulfillmentTask;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

/**
 * Transforms a HL7 message
 *
 * @author Brendan Douglas
 *
 */
@Deprecated
@ApplicationScoped
public class HL7v2xTransformMessage {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xTransformMessage.class);

    protected Logger getLogger(){return(LOG);}

    /**
     * Adds the message to the original unit of work and sets the processing outcomes to no processing required if the message was filtered.
     *
     * @param exchange
     * @param message
     */
    public UoW postTransformProcessing(Message message, Exchange exchange) {
        getLogger().debug(".postTransformProcessing(): Entry, message->{}", message);
        //
        // We embed the fulfillmentTask within the exchange as part of Petasos framework
        PetasosFulfillmentTask fulfillmentTask = (PetasosFulfillmentTask) exchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY);
        //
        // The UoW is extracted from the fulfillmentTask.
        UoW uow = SerializationUtils.clone(fulfillmentTask.getTaskWorkItem());
    	uow.getEgressContent().getPayloadElements().clear();

        UoWPayload newPayload = new UoWPayload();
        DataParcelManifest newManifest = uow.getIngresContent().getPayloadManifest();

        newManifest.setContainerDescriptor(null);
        newManifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_TRUE);

        newPayload.setPayload(message.toString());
        newPayload.setPayloadManifest(newManifest);

        uow.getEgressContent().addPayloadElement(newPayload);
        uow.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);

        Boolean sendMessage = (Boolean) exchange.getProperty("sendMessage");

        if (!sendMessage) {
            fulfillmentTask.getTaskFulfillment().setToBeDiscarded(true);
            uow.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_NO_PROCESSING_REQUIRED);
        }

        getLogger().debug(".postTransformProcessing(): Exit, uow->{}", uow);
        return uow;
    }
}
