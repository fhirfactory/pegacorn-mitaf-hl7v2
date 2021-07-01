package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans;

import ca.uhn.fhir.parser.IParser;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.util.FHIRContextUtility;
import org.apache.camel.Exchange;
import org.hl7.fhir.r4.model.Communication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class UoWToFHIRCommunication {
    private static final Logger LOG = LoggerFactory.getLogger(UoWToFHIRCommunication.class);

    private IParser fhirParser;

    @PostConstruct
    public void initialise(){
        fhirParser = fhirContextUtility.getJsonParser().setPrettyPrint(true);
    }

    @Inject
    private FHIRContextUtility fhirContextUtility;

    public Communication extractCommunicationResource(UoW uow, Exchange camelExchange){
        LOG.debug(".extractCommunicationResource(): Entry, uow->{}", uow);
        LOG.trace(".extractCommunicationResource(): Extracting payload from uow (UoW)");
        String communicationAsString = uow.getIngresContent().getPayload();
        LOG.trace(".packageCommunicationResource(): Converting into (FHIR::Communication) from JSON String");
        Communication communication = (Communication)fhirParser.parseResource(communicationAsString);
        LOG.debug(".packageCommunicationResource(): Exit, communication->{}", communication);
        return(communication);
    }
}
