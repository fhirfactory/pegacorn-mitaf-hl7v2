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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.wup;

import net.fhirfactory.pegacorn.core.interfaces.capabilities.CapabilityFulfillmentInterface;
import net.fhirfactory.pegacorn.core.model.capabilities.base.CapabilityUtilisationRequest;
import net.fhirfactory.pegacorn.core.model.capabilities.base.CapabilityUtilisationResponse;
import net.fhirfactory.pegacorn.core.model.petasos.task.PetasosFulfillmentTask;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.interact.StandardInteractClientTopologyEndpointPort;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.mllp.adapters.MLLPClientAdapter;
import net.fhirfactory.pegacorn.core.model.topology.nodes.external.ConnectedExternalSystemTopologyNode;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.capabilities.HL7v2xQueryCapabilityFulfillerRequestHandler;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.HL7v2xQueryClientQueryProcessor;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.HL7v2xQueryClientResponseProcessor;
import net.fhirfactory.pegacorn.petasos.core.moa.wup.MessageBasedWUPEndpointContainer;
import net.fhirfactory.pegacorn.petasos.core.tasks.accessors.PetasosFulfillmentTaskSharedInstance;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.model.RouteDefinition;

import javax.inject.Inject;
import java.time.Instant;

public abstract class BaseHL7v2QueryMessageClientWUP extends BaseHL7v2MessageEgressWUP implements CapabilityFulfillmentInterface {

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
    private HL7v2xQueryCapabilityFulfillerRequestHandler queryCapabilityHandler;

    @Override
    public void configure() throws Exception {
        getLogger().info("{}:: ingresFeed() --> {}", getClass().getSimpleName(), ingresFeed());
        getLogger().info("{}:: egressFeed() --> {}", getClass().getSimpleName(), egressFeed());

        fromIncludingPetasosServices(ingresFeed())
                .routeId(getNameSet().getRouteCoreWUP())
                .bean(HL7v2xQueryClientQueryProcessor.class, "phase1ReportingProcessingStart(*, Exchange)")
                .bean(HL7v2xQueryClientQueryProcessor.class, "phase2EmbedFulfillmentTaskInExchange(*, Exchange)")
                .bean(HL7v2xQueryClientQueryProcessor.class, "phase3ExtractUoW(*, Exchange)")
                .bean(HL7v2xQueryClientQueryProcessor.class, "phase4ExtractMessageFromUoW(*, Exchange)")
                .to(egressFeed())
                .bean(HL7v2xQueryClientResponseProcessor.class, "phase1ExtractACKContentFromExchange(*, Exchange)")
                .bean(HL7v2xQueryClientResponseProcessor.class, "phase2EncapsulateMessageIntoFulfillmentTask(*, Exchange)")
                .to(ExchangePattern.InOnly, continuityPathwayURL());

        from(continuityPathwayURL())
                .bean(HL7v2xQueryClientResponseProcessor.class, "phase3ResolveUoWFromFulfillmentTask(*, Exchange)")
                .to(getNameSet().getEndPointWUPEgress());

    }

    protected String continuityPathwayURL(){
        String endpointURL = "direct:"+getClass().getSimpleName()+"TowardsTheEgressPathway";
        return(endpointURL);
    }

    @Override
    protected MessageBasedWUPEndpointContainer specifyEgressEndpoint() {
        MessageBasedWUPEndpointContainer endpoint = new MessageBasedWUPEndpointContainer();
        StandardInteractClientTopologyEndpointPort clientTopologyEndpoint = (StandardInteractClientTopologyEndpointPort) getTopologyEndpoint(specifyEgressTopologyEndpointName());
        ConnectedExternalSystemTopologyNode targetSystem = clientTopologyEndpoint.getTargetSystem();
        MLLPClientAdapter externalSystemIPCEndpoint = (MLLPClientAdapter)targetSystem.getTargetPorts().get(0);
        int portValue = externalSystemIPCEndpoint.getPortNumber();
        String targetInterfaceDNSName = externalSystemIPCEndpoint.getHostName();
        endpoint.setEndpointSpecification(CAMEL_COMPONENT_TYPE+":"+targetInterfaceDNSName+":"+Integer.toString(portValue)+"?requireEndOfData=false");
        endpoint.setEndpointTopologyNode(clientTopologyEndpoint);
        endpoint.setFrameworkEnabled(false);
        return endpoint;
    }


    @Override
    public CapabilityUtilisationResponse executeTask(CapabilityUtilisationRequest request) {
        getLogger().info(".executeTask(): Entry, request->{}", request);
        PetasosFulfillmentTask queryTask = request.getRequestFulfillmentTask();
        PetasosFulfillmentTaskSharedInstance localFulfillmentTask = queryCapabilityHandler.createLocalTasks(queryTask, getMeAsATopologyComponent(), getTaskReportAgent());
        PetasosFulfillmentTask response = hl7MessageInjector.requestBody(ingresFeed(), localFulfillmentTask, PetasosFulfillmentTask.class);
        getLogger().info(".executeTask(): response->{}", response);
        CapabilityUtilisationResponse outcome = new CapabilityUtilisationResponse();
        outcome.setInstantCompleted(Instant.now());
        outcome.setSuccessful(true);
        outcome.setAssociatedRequestID(request.getRequestID());
        outcome.setResponseFulfillmentTask(response);
        getLogger().info(".executeTask(): Exit, outcome->{}", outcome);
        return(outcome);
    }

    @Override
    protected void registerCapabilities(){
        getProcessingPlant().registerCapabilityFulfillmentService("A19QueryFulfillment", this);
    }

    private String getA19DirectCamelEndpointName(){
        String name = "direct:" + getClass().getSimpleName() + "-A19QueryPoint";
        return(name);
    }

    //
    // Route Helper Functions
    //

    protected RouteDefinition fromIncludingPetasosServices(String uri) {
        NodeDetailInjector nodeDetailInjector = new NodeDetailInjector();
        AuditAgentInjector auditAgentInjector = new AuditAgentInjector();
        TaskReportAgentInjector taskReportAgentInjector = new TaskReportAgentInjector();
        RouteDefinition route = fromWithStandardExceptionHandling(uri);
        route
                .process(nodeDetailInjector)
                .process(auditAgentInjector)
                .process(taskReportAgentInjector)
        ;
        return route;
    }
}
