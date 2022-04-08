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

import net.fhirfactory.pegacorn.core.interfaces.topology.WorkshopInterface;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.*;
import net.fhirfactory.pegacorn.core.model.petasos.participant.ProcessingPlantPetasosParticipantNameHolder;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.HL7V2XTopicFactory;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.model.HL7v2VersionEnum;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.HL7v2MessageAsTextToHL7V2xMessage;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.HL7v2xInboundMessageTransformationExceptionHandler;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.HL7v2xInboundMessageTransformationPostProcessor;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.HL7v2xOutboundMessageTransformationExceptionHandler;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.FreeMarkerConfiguration;
import net.fhirfactory.pegacorn.workshops.TransformWorkshop;
import net.fhirfactory.pegacorn.wups.archetypes.petasosenabled.messageprocessingbased.MOAStandardWUP;
import org.apache.camel.LoggingLevel;
import org.apache.camel.model.OnExceptionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;


public abstract class BaseHL7v2xInboundMessageTransformationWUP extends MOAStandardWUP {
    private static final Logger LOG = LoggerFactory.getLogger(BaseHL7v2xInboundMessageTransformationWUP.class);
    
    private String WUP_VERSION = "1.0.0";

    @Inject
    private TransformWorkshop workshop;

    @Override
    protected WorkshopInterface specifyWorkshop() {
        return (workshop);
    }

    @Inject
    private HL7v2MessageAsTextToHL7V2xMessage hl7v2TextToMessage;

    @Inject
    private HL7V2XTopicFactory topicFactory;

    @Inject
    private FreeMarkerConfiguration freemarkerConfig;

    @Inject
    private HL7v2xInboundMessageTransformationPostProcessor transformationPostProcessor;

    @Inject
    private HL7v2xInboundMessageTransformationExceptionHandler inboundMessageTransformationExceptionHandler;

    @Inject
    private ProcessingPlantPetasosParticipantNameHolder participantNameHolder;

    @Override
    public void configure() throws Exception {
        getLogger().info("{}:: ingresFeed() --> {}", getClass().getName(), ingresFeed());
        getLogger().info("{}:: egressFeed() --> {}", getClass().getName(), egressFeed());

        // This will make sure the file exists during app startup
        String fileName = System.getenv("TRANSFORMATION_CONFIG_FILE_LOCATION") + "/" + System.getenv("KUBERNETES_SERVICE_NAME") + "/" + System.getenv("KUBERNETES_SERVICE_NAME") + "-ingres-transformation-config.ftl";
        File file = new File(fileName);

        if (!file.exists()) {
            throw new RuntimeException("Transformation file not found: " + fileName);
        }

        specifyDefaultInboundExceptionHandler();

        fromIncludingPetasosServices(ingresFeed())
                .routeId(getNameSet().getRouteCoreWUP())
                .bean(hl7v2TextToMessage, "convertToMessage")
                .bean(freemarkerConfig, "configure(*, Exchange)")
                .to("freemarker:file:" + fileName + "?allowTemplateFromHeader=true&allowContextMapAll=true")
                .bean(transformationPostProcessor, "postTransformProcessing(*, Exchange)")
                .to(egressFeed());
    }

    //
    // Exception Handling
    //

    protected OnExceptionDefinition specifyDefaultInboundExceptionHandler(){
        OnExceptionDefinition exceptionDef = onException(Exception.class)
                .handled(true)
                .log(LoggingLevel.WARN, "Exception in Transformation")
                .bean(inboundMessageTransformationExceptionHandler, "processException(*, Exchange)")
                .to(egressFeed());
        return(exceptionDef);
    }


    public HL7V2XTopicFactory getTopicFactory() {
        return topicFactory;
    }

    protected DataParcelManifest createSubscriptionManifestForInteractIngressHL7v2Messages(String eventType, String eventTrigger, HL7v2VersionEnum version) {
        getLogger().info(".createSubscriptionManifestForInteractIngressHL7v2Messages(): Entry, eventType->{}, eventTrigger->{}, version->{}", eventType, eventTrigger, version);
        DataParcelTypeDescriptor descriptor = getTopicFactory().newDataParcelDescriptor(eventType, eventTrigger, version.getVersionText());
        descriptor.setDataParcelDiscriminatorType(DataParcelManifest.WILDCARD_CHARACTER);
        descriptor.setDataParcelDiscriminatorValue(DataParcelManifest.WILDCARD_CHARACTER);
        DataParcelManifest manifest = new DataParcelManifest();
        manifest.setContentDescriptor(descriptor);
        manifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INFORMATION_FLOW_INBOUND_DATA_PARCEL);
        manifest.setDataParcelType(DataParcelTypeEnum.GENERAL_DATA_PARCEL_TYPE);
        manifest.setEnforcementPointApprovalStatus(PolicyEnforcementPointApprovalStatusEnum.POLICY_ENFORCEMENT_POINT_APPROVAL_NEGATIVE);
        manifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_FALSE);
        manifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATED_FALSE);
        manifest.setSourceSystem(DataParcelManifest.WILDCARD_CHARACTER);
        manifest.setIntendedTargetSystem(DataParcelManifest.WILDCARD_CHARACTER);
        manifest.setExternallyDistributable(DataParcelExternallyDistributableStatusEnum.DATA_PARCEL_EXTERNALLY_DISTRIBUTABLE_FALSE);
        manifest.setSourceProcessingPlantParticipantName(participantNameHolder.getSubsystemParticipantName());
        manifest.setSourceProcessingPlantInterfaceName(DataParcelManifest.WILDCARD_CHARACTER);
        manifest.setInterSubsystemDistributable(false);
        getLogger().info(".createSubscriptionManifestForInteractIngressHL7v2Messages(): Exit, manifest->{}", manifest);
        return (manifest);
    }

    protected String specifyParticipantDisplayName() {
        return ("InboundHL7MessageTransformationEngine");
    }
    
    
    @Override
    protected Logger specifyLogger() {
        return (LOG);
    }

    @Override
    protected String specifyWUPInstanceName() {
        return (getClass().getSimpleName());
    }

    @Override
    protected String specifyWUPInstanceVersion() {
        return (WUP_VERSION);
    }

}
