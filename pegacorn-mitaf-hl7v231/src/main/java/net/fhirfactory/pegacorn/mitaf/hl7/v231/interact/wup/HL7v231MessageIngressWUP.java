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
package net.fhirfactory.pegacorn.mitaf.hl7.v231.interact.wup;

import net.fhirfactory.pegacorn.deployment.topology.model.endpoints.base.IPCServerTopologyEndpoint;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.wup.BaseHL7v2MessageIngresWUP;
import net.fhirfactory.pegacorn.petasos.wup.moa.MessageBasedWUPEndpoint;
import net.fhirfactory.pegacorn.petasos.wup.moa.helper.IngresActivityBeginRegistration;
import net.fhirfactory.pegacorn.mitaf.hl7.v231.interact.beans.HL7v231MessageEncapsulator;

public abstract class HL7v231MessageIngressWUP extends BaseHL7v2MessageIngresWUP {

    private String WUP_VERSION="1.0.0";
    private String CAMEL_COMPONENT_TYPE="mllp";

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
        getLogger().info("{}:: ingresFeed() --> {}", getClass().getSimpleName(), ingresFeed());
        getLogger().info("{}:: egressFeed() --> {}", getClass().getSimpleName(), egressFeed());

        fromInteractIngresService(ingresFeed())
                .routeId(getNameSet().getRouteCoreWUP())
                .bean(HL7v231MessageEncapsulator.class, "encapsulateMessage(*, Exchange)")
                .bean(IngresActivityBeginRegistration.class, "registerActivityStart(*,  Exchange)")
                .to(egressFeed());
    }

    @Override
    protected MessageBasedWUPEndpoint specifyIngresEndpoint() {
        getLogger().debug(".specifyIngresEndpoint(): Entry, specifyIngresTopologyEndpointName()->{}", specifyIngresTopologyEndpointName());
        MessageBasedWUPEndpoint endpoint = new MessageBasedWUPEndpoint();
        IPCServerTopologyEndpoint serverTopologyEndpoint = (IPCServerTopologyEndpoint) getTopologyEndpoint(specifyIngresTopologyEndpointName());
        getLogger().trace(".specifyIngresEndpoint(): Retrieved serverTopologyEndpoint->{}", serverTopologyEndpoint);
        int portValue = serverTopologyEndpoint.getPortValue();
        String interfaceDNSName = serverTopologyEndpoint.getHostDNSName();
        endpoint.setEndpointSpecification(CAMEL_COMPONENT_TYPE+":"+interfaceDNSName+":"+Integer.toString(portValue));
        endpoint.setEndpointTopologyNode(serverTopologyEndpoint);
        endpoint.setFrameworkEnabled(false);
        getLogger().debug(".specifyIngresEndpoint(): Exit, endpoint->{}", endpoint);
        return (endpoint);
    }

}
