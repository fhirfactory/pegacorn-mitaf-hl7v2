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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents;

import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.model.petasos.task.datatypes.fulfillment.valuesets.FulfillmentExecutionStatusEnum;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.wup.valuesets.PetasosTaskExecutionStatusEnum;
import net.fhirfactory.pegacorn.internals.hl7v2.segments.helpers.ZDESegmentHelper;
import net.fhirfactory.pegacorn.petasos.core.tasks.accessors.PetasosActionableTaskSharedInstance;
import net.fhirfactory.pegacorn.petasos.core.tasks.accessors.PetasosActionableTaskSharedInstanceAccessorFactory;
import net.fhirfactory.pegacorn.petasos.core.tasks.accessors.PetasosFulfillmentTaskSharedInstance;
import net.fhirfactory.pegacorn.petasos.core.tasks.management.local.LocalPetasosActionableTaskActivityController;
import net.fhirfactory.pegacorn.petasos.core.tasks.management.local.LocalPetasosFulfilmentTaskActivityController;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.time.Instant;

@Dependent
public class HL7v2xQueryClientQueryProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xQueryClientQueryProcessor.class);

    @Inject
    private LocalPetasosActionableTaskActivityController actionableTaskActivityController;

    @Inject
    private LocalPetasosFulfilmentTaskActivityController fulfilmentTaskActivityController;

    @Inject
    private PetasosActionableTaskSharedInstanceAccessorFactory actionableTaskSharedInstanceFactory;

    //
    // Constructor(s)
    //
    public HL7v2xQueryClientQueryProcessor() {
    }

    //
    // Getters (and Setters)
    //
    protected Logger getLogger() {
        return (LOG);
    }

    //
    // Business Methods
    //

    public PetasosFulfillmentTaskSharedInstance phase1ReportingProcessingStart(PetasosFulfillmentTaskSharedInstance fulfillmentTask, Exchange camelExchange) {
        getLogger().debug(".phase1ReportingProcessingStart(): Entry, fulfillmentTask->{}", fulfillmentTask);

        getLogger().debug("phase1ReportingProcessingStart(): [Update FulfillmentTask Status to Executing] Start");
        fulfillmentTask.setExecutionStatus(PetasosTaskExecutionStatusEnum.PETASOS_TASK_ACTIVITY_STATUS_EXECUTING);
        fulfillmentTask.getTaskFulfillment().setStartInstant(Instant.now());
        fulfillmentTask.getTaskFulfillment().setStatus(FulfillmentExecutionStatusEnum.FULFILLMENT_EXECUTION_STATUS_ACTIVE);
        fulfillmentTask.update();
        fulfilmentTaskActivityController.notifyFulfillmentTaskExecutionStart(fulfillmentTask);
        getLogger().debug("phase1ReportingProcessingStart(): [Update FulfillmentTask Status to Executing] Finish");

        getLogger().debug("phase1ReportingProcessingStart(): [Embed the FulfillmentTask into the Camel Exchange] Start");
        camelExchange.setProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY, fulfillmentTask);
        getLogger().debug("phase1ReportingProcessingStart(): [Embed the FulfillmentTask into the Camel Exchange] Finish");

        getLogger().debug("phase1ReportingProcessingStart(): [Update ActionableTask Status to Executing] Start");
        PetasosActionableTaskSharedInstance actionableTaskSharedInstance = actionableTaskSharedInstanceFactory.getActionableTaskSharedInstance(fulfillmentTask.getActionableTaskId());
        actionableTaskActivityController.notifyTaskStart(actionableTaskSharedInstance.getTaskId(), fulfillmentTask.getInstance());
        getLogger().debug("phase1ReportingProcessingStart(): [Update ActionableTask Status to Executing] Finish");

        getLogger().debug(".phase1ReportingProcessingStart(): Exit, fulfillmentTask->{}", fulfillmentTask);
        return (fulfillmentTask);
    }

    public PetasosFulfillmentTaskSharedInstance phase2EmbedFulfillmentTaskInExchange(PetasosFulfillmentTaskSharedInstance fulfillmentTask, Exchange camelExchange) {
        getLogger().debug(".phase2EmbedFulfillmentTaskInExchange(): Entry, fulfillmentTask->{}", fulfillmentTask);

        getLogger().debug("phase2EmbedFulfillmentTaskInExchange(): [Embed FulfillmentTask Into Exchange] Start");
        camelExchange.setProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY, fulfillmentTask);
        getLogger().debug("phase2EmbedFulfillmentTaskInExchange(): [Embed FulfillmentTask Into Exchange] Finish");

        getLogger().debug(".phase2EmbedFulfillmentTaskInExchange(): Exit, fulfillmentTask->{}", fulfillmentTask);
        return (fulfillmentTask);
    }

    public UoW phase3ExtractUoW(PetasosFulfillmentTaskSharedInstance fulfillmentTask, Exchange camelExchange) {
        getLogger().debug(".phase3ExtractUoW(): Entry, fulfillmentTask->{}", fulfillmentTask);

        getLogger().debug("phase3ExtractUoW(): [Extract (and Clone) UoW] Start");
        UoW uow = SerializationUtils.clone(fulfillmentTask.getTaskWorkItem());
        getLogger().debug("phase3ExtractUoW(): [Extract (and Clone) UoW] Finish");

        getLogger().debug(".phase3ExtractUoW(): Exit, uow->{}", uow);
        return (uow);
    }

    public String phase4ExtractMessageFromUoW(UoW incomingUoW, Exchange camelExchange) {
        getLogger().debug(".phase4ExtractMessageFromUoW(): Entry, incomingUoW->{}", incomingUoW);

        String messageAsString = incomingUoW.getIngresContent().getPayload();

        getLogger().info(".phase4ExtractMessageFromUoW(): OutgoingMessage--->>>" + messageAsString + "<<<---");

        getLogger().debug(".phase4ExtractMessageFromUoW(): Exit, messageAsString->{}", messageAsString);
        return (messageAsString);
    }
}
