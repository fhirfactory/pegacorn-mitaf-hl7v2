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

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.interfaces.HL7v2xInformationExtractionInterface;
import net.fhirfactory.pegacorn.petasos.model.configuration.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWProcessingOutcomeEnum;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class HL7v2MessageAsTextToHL7V2xMessage {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2MessageAsTextToHL7V2xMessage.class);

    private HapiContext context;

    public HL7v2MessageAsTextToHL7V2xMessage(){
        context = new DefaultHapiContext();
    }

    @Inject
    HL7v2xInformationExtractionInterface informationExtractionInterface;

    public Message convertToMessage(UoW incomingUoW, Exchange camelExchange){
        LOG.debug(".convertToMessage(): Entry, incomingUoW->{}", incomingUoW);
        if(incomingUoW == null){
            UoW uowFromExchange = camelExchange.getProperty(PetasosPropertyConstants.WUP_CURRENT_UOW_EXCHANGE_PROPERTY_NAME, UoW.class);
            if(uowFromExchange == null){
                throw(new RuntimeException(".convertToMessage(): UoW is NULL, Petasos has Failed!"));
            } else {
                uowFromExchange.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED);
                uowFromExchange.setFailureDescription("UoW passed as Parameter is null");
                LOG.debug(".convertToMessage(): Exit, incomingUoW- is null");
                return(null);
            }
        }
        String messageAsText= incomingUoW.getIngresContent().getPayload();
        Message message = informationExtractionInterface.convertToHL7v2Message(messageAsText);
        LOG.debug(".convertToMessage(): Exit, message->{}", message);
        return(message);
    }
}
