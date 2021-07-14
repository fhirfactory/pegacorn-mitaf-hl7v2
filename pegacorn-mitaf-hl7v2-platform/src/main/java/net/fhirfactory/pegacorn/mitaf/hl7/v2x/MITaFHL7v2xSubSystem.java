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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x;

import net.fhirfactory.pegacorn.components.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.components.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.components.dataparcel.valuesets.DataParcelDirectionEnum;
import net.fhirfactory.pegacorn.components.dataparcel.valuesets.PolicyEnforcementPointApprovalStatusEnum;
import net.fhirfactory.pegacorn.internals.PegacornReferenceProperties;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.FHIRElementTopicFactory;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.model.SimpleSubscriptionItem;
import net.fhirfactory.pegacorn.platform.edge.model.pubsub.RemoteSubscriptionResponse;
import net.fhirfactory.pegacorn.platform.edge.services.InterSubSystemPubSubBroker;
import net.fhirfactory.pegacorn.processingplant.ProcessingPlant;
import org.hl7.fhir.r4.model.ResourceType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public abstract class MITaFHL7v2xSubSystem extends ProcessingPlant {

    @Inject
    private InterSubSystemPubSubBroker pubSubBroker;

    @Inject
    private FHIRElementTopicFactory fhirElementTopicFactory;

    @Inject
    private PegacornReferenceProperties pegacornReferenceProperties;

    protected RemoteSubscriptionResponse subscribeToRemoteDataParcels(List<DataParcelTypeDescriptor> triggerEventList, String sourceSystem){
        getLogger().info(".subscribeToRemoteDataParcels(): Entry, sourceSystem->{}", sourceSystem);
        if(triggerEventList.isEmpty()){
            RemoteSubscriptionResponse nothingToDoResponse = new RemoteSubscriptionResponse();
            nothingToDoResponse.setSubscriptionSuccessful(true);
            return(nothingToDoResponse);
        }
        getLogger().info(".subscribeToRemoteDataParcels(): We have entries in the subscription list, processing");
        List<DataParcelManifest> manifestList = new ArrayList<>();
        for(DataParcelTypeDescriptor currentTriggerEvent: triggerEventList){
            DataParcelTypeDescriptor container = fhirElementTopicFactory.newTopicToken(ResourceType.Communication.name(), pegacornReferenceProperties.getPegacornDefaultFHIRVersion());
            DataParcelManifest manifest = new DataParcelManifest();
            manifest.setContentDescriptor(currentTriggerEvent);
            manifest.setContainerDescriptor(container);
            manifest.setEnforcementPointApprovalStatus(PolicyEnforcementPointApprovalStatusEnum.POLICY_ENFORCEMENT_POINT_APPROVAL_POSITIVE);
            manifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INBOUND_DATA_PARCEL);
            manifest.setInterSubsystemDistributable(true);
            manifestList.add(manifest);
        }
        getLogger().info(".subscribeToRemoteDataParcels(): Invoking pubsubBroker.subscriber()");
        RemoteSubscriptionResponse response = pubSubBroker.subscribe(manifestList, sourceSystem);
        getLogger().info(".subscribeToRemoteDataParcels(): Exit, response->{}", response);
        return(response);
    }

    @Override
    protected void executePostConstructActivities() {
        getLogger().info(".executePostConstructActivities(): Entry");
        List<SimpleSubscriptionItem> subscriptionList = registerSubscriptionList();
        for(SimpleSubscriptionItem currentSimpleSubscription: subscriptionList){
            List<DataParcelTypeDescriptor> descriptorList = currentSimpleSubscription.getDescriptorList();
            String currentSource = currentSimpleSubscription.getSourceSystem();
            for(DataParcelTypeDescriptor currentDescriptor: descriptorList) {
                getLogger().info(".executePostConstructActivities(): SourceSubsystem->{}, currentDescriptor->{}",currentSource, currentDescriptor);
                subscribeToRemoteDataParcels(descriptorList, currentSource);
            }
        }getLogger().info(".executePostConstructActivities(): Exit");

    }

    abstract protected List<SimpleSubscriptionItem> registerSubscriptionList();

}
