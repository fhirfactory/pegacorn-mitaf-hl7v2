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

import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.interfaces.topology.WorkshopInterface;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.*;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.mllp.MLLPServerEndpoint;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.mllp.adapters.MLLPServerAdapter;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.HL7V2XTopicFactory;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.model.HL7v2VersionEnum;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.mllp.MLLPAckSpecification;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.mllp.MLLPActivityAuditTrail;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.mllp.MLLPMessageIngresProcessor;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.HL7v2xTriggerEventIngresProcessor;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.HL7v2xTriggerEventValidationProcessor;
import net.fhirfactory.pegacorn.petasos.core.moa.wup.MessageBasedWUPEndpointContainer;
import net.fhirfactory.pegacorn.petasos.wup.helper.IngresActivityBeginRegistration;
import net.fhirfactory.pegacorn.workshops.InteractWorkshop;
import net.fhirfactory.pegacorn.wups.archetypes.petasosenabled.messageprocessingbased.InteractIngresMessagingGatewayWUP;
import org.apache.camel.ExchangePattern;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

public abstract class BaseHL7v2xMessageIngressWUP extends InteractIngresMessagingGatewayWUP {
	
	private static Long DEFAULT_ACCEPT_TIMEOUT = 45000L;
	private static Long DEFAULT_BIND_TIMEOUT=20000L;
	private static int DEFAULT_CONCURRENT_CONSUMERS=30;
	private boolean camelToDeliverStringPayload;
	private boolean camelToValidatePayload;
	private Long acceptTimeout;
	private Long bindTimeout;
	private int maxConcurrentConsumers;
	private boolean parametersInitialised;
	private String mllpServerConfiguration;

	@Inject
	private InteractWorkshop interactWorkshop;

	@Inject
	private HL7V2XTopicFactory hl7v2xTopicIDBuilder;

	@Override
	protected WorkshopInterface specifyWorkshop() {
		return (interactWorkshop);
	}

	abstract protected String specifySourceSystem();
	abstract protected String specifyIntendedTargetSystem();
	abstract protected String specifyMessageDiscriminatorType();
	abstract protected String specifyMessageDiscriminatorValue();

	//
	// Constructor(s)
	//

	public BaseHL7v2xMessageIngressWUP(){
		setCamelToDeliverStringPayload(true);
		setCamelToValidatePayload(false);
		setAcceptTimeout(DEFAULT_ACCEPT_TIMEOUT);
		setBindTimeout(DEFAULT_BIND_TIMEOUT);
		setMaxConcurrentConsumers(DEFAULT_CONCURRENT_CONSUMERS);
		setParametersInitialised(false);
		setMllpServerConfiguration(null);
	}

	//
	// Getters (and Setters)
	//


	public String getMllpServerConfiguration() {
		return mllpServerConfiguration;
	}

	public void setMllpServerConfiguration(String mllpServerConfiguration) {
		this.mllpServerConfiguration = mllpServerConfiguration;
	}

	public boolean isCamelToDeliverStringPayload() {
		return camelToDeliverStringPayload;
	}

	public void setCamelToDeliverStringPayload(boolean camelToDeliverStringPayload) {
		this.camelToDeliverStringPayload = camelToDeliverStringPayload;
	}

	public boolean isCamelToValidatePayload() {
		return camelToValidatePayload;
	}

	public void setCamelToValidatePayload(boolean camelToValidatePayload) {
		this.camelToValidatePayload = camelToValidatePayload;
	}

	public Long getAcceptTimeout() {
		return acceptTimeout;
	}

	public void setAcceptTimeout(Long acceptTimeout) {
		this.acceptTimeout = acceptTimeout;
	}

	public Long getBindTimeout() {
		return bindTimeout;
	}

	public void setBindTimeout(Long bindTimeout) {
		this.bindTimeout = bindTimeout;
	}

	public int getMaxConcurrentConsumers() {
		return maxConcurrentConsumers;
	}

	public void setMaxConcurrentConsumers(int maxConcurrentConsumers) {
		this.maxConcurrentConsumers = maxConcurrentConsumers;
	}

	public boolean isParametersInitialised() {
		return parametersInitialised;
	}

	public void setParametersInitialised(boolean parametersInitialised) {
		this.parametersInitialised = parametersInitialised;
	}

	//
	// Superclass Method Overrides
	//

	@Override
	protected String specifyEndpointParticipantName() {
		MessageBasedWUPEndpointContainer ingresEndpoint = getIngresEndpoint();
		MLLPServerEndpoint mllpServerEndpoint = (MLLPServerEndpoint)ingresEndpoint.getEndpointTopologyNode();
		String participantName = mllpServerEndpoint.getParticipantName();
		return (participantName);
	}

	//
	// Useful Methods for Subclasses
	//

	protected DataParcelManifest createPublishedManifestForInteractIngresHL7v2Messages(String eventType, String eventTrigger, HL7v2VersionEnum version) {
		DataParcelTypeDescriptor descriptor = hl7v2xTopicIDBuilder.newDataParcelDescriptor(eventType, eventTrigger, version.getVersionText());
		DataParcelManifest manifest = new DataParcelManifest();
		manifest.setContentDescriptor(descriptor);
		manifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INFORMATION_FLOW_INBOUND_DATA_PARCEL);
		manifest.setDataParcelType(DataParcelTypeEnum.GENERAL_DATA_PARCEL_TYPE);
		manifest.setEnforcementPointApprovalStatus(PolicyEnforcementPointApprovalStatusEnum.POLICY_ENFORCEMENT_POINT_APPROVAL_NEGATIVE);
		manifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_FALSE);
		manifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATED_TRUE);
		manifest.setIntendedTargetSystem("*");
		manifest.setSourceSystem("*");
		manifest.setInterSubsystemDistributable(false);
		return manifest;
	}

    private String WUP_VERSION="1.0.0";
    private String CAMEL_COMPONENT_TYPE="mllp";

    @Inject
    private MLLPActivityAuditTrail mllpAuditTrail;

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
                .bean(MLLPMessageIngresProcessor.class, "captureMLLPMessage(*, Exchange," + specifySourceSystem() +","+specifyIntendedTargetSystem()+","+specifyMessageDiscriminatorType()+","+specifyMessageDiscriminatorValue()+")")
				.bean(HL7v2xTriggerEventValidationProcessor.class, "ensureMinimalCompliance(*, Exchange)")
				.bean(HL7v2xTriggerEventIngresProcessor.class, "encapsulateTriggerEvent(*, Exchange)")
				.bean(IngresActivityBeginRegistration.class, "registerActivityStart(*,  Exchange)")
				.bean(MLLPAckSpecification.class, "setMLLPResponseCode(*, Exchange)")
                .bean(mllpAuditTrail, "logMLLPActivity(*, Exchange, MLLPIngres)")
                .to(ExchangePattern.InOnly, egressFeed());
    }

    @Override
    protected MessageBasedWUPEndpointContainer specifyIngresEndpoint() {
        getLogger().debug(".specifyIngresEndpoint(): Entry, specifyIngresTopologyEndpointName()->{}", specifyIngresTopologyEndpointName());
        MessageBasedWUPEndpointContainer endpoint = new MessageBasedWUPEndpointContainer();
        MLLPServerEndpoint serverTopologyEndpoint = (MLLPServerEndpoint) getTopologyEndpoint(specifyIngresTopologyEndpointName());
		buildMLLPConfigurationString();
        getLogger().trace(".specifyIngresEndpoint(): Retrieved serverTopologyEndpoint->{}", serverTopologyEndpoint);
        int portValue = serverTopologyEndpoint.getMLLPServerAdapter().getServicePortValue();
        String interfaceDNSName = serverTopologyEndpoint.getMLLPServerAdapter().getHostName();
        endpoint.setEndpointSpecification(CAMEL_COMPONENT_TYPE+":"+interfaceDNSName+":"+Integer.toString(portValue)+ getMllpServerConfiguration());
        endpoint.setEndpointTopologyNode(serverTopologyEndpoint);
        endpoint.setFrameworkEnabled(false);
        getLogger().debug(".specifyIngresEndpoint(): Exit, endpoint->{}", endpoint);
        return (endpoint);
    }

	protected void buildMLLPConfigurationString(){
		if(!isParametersInitialised()) {
			MLLPServerEndpoint serverTopologyEndpoint = (MLLPServerEndpoint) getTopologyEndpoint(specifyIngresTopologyEndpointName());
			MLLPServerAdapter mllpAdapter = serverTopologyEndpoint.getMLLPServerAdapter();
			if (mllpAdapter != null) {
				String mllpValidatePayload = mllpAdapter.getAdditionalParameters().get(PetasosPropertyConstants.CAMEL_MLLP_VALIDATE_PAYLOAD_PARAMETER_NAME);
				String mllpDeliveryStringPayload = mllpAdapter.getAdditionalParameters().get(PetasosPropertyConstants.CAMEL_MLLP_STRING_PAYLOAD_PARAMETER_NAME);
				String mllpBindTimeout = mllpAdapter.getAdditionalParameters().get(PetasosPropertyConstants.CAMEL_MLLP_BIND_TIMEOUT_PARAMETER_NAME);
				String mllpAcceptTimeout = mllpAdapter.getAdditionalParameters().get(PetasosPropertyConstants.CAMEL_MLLP_ACCEPT_TIMEOUT_PARAMETER_NAME);
				String mllpMaxConcurrentConsumers = mllpAdapter.getAdditionalParameters().get(PetasosPropertyConstants.CAMEL_MLLP_MAXIMUM_CONSUMERS_PARAMETER_NAME);
				if (StringUtils.isNotEmpty(mllpValidatePayload)) {
					if (mllpValidatePayload.equalsIgnoreCase("True")) {
						setCamelToValidatePayload(true);
					}
				}
				if(StringUtils.isNotEmpty(mllpBindTimeout)){
					try{
						Long bindTimeout = Long.valueOf(mllpBindTimeout);
						setBindTimeout(bindTimeout);
					} catch(Exception ex){
						getLogger().debug(".buildMLLPConfigurationString(): Cannot parse mllpBindTimeout, leaving at default value");
					}
				}
				if(StringUtils.isNotEmpty(mllpAcceptTimeout)){
					try{
						long acceptTimeout = Long.valueOf(mllpAcceptTimeout);
						setAcceptTimeout(acceptTimeout);
					} catch(Exception ex){
						getLogger().debug(".buildMLLPConfigurationString(): Cannot parse mllpAcceptTimeout, leaving at default value");
					}
				}
				if(StringUtils.isNotEmpty(mllpMaxConcurrentConsumers)){
					try{
						int maxConcurrentSessions = Integer.valueOf(mllpMaxConcurrentConsumers);
						setMaxConcurrentConsumers(maxConcurrentSessions);
					} catch(Exception ex){
						getLogger().debug(".buildMLLPConfigurationString(): Cannot parse maxConcurrentConsumers, leaving at default value");
					}
				}
				if(StringUtils.isNotEmpty(mllpDeliveryStringPayload)){
					if(mllpDeliveryStringPayload.equalsIgnoreCase("True")){
						setCamelToDeliverStringPayload(true);
					} else {
						setCamelToDeliverStringPayload(false);
					}
				}
			}
			StringBuilder mllpConfig = new StringBuilder();
			mllpConfig.append("?");
			mllpConfig.append("maxConcurrentConsumers="+Integer.toString(getMaxConcurrentConsumers()));
			mllpConfig.append("&");
			mllpConfig.append("acceptTimeout="+getAcceptTimeout().toString());
			mllpConfig.append("&");
			mllpConfig.append("bindTimeout="+getBindTimeout().toString());
			mllpConfig.append("&");
			if(isCamelToDeliverStringPayload()){
				mllpConfig.append("stringPayload=true");
			} else {
				mllpConfig.append("stringPayload=false");
			}
			mllpConfig.append("&");
			if(isCamelToValidatePayload()){
				mllpConfig.append("validatePayload=true");
			} else {
				mllpConfig.append("validatePayload=false");
			}
			String mllpConfigurationString = mllpConfig.toString();
			setMllpServerConfiguration(mllpConfigurationString);
			setParametersInitialised(true);
		}
	}

}
