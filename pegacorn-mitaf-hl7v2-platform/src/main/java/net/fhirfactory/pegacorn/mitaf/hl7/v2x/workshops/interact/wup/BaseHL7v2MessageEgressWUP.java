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

import java.net.ConnectException;

import javax.inject.Inject;

import org.apache.camel.LoggingLevel;
import org.apache.camel.component.mllp.MllpAcknowledgementReceiveException;
import org.apache.camel.model.OnExceptionDefinition;
import org.apache.camel.model.RouteDefinition;

import net.fhirfactory.pegacorn.components.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.components.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.components.dataparcel.valuesets.DataParcelDirectionEnum;
import net.fhirfactory.pegacorn.components.dataparcel.valuesets.DataParcelNormalisationStatusEnum;
import net.fhirfactory.pegacorn.components.dataparcel.valuesets.DataParcelTypeEnum;
import net.fhirfactory.pegacorn.components.dataparcel.valuesets.DataParcelValidationStatusEnum;
import net.fhirfactory.pegacorn.components.dataparcel.valuesets.PolicyEnforcementPointApprovalStatusEnum;
import net.fhirfactory.pegacorn.components.interfaces.topology.WorkshopInterface;
import net.fhirfactory.pegacorn.deployment.topology.model.endpoints.base.ExternalSystemIPCEndpoint;
import net.fhirfactory.pegacorn.deployment.topology.model.endpoints.interact.StandardInteractClientTopologyEndpointPort;
import net.fhirfactory.pegacorn.deployment.topology.model.nodes.external.ConnectedExternalSystemTopologyNode;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.HL7V2XTopicFactory;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.model.HL7v2VersionEnum;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.HL7v2MessageExtractor;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.HL7v2xMessageFilter;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.MLLPActivityAnswerCollector;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.MLLPActivityAuditTrail;
import net.fhirfactory.pegacorn.petasos.core.moa.wup.MessageBasedWUPEndpoint;
import net.fhirfactory.pegacorn.petasos.wup.helper.EgressActivityFinalisationRegistration;
import net.fhirfactory.pegacorn.workshops.InteractWorkshop;
import net.fhirfactory.pegacorn.wups.archetypes.petasosenabled.messageprocessingbased.InteractEgressMessagingGatewayWUP;

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

	@Inject
	private MLLPActivityAnswerCollector answerCollector;

	@Inject
	private MLLPActivityAuditTrail mllpAuditTrail;
	
	@Inject
	private HL7v2xMessageFilter hl7v2xMessageFilter;

	@Override
	protected WorkshopInterface specifyWorkshop() {
		return (interactWorkshop);
	}

	@Override
	public void configure() throws Exception {
		getLogger().info("{}:: ingresFeed() --> {}", getClass().getSimpleName(), ingresFeed());
		getLogger().warn("{}:: egressFeed() --> {}", getClass().getSimpleName(), egressFeed());

		getMLLPAckException();
		getMLLPConnectionException();

		fromIncludingEgressEndpointDetails(ingresFeed())
				.routeId(getNameSet().getRouteCoreWUP())
				.bean(mllpAuditTrail, "logMLLPActivity(*, Exchange)")
				.choice()
					.when().method(hl7v2xMessageFilter, "filter(*, Exchange)") // Execute the filter logic.  True = send the message, false = do not send.
						.bean(messageExtractor, "convertToMessage(*, Exchange)")
						.to(egressFeed())
						.bean(answerCollector, "extractUoWAndAnswer")
						.bean(mllpAuditTrail, "logMLLPActivity(*, Exchange, MLLPEgress)")
					.end()
				.end()
				.bean(EgressActivityFinalisationRegistration.class,"registerActivityFinishAndFinalisation(*,  Exchange)");
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

	protected DataParcelManifest createSubscriptionManifestForInteractEgressHL7v2Messages(String eventType, String eventTrigger, HL7v2VersionEnum version) {
		DataParcelTypeDescriptor descriptor = hl7v2xTopicIDBuilder.newDataParcelDescriptor(eventType, eventTrigger, version.getVersionText());
		DataParcelManifest manifest = new DataParcelManifest();
		manifest.setContentDescriptor(descriptor);
		manifest.setDataParcelFlowDirection(DataParcelDirectionEnum.OUTBOUND_DATA_PARCEL);
		manifest.setDataParcelType(DataParcelTypeEnum.GENERAL_DATA_PARCEL_TYPE);
		manifest.setEnforcementPointApprovalStatus(PolicyEnforcementPointApprovalStatusEnum.POLICY_ENFORCEMENT_POINT_APPROVAL_POSITIVE);
		manifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_TRUE);
		manifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATION_ANY);
		manifest.setIntendedTargetSystem("*");
		manifest.setSourceSystem("*");
		manifest.setInterSubsystemDistributable(false);
		return manifest;
	}

	/**
	 * @param uri
	 * @return the RouteBuilder.from(uri) with all exceptions logged but not handled
	 */
	protected RouteDefinition fromIncludingEgressEndpointDetails(String uri) {
		PortDetailInjector portDetailInjector = new PortDetailInjector();
		RouteDefinition route = fromWithStandardExceptionHandling(uri);
		route
				.process(portDetailInjector)
		;
		return route;
	}

	protected OnExceptionDefinition getMLLPAckException() {
		OnExceptionDefinition exceptionDef = onException(MllpAcknowledgementReceiveException.class)
				.handled(true)
				.log(LoggingLevel.INFO, "MLLP Acknowledgement Exception...")
				.bean(mllpAuditTrail, "logExceptionMLLPActivity(*, Exchange)");
		return(exceptionDef);
	}

	protected OnExceptionDefinition getMLLPConnectionException() {
		OnExceptionDefinition exceptionDef = onException(ConnectException.class)
				.handled(true)
				.log(LoggingLevel.INFO, "MLLP Connection Exception...")
				.bean(mllpAuditTrail, "logConnectionException(*, Exchange)");
		return(exceptionDef);
	}
}
