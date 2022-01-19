package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import ca.uhn.fhir.parser.IParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.component.freemarker.FreemarkerConstants;

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
import javax.annotation.PostConstruct;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.util.FHIRContextUtility;
import org.apache.commons.lang3.SerializationUtils;
import org.hl7.fhir.r4.model.Communication;

/**
 * @author Brendan Douglas
 *
 */
@ApplicationScoped
public class FreeMarkerConfiguration {

    private IParser fhirResourceParser;

    @Inject
    protected FHIRContextUtility fhirContextUtility;

    @Inject
    private BaseMessageTransform messageTransformation;

    public FreeMarkerConfiguration() {
    }
    
    @PostConstruct
    public void initialise(){
        fhirResourceParser = fhirContextUtility.getJsonParser().setPrettyPrint(true);
    }

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
        configure(message, exchange);
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
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("message", message);
        variableMap.put("exchange", exchange);
        variableMap.put("javaBasedTransformations", messageTransformation);
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

        String communicationAsString = uow.getIngresContent().getPayload();
        Communication communication = fhirResourceParser.parseResource(Communication.class, communicationAsString);
        Communication.CommunicationPayloadComponent communicationPayload = communication.getPayloadFirstRep();
        
        hl7Message = SerializationUtils.clone(communicationPayload.getContentStringType().getValue());

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
