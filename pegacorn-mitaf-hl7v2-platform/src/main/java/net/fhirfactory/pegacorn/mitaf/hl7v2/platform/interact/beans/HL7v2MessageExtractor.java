package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.interact.beans;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.petasos.model.uow.UoW;

@ApplicationScoped
public class HL7v2MessageExtractor {
	private static final Logger LOG = LoggerFactory.getLogger(HL7v2MessageExtractor.class);

	public String convertToMessage(UoW incomingUoW, Exchange exchange) {
		String messageAsString = incomingUoW.getIngresContent().getPayload();
		exchange.setProperty("ThisInstanceUoW", incomingUoW);
		return (messageAsString);
	}
}
