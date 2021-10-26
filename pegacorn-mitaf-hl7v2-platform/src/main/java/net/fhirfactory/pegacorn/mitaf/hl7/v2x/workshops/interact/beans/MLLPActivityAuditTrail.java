package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans;

import net.fhirfactory.pegacorn.components.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.components.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.components.dataparcel.valuesets.DataParcelNormalisationStatusEnum;
import net.fhirfactory.pegacorn.components.dataparcel.valuesets.DataParcelValidationStatusEnum;
import net.fhirfactory.pegacorn.petasos.audit.brokers.MOAServicesAuditBroker;
import net.fhirfactory.pegacorn.petasos.core.tasks.caches.processingplant.LocalPetasosFulfillmentTaskDM;
import net.fhirfactory.pegacorn.petasos.model.configuration.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.moa.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcel;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWProcessingOutcomeEnum;
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
    private MOAServicesAuditBroker servicesBroker;

    public UoW logMLLPActivity(UoW incomingUoW, Exchange camelExchange, String activity, String filtered) {
        ParcelStatusElement statusElement = camelExchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_PARCEL_STATUS_EXCHANGE_PROPERTY_NAME, ParcelStatusElement.class);
        ResilienceParcel parcelInstance = parcelCacheDM.getFulfillmentTask(statusElement.getParcelInstanceID());
        String portType = camelExchange.getProperty(PetasosPropertyConstants.WUP_INTERACT_PORT_TYPE, String.class);
        String portValue = camelExchange.getProperty(PetasosPropertyConstants.WUP_INTERACT_PORT_VALUE, String.class);
        if (portType != null && portValue != null) {
            parcelInstance.setAssociatedPortValue(portValue);
            parcelInstance.setAssociatedPortType(portType);
            UoW cloneUoW = SerializationUtils.clone(incomingUoW);
            servicesBroker.logMLLPTransactions(parcelInstance, cloneUoW, activity, filtered, true);
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
        UoW uow = camelExchange.getProperty(PetasosPropertyConstants.WUP_CURRENT_UOW_EXCHANGE_PROPERTY_NAME, UoW.class);
        ParcelStatusElement statusElement = camelExchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_PARCEL_STATUS_EXCHANGE_PROPERTY_NAME, ParcelStatusElement.class);
        ResilienceParcel parcelInstance = parcelCacheDM.getFulfillmentTask(statusElement.getParcelInstanceID());
        String portType = camelExchange.getProperty(PetasosPropertyConstants.WUP_INTERACT_PORT_TYPE, String.class);
        String portValue = camelExchange.getProperty(PetasosPropertyConstants.WUP_INTERACT_PORT_VALUE, String.class);
        UoW updatedUoW = updateUoWWithExceptionDetails(uow, camelExchange);
        if (portType != null && portValue != null) {
            parcelInstance.setAssociatedPortValue(portValue);
            parcelInstance.setAssociatedPortType(portType);
            updatedUoW = updateUoWWithErrorDetails(uow, "ConnectionError", "Could Not Connect to:"+portValue);
            UoW cloneUoW = SerializationUtils.clone(updatedUoW);
            servicesBroker.logMLLPTransactions(parcelInstance, cloneUoW, "Exception","false", true);
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
