package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter.Filter;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayload;

@ApplicationScoped
public class HL7v2xMessageFilter {
	private static final Logger LOG = LoggerFactory.getLogger(HL7v2xMessageFilter.class);
	
	@Inject
	private Filter filter;


    public boolean filter(UoW incomingUoW, FilterType filterType) throws HL7Exception, IOException {
    	
    	
    	String hl7Message = null;
    	
    	// get the first and only payload element.
    	for (UoWPayload payload : incomingUoW.getEgressContent().getPayloadElements()) {
    		hl7Message = payload.getPayload();
    		break;
    	}
    	
    	// Convert to a Message and execute all the configured filters.
    	try (HapiContext context = new DefaultHapiContext();) {
			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
				
			return filter.doFilter(parser.parse(hl7Message), filterType);
		} 
    }
}
