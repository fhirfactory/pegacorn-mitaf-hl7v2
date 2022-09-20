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

import net.fhirfactory.dricats.interfaces.topology.WorkshopInterface;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.*;
import net.fhirfactory.dricats.internals.fhir.r4.internal.topics.HL7V2XTopicFactory;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.HL7v2xOutboundMessageTransformationExceptionHandler;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.HL7v2xOutboundMessageTransformationPostProcessor;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.FreeMarkerConfiguration;
import net.fhirfactory.dricats.petasos.participant.workshops.TransformWorkshop;
import net.fhirfactory.dricats.petasos.participant.wup.messagebased.MOAStandardWUP;
import org.apache.camel.LoggingLevel;
import org.apache.camel.model.OnExceptionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class BaseHL7v2xOutboundMessageTransformationWUP extends MOAStandardWUP {
	
    private static final Logger LOG = LoggerFactory.getLogger(BaseHL7v2xOutboundMessageTransformationWUP.class);

    private String WUP_VERSION="1.0.0";

	@Inject
	protected HL7V2XTopicFactory hl7v2xTopicIDBuilder;

	@Inject
	private TransformWorkshop workshop;
	
	@Inject
	private FreeMarkerConfiguration freemarkerConfig;

    @Inject
    private HL7v2xOutboundMessageTransformationExceptionHandler generalExceptionHandler;

    @Inject
    private HL7v2xOutboundMessageTransformationPostProcessor outboundMessageTransformationPostProcessor;

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

    //
    // Route
    //

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

        handleGeneralException();

        fromIncludingPetasosServicesNoExceptionHandling(ingresFeed())
                .routeId(getNameSet().getRouteCoreWUP())
                .bean(freemarkerConfig,"configure(*, Exchange)")
                .to("freemarker:file:" + fileName + "?contentCache=false&allowTemplateFromHeader=true&allowContextMapAll=true")
                .bean(outboundMessageTransformationPostProcessor, "postTransformProcessing(*, Exchange)")
                .to(egressFeed());
	}

    //
    // Exception Handler for (Outbound) Transformation WUPs
    //

    protected OnExceptionDefinition handleGeneralException(){
        OnExceptionDefinition exceptionDef = onException(Exception.class)
                .log(LoggingLevel.INFO, ".handleGeneralException(): Exception (General Exception)...")
                .handled(true)
                .bean(generalExceptionHandler, "processException(*, Exchange)")
                .to(egressFeed());
        return(exceptionDef);
    }

    //
    // WUP Construction/Definition
    //

    @Override
    protected List<DataParcelManifest> specifySubscriptionTopics() {
        List<DataParcelManifest> subscriptionList = new ArrayList<>();

        DataParcelManifest subscriptionManifest = new DataParcelManifest();

        DataParcelTypeDescriptor messageDescriptor = new DataParcelTypeDescriptor();
        messageDescriptor.setDataParcelDefiner(hl7v2xTopicIDBuilder.getHl7MessageDefiner());
        messageDescriptor.setDataParcelCategory(hl7v2xTopicIDBuilder.getHl7MessageCategory());
        messageDescriptor.setDataParcelSubCategory(DataParcelManifest.WILDCARD_CHARACTER);
        messageDescriptor.setDataParcelResource(DataParcelManifest.WILDCARD_CHARACTER);
        messageDescriptor.setDataParcelDiscriminatorType(DataParcelManifest.WILDCARD_CHARACTER);
        messageDescriptor.setDataParcelDiscriminatorValue(DataParcelManifest.WILDCARD_CHARACTER);
        messageDescriptor.setVersion(DataParcelManifest.WILDCARD_CHARACTER);
        subscriptionManifest.setContentDescriptor(messageDescriptor);

        subscriptionManifest.setSourceSystem("*");
        subscriptionManifest.setIntendedTargetSystem("*");

        subscriptionManifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INFORMATION_FLOW_OUTBOUND_DATA_PARCEL);
        subscriptionManifest.setDataParcelType(DataParcelTypeEnum.GENERAL_DATA_PARCEL_TYPE);
        subscriptionManifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATED_FALSE);
        subscriptionManifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_TRUE);
        subscriptionManifest.setEnforcementPointApprovalStatus(PolicyEnforcementPointApprovalStatusEnum.POLICY_ENFORCEMENT_POINT_APPROVAL_POSITIVE);
        subscriptionManifest.setInterSubsystemDistributable(false);

        subscriptionList.add(subscriptionManifest);
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
