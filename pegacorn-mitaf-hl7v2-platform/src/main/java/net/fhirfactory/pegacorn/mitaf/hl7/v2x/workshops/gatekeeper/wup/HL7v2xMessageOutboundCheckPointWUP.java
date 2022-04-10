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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.gatekeeper.wup;

import net.fhirfactory.pegacorn.core.constants.systemwide.PegacornReferenceProperties;
import net.fhirfactory.pegacorn.core.interfaces.topology.WorkshopInterface;
import net.fhirfactory.pegacorn.core.model.petasos.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.petasos.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.petasos.dataparcel.valuesets.*;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.HL7V2XTopicFactory;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.gatekeeper.beans.PegacornEdgeHL7v2xPolicyEnforcementPoint;
import net.fhirfactory.pegacorn.workshops.PolicyEnforcementWorkshop;
import net.fhirfactory.pegacorn.wups.archetypes.petasosenabled.messageprocessingbased.MOAStandardWUP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class HL7v2xMessageOutboundCheckPointWUP extends MOAStandardWUP {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xMessageOutboundCheckPointWUP.class);

    private static String WUP_VERSION = "1.0.0";

    @Inject
    private PolicyEnforcementWorkshop policyEnforcementWorkshop;

    @Inject
    private PegacornReferenceProperties referenceProperties;

    @Inject
    private HL7V2XTopicFactory topicFactory;

    @Override
    protected Logger specifyLogger() {
        return LOG;
    }

    @Override
    protected List<DataParcelManifest> specifySubscriptionTopics() {
        getLogger().debug(".specifySubscriptionTopics(): Entry");
        List<DataParcelManifest> subscriptionList = new ArrayList<>();
        DataParcelManifest hl7EventMessageManifest = new DataParcelManifest();

        DataParcelTypeDescriptor hl7EventDescriptor = new DataParcelTypeDescriptor();
        hl7EventDescriptor.setDataParcelDefiner(topicFactory.getHl7MessageDefiner());
        hl7EventDescriptor.setDataParcelCategory(topicFactory.getHl7MessageCategory());
        hl7EventDescriptor.setDataParcelSubCategory(DataParcelManifest.WILDCARD_CHARACTER);
        hl7EventDescriptor.setDataParcelResource(DataParcelManifest.WILDCARD_CHARACTER);
        hl7EventDescriptor.setDataParcelDiscriminatorValue(DataParcelManifest.WILDCARD_CHARACTER);
        hl7EventDescriptor.setDataParcelDiscriminatorType(DataParcelManifest.WILDCARD_CHARACTER);
        hl7EventMessageManifest.setContainerDescriptor(hl7EventDescriptor);
        hl7EventMessageManifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INFORMATION_FLOW_OUTBOUND_DATA_PARCEL);
        hl7EventMessageManifest.setSourceSystem(DataParcelManifest.WILDCARD_CHARACTER);
        hl7EventMessageManifest.setIntendedTargetSystem(DataParcelManifest.WILDCARD_CHARACTER);
        hl7EventMessageManifest.setEnforcementPointApprovalStatus(PolicyEnforcementPointApprovalStatusEnum.POLICY_ENFORCEMENT_POINT_APPROVAL_NEGATIVE);
        hl7EventMessageManifest.setDataParcelType(DataParcelTypeEnum.GENERAL_DATA_PARCEL_TYPE);
        hl7EventMessageManifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATION_ANY);
        hl7EventMessageManifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_ANY);
        hl7EventMessageManifest.setExternallyDistributable(DataParcelExternallyDistributableStatusEnum.DATA_PARCEL_EXTERNALLY_DISTRIBUTABLE_ANY);
        hl7EventMessageManifest.setInterSubsystemDistributable(false);
        subscriptionList.add(hl7EventMessageManifest);
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
    protected WorkshopInterface specifyWorkshop() {
        return (policyEnforcementWorkshop);
    }

    @Override
    protected String specifyParticipantDisplayName(){
        return("OutboundMessageCheckpoint");
    }

    @Override
    public void configure() throws Exception {
        getLogger().info("{}:: ingresFeed() --> {}", getClass().getName(), ingresFeed());
        getLogger().info("{}:: egressFeed() --> {}", getClass().getName(), egressFeed());

        fromIncludingPetasosServices(ingresFeed())
                .routeId(getNameSet().getRouteCoreWUP())
                .bean(PegacornEdgeHL7v2xPolicyEnforcementPoint.class, "enforceOutboundPolicy")
                .to(egressFeed());
    }

    @Override
    protected List<DataParcelManifest> declarePublishedTopics() {
        return (new ArrayList<>());
    }
}
