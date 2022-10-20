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

import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.model.petasos.task.PetasosFulfillmentTask;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.petasos.audit.brokers.PetasosFulfillmentTaskAuditServicesBroker;
import net.fhirfactory.pegacorn.petasos.core.tasks.registries.LocalFulfillmentTaskRegistry;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MLLPActivityAuditTrail {

    @Inject
    private LocalFulfillmentTaskRegistry parcelCacheDM;

    @Inject
    private PetasosFulfillmentTaskAuditServicesBroker servicesBroker;

    public UoW logMLLPActivity(UoW incomingUoW, Exchange camelExchange, String filtered) {
        PetasosFulfillmentTask fulfillmentTask = camelExchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY, PetasosFulfillmentTask.class);
        PetasosFulfillmentTask clonedFulfillmentTask = SerializationUtils.clone(fulfillmentTask);
        servicesBroker.logMLLPTransactions(clonedFulfillmentTask, filtered, true);
        return (incomingUoW);
    }

    public UoW logMLLPActivity(UoW incomingUoW, Exchange camelExchange) {
        PetasosFulfillmentTask fulfillmentTask = camelExchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY, PetasosFulfillmentTask.class);
        PetasosFulfillmentTask clonedFulfillmentTask = SerializationUtils.clone(fulfillmentTask);
        servicesBroker.logMLLPTransactions(clonedFulfillmentTask, "false", true);
        return (incomingUoW);
    }

}
