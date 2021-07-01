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

import net.fhirfactory.pegacorn.components.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.components.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.components.dataparcel.valuesets.*;
import net.fhirfactory.pegacorn.components.interfaces.topology.WorkshopInterface;
import net.fhirfactory.pegacorn.internals.PegacornReferenceProperties;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.FHIRElementTopicFactory;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.gatekeeper.beans.PegacornEdgeHL7v2xPolicyEnforcementPoint;
import net.fhirfactory.pegacorn.workshops.GatekeeperWorkshop;
import net.fhirfactory.pegacorn.wups.archetypes.petasosenabled.messageprocessingbased.MOAStandardWUP;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class HL7v2xMessageEgressGatekeeperWUP extends MOAStandardWUP {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xMessageEgressGatekeeperWUP.class);

    private static String WUP_NAME = "HL7v2xMessageEgressGatekeeperWUP";
    private static String WUP_VERSION = "1.0.0";

    @Inject
    private GatekeeperWorkshop gatekeeperWorkshop;

    @Inject
    private PegacornReferenceProperties referenceProperties;

    @Inject
    private FHIRElementTopicFactory fhirTopicFactory;

    @Override
    protected Logger specifyLogger() {
        return LOG;
    }

    @Override
    protected List<DataParcelManifest> specifySubscriptionTopics() {
        LOG.debug(".specifySubscriptionTopics(): Entry");
        List<DataParcelManifest> subscriptionList = new ArrayList<>();
        DataParcelManifest communicationEvents = new DataParcelManifest();
        DataParcelTypeDescriptor fhirCommunicationDescriptor = fhirTopicFactory.newTopicToken(ResourceType.Communication.name(), referenceProperties.getPegacornDefaultFHIRVersion());
        communicationEvents.setContainerDescriptor(fhirCommunicationDescriptor);
        communicationEvents.setDataParcelFlowDirection(DataParcelDirectionEnum.OUTBOUND_DATA_PARCEL);
        communicationEvents.setEnforcementPointApprovalStatus(PolicyEnforcementPointApprovalStatusEnum.POLICY_ENFORCEMENT_POINT_APPROVAL_NEGATIVE);
        communicationEvents.setDataParcelType(DataParcelTypeEnum.GENERAL_DATA_PARCEL_TYPE);
        communicationEvents.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATION_ANY);
        communicationEvents.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_ANY);
        communicationEvents.setInterSubsystemDistributable(false);
        subscriptionList.add(communicationEvents);
        return (subscriptionList);
    }

    @Override
    protected String specifyWUPInstanceName() {
        return (WUP_NAME);
    }

    @Override
    protected String specifyWUPInstanceVersion() {
        return (WUP_VERSION);
    }

    @Override
    protected WorkshopInterface specifyWorkshop() {
        return (gatekeeperWorkshop);
    }

    @Override
    public void configure() throws Exception {
        getLogger().info(this.getClass().getName() + ":: ingresFeed() --> {}", this.ingresFeed());
        getLogger().info(this.getClass().getName() + ":: egressFeed() --> {}", this.egressFeed());

        fromIncludingPetasosServices(ingresFeed())
                .routeId(getNameSet().getRouteCoreWUP())
                .bean(PegacornEdgeHL7v2xPolicyEnforcementPoint.class, "enforceEgressPolicy")
                .to(egressFeed());
    }
}
