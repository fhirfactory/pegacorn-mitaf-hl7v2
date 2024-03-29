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

import net.fhirfactory.pegacorn.core.model.topology.endpoints.mllp.MLLPServerEndpoint;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.HL7v2xMessageEncapsulator;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.mllp.MLLPActivityAuditTrail;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.mllp.MLLPAsynchronousMessageACKCollector;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.wup.BaseHL7v2MessageAsynchronousACKIngresWUP;
import net.fhirfactory.pegacorn.petasos.core.moa.wup.MessageBasedWUPEndpointContainer;
import net.fhirfactory.pegacorn.petasos.wup.helper.IngresActivityBeginRegistration;
import org.apache.camel.ExchangePattern;

import javax.inject.Inject;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.mllp.MLLPAsynchronousMessageORRCollector;

public abstract class HL7v24MessageAsynchronousACKIngressWUP extends BaseHL7v2MessageAsynchronousACKIngresWUP {

    private String WUP_VERSION="1.0.0";
    private String CAMEL_COMPONENT_TYPE="mllp";

    @Inject
    private MLLPActivityAuditTrail mllpAuditTrail;
    
    @Inject
    private MLLPAsynchronousMessageACKCollector mllpAsynchronousMessageACKCollector;
    
    @Inject
    private MLLPAsynchronousMessageORRCollector MLLPAsynchronousMessageORRCollector;

    @Override
    protected String specifyWUPInstanceName() {
        return (this.getClass().getSimpleName());
    }

    @Override
    protected String specifyWUPInstanceVersion() {
        return (WUP_VERSION);
    }

    @Override
    public void configure() throws Exception {
        getLogger().warn("{}:: ingresFeed() --> {}", getClass().getSimpleName(), ingresFeed());
        getLogger().info("{}:: egressFeed() --> {}", getClass().getSimpleName(), egressFeed());

        fromInteractIngresService(ingresFeed())
                .routeId(getNameSet().getRouteCoreWUP())
                .bean(HL7v2xMessageEncapsulator.class, "encapsulateMessage(*, Exchange," + specifySourceSystem() +","+specifyIntendedTargetSystem()+","+specifyMessageDiscriminatorType()+","+specifyMessageDiscriminatorValue()+")")
                .choice()
                    .when(header("CamelMllpEventType").isEqualTo("ACK"))
                        .bean(mllpAsynchronousMessageACKCollector, "extractAndSaveACKMessage(*, Exchange)")
                    .when(header("CamelMllpEventType").isEqualTo("ORR"))
                        .bean(MLLPAsynchronousMessageORRCollector, "extractAndSaveACKMessage(*, Exchange)")  
                        .bean(IngresActivityBeginRegistration.class, "registerActivityStart(*,  Exchange)")
                        .bean(mllpAuditTrail, "logMLLPActivity(*, Exchange, MLLPIngres)")
                        .to(ExchangePattern.InOnly, egressFeed())
                .otherwise()
                    .bean(IngresActivityBeginRegistration.class, "registerActivityStart(*,  Exchange)")
                    .bean(mllpAuditTrail, "logMLLPActivity(*, Exchange, MLLPIngres)")
                    .to(ExchangePattern.InOnly, egressFeed())
                .end();
    }

    @Override
    protected MessageBasedWUPEndpointContainer specifyIngresEndpoint() {
        getLogger().debug(".specifyIngresEndpoint(): Entry, specifyIngresTopologyEndpointName()->{}", specifyIngresTopologyEndpointName());
        MessageBasedWUPEndpointContainer endpoint = new MessageBasedWUPEndpointContainer();
        MLLPServerEndpoint serverTopologyEndpoint = (MLLPServerEndpoint) getTopologyEndpoint(specifyIngresTopologyEndpointName());
        getLogger().trace(".specifyIngresEndpoint(): Retrieved serverTopologyEndpoint->{}", serverTopologyEndpoint);
        int portValue = serverTopologyEndpoint.getMLLPServerAdapter().getPortNumber();
        String interfaceDNSName = serverTopologyEndpoint.getMLLPServerAdapter().getHostName();
        endpoint.setEndpointSpecification(CAMEL_COMPONENT_TYPE+":"+interfaceDNSName+":"+Integer.toString(portValue)+ getMllpConfigurationString());
        endpoint.setEndpointTopologyNode(serverTopologyEndpoint);
        endpoint.setFrameworkEnabled(false);
        getLogger().debug(".specifyIngresEndpoint(): Exit, endpoint->{}", endpoint);
        return (endpoint);
    }

    @Override
    protected String specifySourceSystem() {
        return null;
    }

    @Override
    protected String specifyIntendedTargetSystem() {
        return null;
    }

    @Override
    protected String specifyMessageDiscriminatorType() {
        return null;
    }

    @Override
    protected String specifyMessageDiscriminatorValue() {
        return null;
    }
}
