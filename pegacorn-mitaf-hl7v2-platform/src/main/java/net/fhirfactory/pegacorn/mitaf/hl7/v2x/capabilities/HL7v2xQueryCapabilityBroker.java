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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.capabilities;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.core.interfaces.capabilities.CapabilityUtilisationBrokerInterface;
import net.fhirfactory.pegacorn.core.model.capabilities.base.CapabilityUtilisationRequest;
import net.fhirfactory.pegacorn.core.model.capabilities.base.CapabilityUtilisationResponse;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.UUID;
import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.model.petasos.task.PetasosFulfillmentTask;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.core.tasks.accessors.PetasosFulfillmentTaskSharedInstance;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class HL7v2xQueryCapabilityBroker {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xQueryCapabilityBroker.class);

    private HapiContext hapiContext;
    
    private boolean initialised;

    // ***********************************************************************************
    //
    // W A R N I N G: Tactical Solution for Short-Term Integration Support
    //
    private static String A19_CAPABILITY_PROVIDER = "MITaF.Bridges";
    //
    // ***********************************************************************************

    @Inject
    private CapabilityUtilisationBrokerInterface capabilityUtilisationBroker;
    
    //
    // Constructor
    //
    
    public HL7v2xQueryCapabilityBroker(){
        this.initialised = false;
    }
    
    //
    // PostConstruct
    //

    @PostConstruct
    public void initialise(){
        if(initialised){
            getLogger().debug(".initialise(): Nothing to do, already initialised");
        } else {
            this.hapiContext = new DefaultHapiContext();
        }
    }
    
    //
    // Business Methods
    //

    public String processQueryTask(Message incomingRequest, Exchange camelExchange) {
        LOG.info(".processA19Request(): Entry Received Message");
        
        // We embed the fulfillmentTask within the exchange as part of Petasos framework
        PetasosFulfillmentTaskSharedInstance fulfillmentTask = (PetasosFulfillmentTaskSharedInstance) camelExchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY);

        PetasosFulfillmentTask task = fulfillmentTask.getInstance();
        PetasosFulfillmentTask responseTask = utiliseRemoteQueryCapability(task);
        if(responseTask.getTaskWorkItem().hasEgressContent()){
            UoWPayload payload = responseTask.getTaskWorkItem().getEgressContent().getPayloadElements().stream().findFirst().get();
            if(payload != null){
                String responseString = payload.getPayload();
                if(StringUtils.isNotEmpty(responseString)){
                    camelExchange.setProperty("CamelMllpAcknowledgementString", responseString);
                    return (responseString);
                }
            }
        }
        return(null);
    }

    private PetasosFulfillmentTask utiliseRemoteQueryCapability( PetasosFulfillmentTask queryTask){
        LOG.info(".utiliseA19QueryCapability(): Entry, queryTask->{}", queryTask);
        //
        // Build Query
        //
        CapabilityUtilisationRequest task = new CapabilityUtilisationRequest();
        task.setRequestID(UUID.randomUUID().toString());
        task.setRequestContent(queryTask);
        task.setRequestContentType(PetasosFulfillmentTask.class);
        task.setRequiredCapabilityName("A19QueryFulfillment");
        task.setRequestInstant(Instant.now());
        //
        // Do Query
        //
        CapabilityUtilisationResponse a19QueryTaskOutcome = capabilityUtilisationBroker.executeTask(A19_CAPABILITY_PROVIDER, task);
        if (a19QueryTaskOutcome == null) {
            throw new IllegalStateException("Null result for A19 Query Task Outcome for task " + task);
        }
        //
        // Extract the response
        //
        PetasosFulfillmentTask result = a19QueryTaskOutcome.getResponseFulfillmentTask();
        if (result == null) {
            throw new IllegalStateException("Null result string in response for A19Query Task Outcome: response->"
                    + a19QueryTaskOutcome + ", task->" + task);
        }
        return(result);
    }


    //
    // Getters (and Setters)
    //


    public HapiContext getHAPIContext() {
        return hapiContext;
    }
    
    protected Logger getLogger(){
        return(LOG);
    }
}
