package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.Exchange;
import org.apache.camel.component.freemarker.FreemarkerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.Version;
import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.core.tasks.accessors.PetasosFulfillmentTaskSharedInstance;

/**
 * @author Brendan Douglas
 *
 */
@ApplicationScoped
public class FreeMarkerConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(FreeMarkerConfiguration.class);
	
	
	/**
	 * Configure Freemarker
	 * 
	 * @param uow
	 * @param exchange
	 * @throws HL7Exception
	 * @throws IOException
	 */
	public void configure(UoW uow, Exchange exchange) throws HL7Exception, IOException {	
		Message message = extractHL7Message(uow, exchange);
		configure(uow, message, exchange);
	}
	
	
	/**
	 * Configure Freemarker
	 * 
	 * @param message
	 * @param exchange
	 * @throws HL7Exception
	 * @throws IOException
	 */
	public void configure(Message message, Exchange exchange) throws HL7Exception, IOException {
		PetasosFulfillmentTaskSharedInstance fulfillmentTask = (PetasosFulfillmentTaskSharedInstance) exchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY);
		configure(fulfillmentTask.getTaskWorkItem(), message, exchange);
	}

        /**
         * Configure Freemarker; add required data, methods which will be used inside freemarker transformation files (*.ftl).
         *
         * @param uoW the unit of work being transformed.
         * @param message extracted message from the current unit of work.
         * @param exchange the camel exchange.
         */
        private void configure(UoW uoW, Message message, Exchange exchange) {
            Map<String, Object> variableMap = new HashMap<>();
            variableMap.put("uoW", uoW);
            variableMap.put("message", message);
            variableMap.put("exchange", exchange);
            // Set sendMessage property in the exchange to default of true, as is the case with most transformations, which require to be sent.
            exchange.setProperty("sendMessage", true);
            exchange.getIn().setHeader(FreemarkerConstants.FREEMARKER_DATA_MODEL, variableMap);
            BeansWrapper wrapper = new BeansWrapper(new Version(2, 3, 27));
            TemplateModel statics = wrapper.getStaticModels();

            variableMap.put("statics", statics);
        }
	
	
	/**
	 * Get the message out of the unit of work so it can be passed to Freemarker.
	 * 
	 * @param uow
	 * @param exchange
	 * @return
	 * @throws IOException
	 * @throws HL7Exception
	 */
	private Message extractHL7Message(UoW uow, Exchange exchange) throws IOException, HL7Exception {
		String hl7Message = null;

		// get the first and only payload element.
		for (UoWPayload payload : uow.getEgressContent().getPayloadElements()) {
			hl7Message = payload.getPayload();
					
			break;
		}
		
		
		// Maybe the message is on the ingres feed.
		if (hl7Message == null) {
			hl7Message = uow.getIngresContent().getPayload();
		}
		
		if (hl7Message == null) {
			throw new RuntimeException("Unable to extract the HL7 message");
		}
		
		try (HapiContext hapiContext = new DefaultHapiContext();) {
			PipeParser parser = hapiContext.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			hapiContext.setModelClassFactory(cmf);

			Message message = parser.parse(hl7Message);

			exchange.getMessage().setBody(message);
			exchange.getIn().setBody(message);

			return parser.parse(hl7Message);
		}
	}
	
	
	/**
	 * Converts the message string that was returned by freemarker to a Message.
	 * 
	 * @param message
	 * @return
	 * @throws IOException
	 * @throws HL7Exception
	 */
	public Message convertToMessage(String message, Exchange exchange) throws IOException, HL7Exception {
		try (HapiContext hapiContext = new DefaultHapiContext();) {
			PipeParser parser = hapiContext.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			hapiContext.setModelClassFactory(cmf);

			return parser.parse(message);
		}		
	}
}
