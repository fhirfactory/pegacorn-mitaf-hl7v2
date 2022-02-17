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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.wup;

import net.fhirfactory.pegacorn.core.constants.systemwide.PegacornReferenceProperties;
import net.fhirfactory.pegacorn.core.interfaces.topology.WorkshopInterface;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.*;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.FHIRElementTopicFactory;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.HL7V2XTopicFactory;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.HL7v2xMessageOutOfFHIRCommunication;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.HL7v2xTransformMessage;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.FreeMarkerConfiguration;
import net.fhirfactory.pegacorn.workshops.TransformWorkshop;
import net.fhirfactory.pegacorn.wups.archetypes.petasosenabled.messageprocessingbased.MOAStandardWUP;

import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class BaseFHIRCommunicationToHL7v2xMessageWUP extends MOAStandardWUP {
	
    private static final Logger LOG = LoggerFactory.getLogger(BaseFHIRCommunicationToHL7v2xMessageWUP.class);

    private String WUP_VERSION="1.0.0";

    @Inject
    private FHIRElementTopicFactory fhirTopicFactory;

    @Inject
    private PegacornReferenceProperties referenceProperties;

	@Inject
	protected HL7V2XTopicFactory hl7v2xTopicIDBuilder;

	@Inject
	private TransformWorkshop workshop;
	
	@Inject
	private FreeMarkerConfiguration freemarkerConfig;
	
	@Override
	protected WorkshopInterface specifyWorkshop() {
		return (workshop);
	}

	@Override
	protected String specifyParticipantDisplayName(){
		return("OutboundHL7MessageTransformationEngine");
	}

    @Override
    protected Logger specifyLogger() {
        return (LOG);
    }

	@Override
	public void configure() throws Exception {
        getLogger().info("{}:: ingresFeed() --> {}", getClass().getName(), ingresFeed());
        getLogger().info("{}:: egressFeed() --> {}", getClass().getName(), egressFeed());
        
        // This will make sure the file exists during app startup
        String fileName = System.getenv("TRANSFORMATION_CONFIG_FILE_LOCATION") + "/" + System.getenv("KUBERNETES_SERVICE_NAME") + "/" + System.getenv("KUBERNETES_SERVICE_NAME") + "-egress-transformation-config.ftl";
        File file = new File(fileName);
        
        if (!file.exists()) {
        	throw new RuntimeException("Transformation file not found: " + fileName);
        }

        
        fromIncludingPetasosServices(ingresFeed())
			.routeId(getNameSet().getRouteCoreWUP())
                        .bean(HL7v2xMessageOutOfFHIRCommunication.class, "extractMessage")
			.bean(freemarkerConfig,"configure(*, Exchange)")
			.to("freemarker:file:" + fileName + "?contentCache=false&allowTemplateFromHeader=true&allowContextMapAll=true")
			.bean(HL7v2xTransformMessage.class, "postTransformProcessing(*, Exchange)")
			.to(egressFeed());
	}


    @Override
    protected List<DataParcelManifest> specifySubscriptionTopics() {
        List<DataParcelManifest> subscriptionList = new ArrayList<>();
        DataParcelTypeDescriptor parcelDescriptor = fhirTopicFactory.newTopicToken(ResourceType.Communication.name(), referenceProperties.getPegacornDefaultFHIRVersion());
        DataParcelManifest manifest = new DataParcelManifest();
        manifest.setContainerDescriptor(parcelDescriptor);
        manifest.setSourceSystem("*");
        manifest.setIntendedTargetSystem("*");
        manifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INFORMATION_FLOW_OUTBOUND_DATA_PARCEL);
        manifest.setDataParcelType(DataParcelTypeEnum.GENERAL_DATA_PARCEL_TYPE);
        manifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATION_ANY);
        manifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_ANY);
        manifest.setEnforcementPointApprovalStatus(PolicyEnforcementPointApprovalStatusEnum.POLICY_ENFORCEMENT_POINT_APPROVAL_POSITIVE);
        subscriptionList.add(manifest);
        return (subscriptionList);
    }

    @Override
    protected String specifyWUPInstanceName() {
        return (getClass().getSimpleName());
    }

    @Override
    protected String specifyWUPInstanceVersion() {
        return (WUP_VERSION);
    }

    @Override
    protected List<DataParcelManifest> declarePublishedTopics() {
        return (new ArrayList<>());
    }
}
