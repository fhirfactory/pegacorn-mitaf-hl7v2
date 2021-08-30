package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans;

import net.fhirfactory.pegacorn.petasos.audit.brokers.MOAServicesAuditBroker;
import net.fhirfactory.pegacorn.petasos.core.common.resilience.processingplant.cache.ProcessingPlantParcelCacheDM;
import net.fhirfactory.pegacorn.petasos.model.configuration.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.petasos.model.resilience.activitymatrix.moa.ParcelStatusElement;
import net.fhirfactory.pegacorn.petasos.model.resilience.parcel.ResilienceParcel;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MLLPActivityAuditTrail {

    @Inject
    private ProcessingPlantParcelCacheDM parcelCacheDM;

    @Inject
    private MOAServicesAuditBroker servicesBroker;

    public UoW logMLLPActivity(UoW incomingUoW, Exchange camelExchange) {
        ParcelStatusElement statusElement = camelExchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_PARCEL_STATUS_EXCHANGE_PROPERTY_NAME, ParcelStatusElement.class);
        ResilienceParcel parcelInstance = parcelCacheDM.getParcelInstance(statusElement.getParcelInstanceID());
        String portType = camelExchange.getProperty(PetasosPropertyConstants.WUP_INTERACT_PORT_TYPE, String.class);
        String portValue = camelExchange.getProperty(PetasosPropertyConstants.WUP_INTERACT_PORT_VALUE, String.class);
        if (portType != null && portValue != null) {
            parcelInstance.setAssociatedPortValue(portValue);
            parcelInstance.setAssociatedPortType(portType);
            UoW cloneUoW = SerializationUtils.clone(incomingUoW);
            servicesBroker.logMLLPTransactions(parcelInstance, cloneUoW, true);
        }
        return (incomingUoW);
    }
}
