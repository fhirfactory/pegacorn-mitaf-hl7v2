package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

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
import net.fhirfactory.pegacorn.core.model.petasos.task.PetasosFulfillmentTask;
import net.fhirfactory.pegacorn.core.model.petasos.task.datatypes.work.datatypes.TaskWorkItemType;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import org.apache.camel.Exchange;
import org.apache.camel.component.freemarker.FreemarkerConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
	public void configure(UoW uow, Exchange exchange) throws HL7Exception, IOException, Exception {	
		Message message = extractHL7Message(uow, exchange);
		configure(uow, message, exchange, null);
	}
	
	
	/**
	 * Configure Freemarker.  The uow will contain a {@link HL7MessageWithAttributes}.  
	 * 
	 * @param uow
	 * @param exchange
	 * @throws HL7Exception
	 * @throws IOException
	 */
	public void configureFromJSONWithAdditionalAttributes(UoW uow, Exchange exchange) throws HL7Exception, IOException, Exception {	
		Message message = extractHL7MessageFromJSON(uow, exchange);
		Map<String, Object> hl7MessageAttributes = extractHL7MessageAttrbutesFromJSON(uow, exchange);
		configure(uow, message, exchange, hl7MessageAttributes);
	}
	
	
	
	/**
	 * Configure Freemarker
	 * 
	 * @param message
	 * @param exchange
	 * @throws HL7Exception
	 * @throws IOException
	 */
	public void configure(Message message, Exchange exchange) throws HL7Exception, IOException, Exception {
		PetasosFulfillmentTask fulfillmentTask = (PetasosFulfillmentTask) exchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY);
		TaskWorkItemType uow = null;
		if (fulfillmentTask != null) {
		    uow = fulfillmentTask.getTaskWorkItem();
		}
		configure(uow, message, exchange, null);
	}

	
	/**
     * Configure Freemarker; add required data, methods which will be used inside freemarker transformation files (*.ftl).
     *
     * @param uoW the unit of work being transformed.
     * @param message extracted message from the current unit of work.
     * @param exchange the camel exchange.
     */
    private void configure(UoW uoW, Message message, Exchange exchange, Map<String, Object> hl7MessageAttributes) throws Exception {
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("uoW", uoW);
        variableMap.put("message", message);
        variableMap.put("exchange", exchange);
        // Set sendMessage property in the exchange to default of true, as is the case with most transformations, which require to be sent.
        exchange.setProperty("sendMessage", true);
        
        if (hl7MessageAttributes != null) {
        	variableMap.putAll(hl7MessageAttributes);
        }
        
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
	 * Get the message out of the unit of work so it can be passed to Freemarker.
	 * 
	 * @param uow
	 * @param exchange
	 * @return
	 * @throws IOException
	 * @throws HL7Exception
	 */
	private Message extractHL7MessageFromJSON(UoW uow, Exchange exchange) throws IOException, HL7Exception, Exception {
		return getHL7MessageWithAttributes(uow, exchange).getMessage();
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
	private Map<String, Object> extractHL7MessageAttrbutesFromJSON(UoW uow, Exchange exchange) throws IOException, HL7Exception, Exception {	
		return getHL7MessageWithAttributes(uow, exchange).getAttributes();
	}

	
	private HL7MessageWithAttributes getHL7MessageWithAttributes(UoW uow, Exchange exchange) {
		String jsonMessageWithAttributes = null;

		// get the first and only payload element.
		for (UoWPayload payload : uow.getEgressContent().getPayloadElements()) {
			jsonMessageWithAttributes = payload.getPayload();
					
			break;
		}

		// Maybe the message is on the ingres feed.
		if (jsonMessageWithAttributes == null) {
			jsonMessageWithAttributes = uow.getIngresContent().getPayload();
		}
		
		if (jsonMessageWithAttributes == null) {
			throw new RuntimeException("Unable to extract the HL7 message with attributes");
		}
		
		return new HL7MessageWithAttributes(new JSONObject(jsonMessageWithAttributes));
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
