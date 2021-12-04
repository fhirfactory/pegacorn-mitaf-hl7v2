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

import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelNormalisationStatusEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelValidationStatusEnum;
import net.fhirfactory.pegacorn.core.model.petasos.task.PetasosFulfillmentTask;
import net.fhirfactory.pegacorn.petasos.audit.brokers.PetasosFulfillmentTaskAuditServicesBroker;
import net.fhirfactory.pegacorn.petasos.core.tasks.caches.processingplant.LocalPetasosFulfillmentTaskDM;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MLLPActivityAuditTrail {

    @Inject
    private LocalPetasosFulfillmentTaskDM parcelCacheDM;

    @Inject
    private PetasosFulfillmentTaskAuditServicesBroker servicesBroker;

    public UoW logMLLPActivity(UoW incomingUoW, Exchange camelExchange, String activity, String filtered) {
        PetasosFulfillmentTask fulfillmentTask = camelExchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY, PetasosFulfillmentTask.class);
        String portType = camelExchange.getProperty(PetasosPropertyConstants.WUP_INTERACT_PORT_TYPE, String.class);
        String portValue = camelExchange.getProperty(PetasosPropertyConstants.WUP_INTERACT_PORT_VALUE, String.class);
        if (portType != null && portValue != null) {
//            parcelInstance.setAssociatedPortValue(portValue);
//            parcelInstance.setAssociatedPortType(portType);
            UoW cloneUoW = SerializationUtils.clone(incomingUoW);
            servicesBroker.logMLLPTransactions(fulfillmentTask, activity, filtered, true);
        }
        return (incomingUoW);
    }

    public UoW logExceptionMLLPActivity(Object incoming, Exchange camelExchange){
        String portValue = camelExchange.getProperty(PetasosPropertyConstants.WUP_INTERACT_PORT_VALUE, String.class);
        String errorString = "MLLP ACK Exception: unknown";
        UoW uow = logExceptionError(incoming, camelExchange, "MLLP-ACK-Exception", errorString );
        return (uow);
    }

    public UoW logConnectionException(Object incoming, Exchange camelExchange){
        String portValue = camelExchange.getProperty(PetasosPropertyConstants.WUP_INTERACT_PORT_VALUE, String.class);
        String errorString = null;
        if(StringUtils.isEmpty(portValue)){
            errorString = "Could Not Connect to: unknown";
        } else {
            errorString = "Could Not Connect to:" + portValue;
        }
        UoW uow = logExceptionError(incoming, camelExchange, "ConnectionError", errorString );
        return (uow);
    }

    public UoW logExceptionError(Object incoming, Exchange camelExchange, String errorType, String errorText){
        PetasosFulfillmentTask fulfillmentTask = camelExchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY, PetasosFulfillmentTask.class);
        UoW uow = fulfillmentTask.getTaskWorkItem();
        String portType = camelExchange.getProperty(PetasosPropertyConstants.WUP_INTERACT_PORT_TYPE, String.class);
        String portValue = camelExchange.getProperty(PetasosPropertyConstants.WUP_INTERACT_PORT_VALUE, String.class);
        UoW updatedUoW = updateUoWWithExceptionDetails(uow, camelExchange);
        if (portType != null && portValue != null) {
//            parcelInstance.setAssociatedPortValue(portValue);
//            parcelInstance.setAssociatedPortType(portType);
            updatedUoW = updateUoWWithErrorDetails(uow, "ConnectionError", "Could Not Connect to:"+portValue);
            UoW cloneUoW = SerializationUtils.clone(updatedUoW);
            servicesBroker.logMLLPTransactions(fulfillmentTask, "Exception","false", true);
        }
        return (updatedUoW);
    }

    public UoW updateUoWWithErrorDetails(UoW incomingUoW, String errorType, String errorText){
        UoWPayload payload = new UoWPayload();
        DataParcelManifest egressManifest = SerializationUtils.clone(incomingUoW.getIngresContent().getPayloadManifest());
        DataParcelTypeDescriptor contentDescriptor = egressManifest.getContentDescriptor();
        contentDescriptor.setDataParcelDiscriminatorType("Error");
        contentDescriptor.setDataParcelDiscriminatorValue(errorType);
        payload.setPayloadManifest(egressManifest);
        payload.setPayload(errorText);
        incomingUoW.getEgressContent().addPayloadElement(payload);
        incomingUoW.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED);
        incomingUoW.setFailureDescription(errorText);
        return(incomingUoW);
    }

    public UoW updateUoWWithExceptionDetails(UoW incomingUoW, Exchange camelExchange){
        Exception caughtException = camelExchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        if(caughtException == null){
            return(incomingUoW);
        }
        DataParcelManifest exceptionManifest = new DataParcelManifest();
        DataParcelTypeDescriptor exceptionDescriptor = new DataParcelTypeDescriptor();
        exceptionDescriptor.setDataParcelDefiner("System");
        exceptionDescriptor.setDataParcelCategory("Exception");
        exceptionManifest.setContentDescriptor(exceptionDescriptor);
        exceptionManifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_TRUE);
        exceptionManifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATED_TRUE);
        exceptionManifest.setInterSubsystemDistributable(false);
        UoWPayload payload = new UoWPayload();
        payload.setPayloadManifest(exceptionManifest);
        String exceptionStackTrace = ExceptionUtils.getStackTrace(caughtException);
        payload.setPayload(exceptionStackTrace);
        incomingUoW.getEgressContent().addPayloadElement(payload);
        return(incomingUoW);
    }
}
