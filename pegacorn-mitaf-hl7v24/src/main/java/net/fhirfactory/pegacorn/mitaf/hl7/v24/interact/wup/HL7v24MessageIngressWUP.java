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

import net.fhirfactory.pegacorn.common.model.componentid.TopologyNodeFDN;
import net.fhirfactory.pegacorn.deployment.topology.model.endpoints.base.IPCTopologyEndpoint;
import net.fhirfactory.pegacorn.mitaf.hl7.v24.interact.beans.HL7v24MessageEncapsulator;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.wup.BaseHL7v2MessageIngresWUP;
import net.fhirfactory.pegacorn.petasos.wup.helper.IngresActivityBeginRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public abstract class HL7v24MessageIngressWUP extends BaseHL7v2MessageIngresWUP {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v24MessageIngressWUP.class);

    private String WUP_VERSION="1.0.0";

    @Override
    protected String specifyWUPInstanceName() {
        return (this.getClass().getSimpleName());
    }

    @Override
    protected String specifyWUPInstanceVersion() {
        return (WUP_VERSION);
    }

    @Override
    protected Logger specifyLogger() {
        return (LOG);
    }

    @Override
    public void configure() throws Exception {
        getLogger().info("{}:: ingresFeed() --> {}", getClass().getSimpleName(), ingresFeed());
        getLogger().info("{}:: egressFeed() --> {}", getClass().getSimpleName(), egressFeed());

        fromIncludingPetasosServices(ingresFeed())
                .routeId(getNameSet().getRouteCoreWUP())
                .bean(HL7v24MessageEncapsulator.class, "encapsulateMessage(*, Exchange)")
                .bean(IngresActivityBeginRegistration.class, "registerActivityStart(*,  Exchange)")
                .to(egressFeed());
    }

    protected IPCTopologyEndpoint getTopologyEndpoint(String topologyEndpointName){
        getLogger().debug(".getTopologyEndpoint(): Entry, topologyEndpointName->{}", topologyEndpointName);
        ArrayList<TopologyNodeFDN> endpointFDNs = getProcessingPlant().getProcessingPlantNode().getEndpoints();
        for(TopologyNodeFDN currentEndpointFDN: endpointFDNs){
            IPCTopologyEndpoint endpointTopologyNode = (IPCTopologyEndpoint)getTopologyIM().getNode(currentEndpointFDN);
            if(endpointTopologyNode.getName().contentEquals(topologyEndpointName)){
                getLogger().debug(".getTopologyEndpoint(): Exit, node found -->{}", endpointTopologyNode);
                return(endpointTopologyNode);
            }
        }
        getLogger().debug(".getTopologyEndpoint(): Exit, Could not find node!");
        return(null);
    }
}
