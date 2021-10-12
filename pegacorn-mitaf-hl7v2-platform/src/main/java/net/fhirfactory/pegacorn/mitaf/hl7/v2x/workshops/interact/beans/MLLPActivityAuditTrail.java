package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans;

import net.fhirfactory.pegacorn.petasos.audit.brokers.MOAServicesAuditBroker;
import net.fhirfactory.pegacorn.petasos.core.tasks.processingplant.cache.ProcessingPlantResilienceParcelCacheDM;
import net.fhirfactory.pegacorn.petasos.model.configuration.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.petasos.model.task.PetasosTaskOld;
import net.fhirfactory.pegacorn.petasos.model.task.segments.status.datatypes.TaskStatusType;
import net.fhirfactory.pegacorn.petasos.model.task.ResilienceParcel;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MLLPActivityAuditTrail {
    private static final Logger LOG = LoggerFactory.getLogger(MLLPActivityAuditTrail.class);

    @Inject
    private ProcessingPlantResilienceParcelCacheDM parcelCacheDM;

    @Inject
    private MOAServicesAuditBroker servicesBroker;

    public UoW logMLLPActivity(UoW incomingUoW, Exchange camelExchange) {
        LOG.debug(".logMLLPActivity(): Entry");
        PetasosTaskOld wupTransportPacket = camelExchange.getProperty(PetasosPropertyConstants.WUP_TRANSPORT_PACKET_EXCHANGE_PROPERTY_NAME, PetasosTaskOld.class);
        TaskStatusType statusElement = wupTransportPacket.getCurrentParcelStatus();
        ResilienceParcel parcelInstance = parcelCacheDM.getParcelInstance(statusElement.getParcelInstanceID());
        String portType = camelExchange.getProperty(PetasosPropertyConstants.WUP_INTERACT_PORT_TYPE, String.class);
        String portValue = camelExchange.getProperty(PetasosPropertyConstants.WUP_INTERACT_PORT_VALUE, String.class);
        if (portType != null && portValue != null) {
            parcelInstance.setAssociatedPortValue(portValue);
            parcelInstance.setAssociatedPortType(portType);
            UoW cloneUoW = SerializationUtils.clone(incomingUoW);
            servicesBroker.logMLLPTransactions(parcelInstance, cloneUoW, true);
        }
        LOG.debug(".logMLLPActivity(): Exit");
        return (incomingUoW);
    }
}
