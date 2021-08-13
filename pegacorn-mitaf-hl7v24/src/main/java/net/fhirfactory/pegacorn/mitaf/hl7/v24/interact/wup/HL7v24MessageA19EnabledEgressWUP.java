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
package net.fhirfactory.pegacorn.mitaf.hl7.v24.interact.wup;

import net.fhirfactory.pegacorn.components.tasks.hl7v2tasks.A19QueryCapabilityFulfillmentInterface;
import net.fhirfactory.pegacorn.components.tasks.hl7v2tasks.A19QueryTask;
import net.fhirfactory.pegacorn.components.tasks.hl7v2tasks.A19QueryTaskOutcome;
import net.fhirfactory.pegacorn.deployment.topology.model.endpoints.base.ExternalSystemIPCEndpoint;
import net.fhirfactory.pegacorn.deployment.topology.model.endpoints.interact.StandardInteractClientTopologyEndpointPort;
import net.fhirfactory.pegacorn.deployment.topology.model.nodes.external.ConnectedExternalSystemTopologyNode;
import net.fhirfactory.pegacorn.mitaf.hl7.v24.interact.beans.HL7v24A19QueryMessageEncapsulator;
import net.fhirfactory.pegacorn.mitaf.hl7.v24.interact.beans.HL7v24A19ResponseACKExtractor;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.wup.BaseHL7v2MessageEgressWUP;
import net.fhirfactory.pegacorn.petasos.core.moa.wup.MessageBasedWUPEndpoint;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;

import javax.inject.Inject;
import java.time.Instant;

public abstract class HL7v24MessageA19EnabledEgressWUP extends BaseHL7v2MessageEgressWUP implements A19QueryCapabilityFulfillmentInterface  {

    private String WUP_VERSION="1.0.0";
    private String CAMEL_COMPONENT_TYPE="mllp";

    @Produce
    private ProducerTemplate hl7MessageInjector;

    @Override
    protected String specifyWUPInstanceName() {
        return (this.getClass().getSimpleName());
    }

    @Override
    protected String specifyWUPInstanceVersion() {
        return (WUP_VERSION);
    }

    @Inject
    private HL7v24A19QueryMessageEncapsulator a19QueryMessageEncapsulator;

    @Inject
    private HL7v24A19ResponseACKExtractor a19ResponseACKExtractor;

    @Override
    public void configure() throws Exception {
        getLogger().info("{}:: ingresFeed() --> {}", getClass().getSimpleName(), ingresFeed());
        getLogger().info("{}:: egressFeed() --> {}", getClass().getSimpleName(), egressFeed());

        fromIncludingPetasosServices(ingresFeed())
                .to(getA19DirectCamelEndpointName());

        from(getA19DirectCamelEndpointName())
                .routeId(getNameSet().getRouteCoreWUP())
                .bean(a19QueryMessageEncapsulator, "buildA19Request(*, Exchange)")
                .to(egressFeed())
                .bean(a19ResponseACKExtractor, "extractACKContent");
    }

    @Override
    protected MessageBasedWUPEndpoint specifyEgressEndpoint() {
        MessageBasedWUPEndpoint endpoint = new MessageBasedWUPEndpoint();
        StandardInteractClientTopologyEndpointPort clientTopologyEndpoint = (StandardInteractClientTopologyEndpointPort) getTopologyEndpoint(specifyEgressTopologyEndpointName());
        ConnectedExternalSystemTopologyNode targetSystem = clientTopologyEndpoint.getTargetSystem();
        ExternalSystemIPCEndpoint externalSystemIPCEndpoint = targetSystem.getTargetPorts().get(0);
        int portValue = externalSystemIPCEndpoint.getTargetPortValue();
        String targetInterfaceDNSName = externalSystemIPCEndpoint.getTargetPortDNSName();
        endpoint.setEndpointSpecification(CAMEL_COMPONENT_TYPE+":"+targetInterfaceDNSName+":"+Integer.toString(portValue)+"?requireEndOfData=false");
        endpoint.setEndpointTopologyNode(clientTopologyEndpoint);
        endpoint.setFrameworkEnabled(false);
        return endpoint;
    }

    @Override
    public A19QueryTaskOutcome fulfillA19QueryCapability(A19QueryTask a19QueryTask) {
        getLogger().info(".fulfillA19QueryCapability(): Entry, a19QueryTask->{}", a19QueryTask);
        String queryString = a19QueryTask.getA19QueryString();
        String response = hl7MessageInjector.requestBody(getA19DirectCamelEndpointName(), queryString, String.class);
        getLogger().info(".fulfillA19QueryCapability(): response->{}", response);
        A19QueryTaskOutcome outcome = new A19QueryTaskOutcome();
        outcome.setDateCompleted(Instant.now());
        outcome.setSuccessful(true);
        outcome.setAssociatedRequestID(a19QueryTask.getRequestID());
        outcome.setA19QueryResponse(response);
        getLogger().info(".fulfillA19QueryCapability(): Exit, outcome->{}", outcome);
        return(outcome);
    }

    private String getA19DirectCamelEndpointName(){
        String name = "direct:" + getClass().getSimpleName() + "-A19QueryPoint";
        return(name);
    }
}
