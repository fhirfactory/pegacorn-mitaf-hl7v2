/*
 * Copyright (c) 2021 ACT Health
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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import net.fhirfactory.pegacorn.components.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.components.dataparcel.valuesets.DataParcelNormalisationStatusEnum;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.BaseMessageTransform;
import net.fhirfactory.pegacorn.petasos.model.configuration.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.util.FHIRContextUtility;

/**
 * Transforms a HL7 message
 * 
 * @author Brendan Douglas
 *
 */
@ApplicationScoped
public class HL7v2xTransformMessage {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xTransformMessage.class);

    private IParser fhirResourceParser;

    protected Logger getLogger(){return(LOG);}

    @Inject
    protected FHIRContextUtility fhirContextUtility;
    
    @Inject
    protected BaseMessageTransform messageTransform;

    @PostConstruct
    public void initialise(){
        fhirResourceParser = fhirContextUtility.getJsonParser().setPrettyPrint(true);
    }
   
    public UoW transformMessage(UoW uow) throws IOException, HL7Exception {
        getLogger().info(".transformMessage(): Entry, uow->{}", uow);
        
        
        String hl7Message = null;
        
    	// get the first and only payload element.
    	for (UoWPayload payload : uow.getEgressContent().getPayloadElements()) {
    		hl7Message = payload.getPayload();
    		break;
    	}

        // Transform the message
        Message message = messageTransform.doEgressTransform(hl7Message);
                
        getLogger().trace(".transformMessage(): Create the egress payload (UoWPayload) to contain the message");
        UoWPayload newPayload = new UoWPayload();

        DataParcelManifest newManifest = SerializationUtils.clone(uow.getIngresContent().getPayloadManifest());

        getLogger().trace(".transformMessage(): Now, set the containerDescriptor to null, as we've removed payload from the Communication resource");
        newManifest.setContainerDescriptor(null);
        newManifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_TRUE);

        getLogger().trace(".transformMessage(): Populate the new Egress payload object");
        newPayload.setPayload(message.toString());
        newPayload.setPayloadManifest(newManifest);

        getLogger().trace(".transformMessage(): Add the new Egress payload to the UoW");
        
        
        UoW newUoW = new UoW(uow);
        newUoW.getEgressContent().getPayloadElements().clear();
        newUoW.getEgressContent().addPayloadElement(newPayload);

        getLogger().trace(".transformMessage(): Assign the processing outcome to the UoW");
        newUoW.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);

        getLogger().debug(".transformMessage(): Exit, uow->{}", newUoW);
        return (newUoW);
    }
    
    
    
    /**
     * Sets the HL7 message from the unit of work payload as the exchange message body to be use in the transformation component.
     * 
     * @param uow
     * @return
     * @throws IOException
     * @throws HL7Exception
     */
    public Message setHL7MessageAsExchangeProperty(UoW uow, Exchange exchange) throws IOException, HL7Exception {
    	String hl7Message = null;
         
    	// get the first and only payload element.
    	for (UoWPayload payload : uow.getEgressContent().getPayloadElements()) {
    		hl7Message = payload.getPayload();
	     	break;
    	}
     	
     	
    	try (HapiContext hapiContext = new DefaultHapiContext();) {            
    		PipeParser parser = hapiContext.getPipeParser();
     		parser.getParserConfiguration().setValidating(false);
    
     		ModelClassFactory cmf = new DefaultModelClassFactory();
     		hapiContext.setModelClassFactory(cmf);

     		Message message = parser.parse(hl7Message);
     		 
            exchange.setProperty("hl7Message", message);
            
     		return parser.parse(hl7Message); 	            
    	}              	
    }

    
    /**
     * Adds the message to the original unit of work and sets the processing outcomes to no processing required if the message was filtered.
     * 
     * @param exchange
     * @param message
     */
    public UoW postTransformProcessing(Exchange exchange) {   	
    	
    	UoW uow = (UoW) exchange.getProperty(PetasosPropertyConstants.WUP_CURRENT_UOW_EXCHANGE_PROPERTY_NAME);
    	uow.getEgressContent().getPayloadElements().clear();
		    
        UoWPayload newPayload = new UoWPayload();

        DataParcelManifest newManifest = SerializationUtils.clone(uow.getIngresContent().getPayloadManifest());

        newManifest.setContainerDescriptor(null);
        newManifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_TRUE);

        newPayload.setPayload( exchange.getProperty("hl7Message").toString());
        newPayload.setPayloadManifest(newManifest);
        
        
        uow.getEgressContent().getPayloadElements().clear();
        uow.getEgressContent().addPayloadElement(newPayload);
        uow.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);
        
        exchange.getMessage().setBody(uow);
        exchange.getIn().setBody(uow);
        
        String sendMessage = (String)exchange.getProperty("sendMessage");
        
        if (!sendMessage.isBlank() && !Boolean.valueOf(sendMessage)) {
        	uow.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_NO_PROCESSING_REQUIRED);
        }
        
        return uow;
    }
}