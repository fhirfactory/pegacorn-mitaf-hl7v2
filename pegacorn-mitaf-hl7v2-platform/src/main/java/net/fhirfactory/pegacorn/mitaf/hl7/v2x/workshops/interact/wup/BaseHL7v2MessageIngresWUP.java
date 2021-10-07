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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.wup;

import net.fhirfactory.pegacorn.components.interfaces.topology.WorkshopInterface;
import net.fhirfactory.pegacorn.deployment.topology.model.nodes.WorkUnitProcessorTopologyNode;
import net.fhirfactory.pegacorn.petasos.model.configuration.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.workshops.InteractWorkshop;
import net.fhirfactory.pegacorn.wups.archetypes.petasosenabled.messageprocessingbased.InteractIngresMessagingGatewayWUP;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.RouteDefinition;

import javax.inject.Inject;
import net.fhirfactory.pegacorn.deployment.topology.model.endpoints.common.PetasosEndpointTopologyTypeEnum;
import net.fhirfactory.pegacorn.petasos.itops.collectors.metrics.WorkUnitProcessorMetricsCollectionAgent;
import org.thymeleaf.util.StringUtils;

/**
 * Base class for all MITaF MLLP Ingres WUPs.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL7v2MessageIngresWUP extends InteractIngresMessagingGatewayWUP {

    private String MSG_HEADER_CAMEL_MLLP_REMOTE_ADDRESS = "CamelMllpRemoteAddress";
    private String MSG_HEADER_CAMEL_MLLP_LOCAL_ADDRESS = "CamelMllpLocalAddress";

    @Inject
    private InteractWorkshop interactWorkshop;
    
    @Inject
    private WorkUnitProcessorMetricsCollectionAgent metricsCollectionAgent;

    @Override
    protected WorkshopInterface specifyWorkshop() {
            return (interactWorkshop);
    }

    abstract protected String specifySourceSystem();
    abstract protected String specifyIntendedTargetSystem();
    abstract protected String specifyMessageDiscriminatorType();
    abstract protected String specifyMessageDiscriminatorValue();

    protected RouteDefinition fromIngresMLLP(String uri) {
        InteractIngresMetricsCapture ingresMetrics = new InteractIngresMetricsCapture();
        RouteDefinition route = fromInteractIngresService(uri);
        route
                .process(ingresMetrics)
        ;
        return route;
    }

    protected class InteractIngresMetricsCapture implements Processor{

        @Override
        public void process(Exchange camelExchange){
            getLogger().debug("BaseHL7v2MessageIngresWUP.captureMetrics(): Entry");
            PetasosEndpointTopologyTypeEnum endpointType = getIngresEndpoint().getEndpointTopologyNode().getEndpointType();
            boolean captureMetrics = false;
            switch(endpointType){
                case MLLP_CLIENT:
                case MLLP_SERVER:
                    captureMetrics = true;
                    break;
                default:
                    captureMetrics = false;
            }
            if(captureMetrics){ 
                WorkUnitProcessorTopologyNode wupTN = camelExchange.getProperty(PetasosPropertyConstants.WUP_TOPOLOGY_NODE_EXCHANGE_PROPERTY_NAME, WorkUnitProcessorTopologyNode.class);
                captureMLLPRemoteAddress(camelExchange, wupTN);

            }
            getLogger().debug("BaseHL7v2MessageIngresWUP.captureMetrics(): Exit");
        }
    }

    private void captureMLLPRemoteAddress(Exchange camelExchange, WorkUnitProcessorTopologyNode wupTopologyNode){
        String mllpRemoteAddress = camelExchange.getProperty(MSG_HEADER_CAMEL_MLLP_REMOTE_ADDRESS,String.class);
        String componentID = wupTopologyNode.getComponentID();
        if(StringUtils.isEmpty(mllpRemoteAddress)){
            getLogger().info(".captureMetrics(): componentID->{}, mllpRemoteAddress->Not Reported", componentID);
//            metricsCollectionAgent.updateRemoteEndpointDetail(wupTopologyNode.getComponentID(), "Not Reported");
        } else {
            getLogger().info(".captureMetrics(): componentID->{}, mllpRemoteAddress->{}", componentID, mllpRemoteAddress);
//            metricsCollectionAgent.updateRemoteEndpointDetail(wupTopologyNode.getComponentID(), mllpRemoteAddress);
        }
    }

    @Override
    protected String specifyWUPInstanceName() {
        String wupName = this.getClass().getSimpleName();
        return (wupName);
    }
}
