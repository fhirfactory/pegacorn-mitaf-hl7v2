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

import net.fhirfactory.pegacorn.deployment.topology.model.endpoints.base.ExternalSystemIPCEndpoint;
import net.fhirfactory.pegacorn.deployment.topology.model.endpoints.interact.StandardInteractClientTopologyEndpointPort;
import net.fhirfactory.pegacorn.deployment.topology.model.nodes.external.ConnectedExternalSystemTopologyNode;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.HL7V2XTopicFactory;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.HL7v2MessageExtractor;
import net.fhirfactory.pegacorn.petasos.core.moa.wup.MessageBasedWUPEndpoint;
import net.fhirfactory.pegacorn.petasos.wup.helper.IngresActivityBeginRegistration;
import net.fhirfactory.pegacorn.workshops.InteractWorkshop;
import net.fhirfactory.pegacorn.wups.archetypes.petasosenabled.messageprocessingbased.InteractEgressMessagingGatewayWUP;

import javax.inject.Inject;

/**
 * Base class for all Mitaf Egress WUPs.
 * 
 * @author Brendan Douglas
 * @author Mark Hunter
 *
 */
public abstract class BaseHL7v2MessageEgressWUP extends InteractEgressMessagingGatewayWUP {

	private String CAMEL_COMPONENT_TYPE="mllp";

	@Inject
	private InteractWorkshop interactWorkshop;

	@Inject
	private HL7V2XTopicFactory hl7v2xTopicIDBuilder;

	@Inject
	private HL7v2MessageExtractor messageExtractor;

	@Override
	public void configure() throws Exception {
		getLogger().info("{}:: ingresFeed() --> {}", getClass().getSimpleName(), ingresFeed());
		getLogger().info("{}:: egressFeed() --> {}", getClass().getSimpleName(), egressFeed());

		fromIncludingPetasosServices(ingresFeed())
				.routeId(getNameSet().getRouteCoreWUP())
				.bean(messageExtractor, "convertToMessage(*, Exchange)")
				.bean(IngresActivityBeginRegistration.class, "registerActivityStart(*,  Exchange)")
				.to(egressFeed());
	}

	@Override
	protected MessageBasedWUPEndpoint specifyEgressEndpoint() {
		MessageBasedWUPEndpoint endpoint = new MessageBasedWUPEndpoint();
		StandardInteractClientTopologyEndpointPort clientTopologyEndpoint = (StandardInteractClientTopologyEndpointPort) getTopologyEndpoint(specifyEgressTopologyEndpointName());
		ConnectedExternalSystemTopologyNode targetSystem = clientTopologyEndpoint.getTargetSystem();
		ExternalSystemIPCEndpoint externalSystemIPCEndpoint = targetSystem.getTargetPorts().get(0);
		int portValue = externalSystemIPCEndpoint.getTargetPortValue();
		String targetInterfaceDNSName = externalSystemIPCEndpoint.getTargetPortDNSName();
		endpoint.setEndpointSpecification(CAMEL_COMPONENT_TYPE+":"+targetInterfaceDNSName+":"+Integer.toString(portValue));
		endpoint.setEndpointTopologyNode(clientTopologyEndpoint);
		endpoint.setFrameworkEnabled(false);
		return endpoint;
	}

}
