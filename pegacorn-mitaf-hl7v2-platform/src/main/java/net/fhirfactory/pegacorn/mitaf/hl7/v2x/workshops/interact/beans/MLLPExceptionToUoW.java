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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans;

import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelNormalisationStatusEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelValidationStatusEnum;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.petasos.core.tasks.accessors.PetasosFulfillmentTaskSharedInstance;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MLLPExceptionToUoW {
    private static final Logger LOG = LoggerFactory.getLogger(MLLPExceptionToUoW.class);

    //
    // Constructor(s)
    //

    //
    // Getters (and Setters)
    //

    protected Logger getLogger(){
        return(LOG);
    }

    //
    // Business Method(s)
    //

    public UoW updateUoWWithExceptionDetails(String message, Exchange camelExchange){
        getLogger().info(".updateUoWWithExceptionDetails(): Entry, message->{}", message);
        //
        // 1st Check to see if the Exception has been handled
        Exception caughtException = camelExchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        getLogger().trace(".updateUoWWithExceptionDetails(): caughtException->{}", caughtException);
        if(caughtException == null){
            getLogger().info(".updateUoWWithExceptionDetails(): Exception is null (already handled), returning null!");
            return(null);
        }
        //
        // Extract the fulfillmentTask from the exchange
        PetasosFulfillmentTaskSharedInstance fulfillmentTask = camelExchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY, PetasosFulfillmentTaskSharedInstance.class);
        //
        // Get the UoW from the fulfillmentTask (clone it...)
        UoW uow = SerializationUtils.clone(fulfillmentTask.getTaskWorkItem());
        //
        // Process the content (create "failed activity manifest for egress");
        DataParcelManifest exceptionManifest = new DataParcelManifest();
        DataParcelTypeDescriptor exceptionDescriptor = new DataParcelTypeDescriptor();
        exceptionDescriptor.setDataParcelDefiner("FHIRFactory");
        exceptionDescriptor.setDataParcelCategory("System");
        exceptionDescriptor.setDataParcelSubCategory("Exception");
        exceptionManifest.setContentDescriptor(exceptionDescriptor);
        exceptionManifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_TRUE);
        exceptionManifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATED_TRUE);
        exceptionManifest.setInterSubsystemDistributable(false);
        UoWPayload payload = new UoWPayload();
        payload.setPayloadManifest(exceptionManifest);
        String exceptionStackTrace = ExceptionUtils.getStackTrace(caughtException);
        String exceptionMessage = ExceptionUtils.getMessage(caughtException);
        String exceptionDescription = "Error: message-> "+ exceptionMessage + ", stack trace-> "+ exceptionStackTrace;
        payload.setPayload(exceptionDescription);
        uow.getEgressContent().addPayloadElement(payload);
        uow.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED);
        uow.setFailureDescription(exceptionDescription);

        getLogger().info(".updateUoWWithExceptionDetails(): Exit, uow->{}", uow);
        return(uow);
    }
}
