/*
 * Copyright (c) 2021 Mark A. Hunter
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
package net.fhirfactory.pegacorn.mitaf.hl7.v24.interact.beans;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.segment.QRD;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.idgenerator.NanoTimeGenerator;
import net.fhirfactory.dricats.interfaces.capabilities.CapabilityUtilisationBrokerInterface;
import net.fhirfactory.dricats.model.capabilities.base.CapabilityUtilisationRequest;
import net.fhirfactory.dricats.model.capabilities.base.CapabilityUtilisationResponse;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class HL7v24TaskA19QueryClientHandler {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v24TaskA19QueryClientHandler.class);

    private HapiContext hapiContext;

    // ***********************************************************************************
    //
    // W A R N I N G: Tactical Solution for Short-Term Integration Support
    //
    private static String A19_CAPABILITY_PROVIDER = "aether-mitaf-bridges";
    //
    // ***********************************************************************************

    @Inject
    private CapabilityUtilisationBrokerInterface capabilityUtilisationBroker;

    @PostConstruct
    public void initialise(){
        this.hapiContext = new DefaultHapiContext();
    }

    public Message processA19Request(Message incomingRequest, Exchange exchange) {
        LOG.info(".processA19Request(): Entry Received Message");
        String queryString = "";
        String urn = "";
        try {
            String stringToPrint = incomingRequest.printStructure();
			//
			// Because auditing is not running yet
			// Remove once Auditing is in place
			//
            LOG.warn(".processA19Request(): IncomingMessage->{}", stringToPrint); // Log at WARN level so always seen in TEST
            QRD query = (QRD) incomingRequest.get("QRD");
            queryString = incomingRequest.encode();
            urn = query.getWhoSubjectFilter(0).getIDNumber().getValue();
            LOG.info(".processA19Request(): URN --> {}", urn);
        } catch (Exception ex) {
            LOG.warn(".processA19Request(): Something went wrong --> {}", ex);
        }
        Parser parser = getHAPIContext().getPipeParser();
        parser.getParserConfiguration().setValidating(false);
        parser.getParserConfiguration().setEncodeEmptyMandatoryFirstSegments(true);
        NanoTimeGenerator timeBasedIdGenerator = new NanoTimeGenerator();
        parser.getParserConfiguration().setIdGenerator(timeBasedIdGenerator);

        String queryResponse = utiliseA19QueryCapability(queryString);
        //
        // Because auditing is not running yet
        // Remove once Auditing is in place
        //
        LOG.warn(".processA19Request(): ResponseMessage->{}", queryResponse); // Log at WARN level so always seen in TEST
        try {
            Message resultMessage = parser.parse(queryResponse);
            String responseAsString = resultMessage.encode();
            exchange.setProperty("CamelMllpAcknowledgementString", responseAsString);
            return (resultMessage.getMessage());
        } catch (Exception ex) {
            LOG.info(".processA19Request(): Something went wrong with parsing --> {}", ex);
        }
        return(null);
    }

    private String utiliseA19QueryCapability( String queryString){
        LOG.info(".utiliseA19QueryCapability(): Entry, queryString --> {}", queryString);
        //
        // Build Query
        //
        CapabilityUtilisationRequest task = new CapabilityUtilisationRequest();
        task.setRequestID(UUID.randomUUID().toString());
        task.setRequestContent(queryString);
        task.setRequiredCapabilityName("A19QueryFulfillment");
        task.setRequestInstant(Instant.now());
        //
        // Do Query
        //
        CapabilityUtilisationResponse a19QueryTaskOutcome = capabilityUtilisationBroker.executeTask(A19_CAPABILITY_PROVIDER, task);
        //
        // Extract the response
        //
        String resultString = a19QueryTaskOutcome.getResponseStringContent();
        return(resultString);
    }


    //
    // Getters (and Setters)
    //


    public HapiContext getHAPIContext() {
        return hapiContext;
    }
}
