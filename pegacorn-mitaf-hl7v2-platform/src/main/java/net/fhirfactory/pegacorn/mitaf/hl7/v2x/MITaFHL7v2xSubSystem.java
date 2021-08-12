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

import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelDirectionEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.PolicyEnforcementPointApprovalStatusEnum;

import net.fhirfactory.pegacorn.deployment.properties.codebased.PegacornReferenceProperties;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.FHIRElementTopicFactory;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.model.SimpleSubscriptionItem;
import net.fhirfactory.pegacorn.petasos.model.pubsub.InterSubsystemPubSubPublisherSubscriptionRegistration;
import net.fhirfactory.pegacorn.platform.edge.services.InterSubSystemPubSubBroker;
import net.fhirfactory.pegacorn.processingplant.ProcessingPlant;
import org.hl7.fhir.r4.model.ResourceType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public abstract class MITaFHL7v2xSubSystem extends ProcessingPlant {

    private boolean mitafHL7v2SubsystemInitialised;

    @Inject
    private InterSubSystemPubSubBroker pubSubBroker;

    @Inject
    private FHIRElementTopicFactory fhirElementTopicFactory;

    @Inject
    private PegacornReferenceProperties pegacornReferenceProperties;

    public MITaFHL7v2xSubSystem(){
        super();
        mitafHL7v2SubsystemInitialised = false;
    }

    protected InterSubsystemPubSubPublisherSubscriptionRegistration subscribeToRemoteDataParcels(List<DataParcelTypeDescriptor> triggerEventList, String sourceSystem){
        getLogger().debug(".subscribeToRemoteDataParcels(): Entry, sourceSystem->{}", sourceSystem);
        if(triggerEventList.isEmpty()){
            return(null);
        }
        getLogger().trace(".subscribeToRemoteDataParcels(): We have entries in the subscription list, processing");
        List<DataParcelManifest> manifestList = new ArrayList<>();
        for(DataParcelTypeDescriptor currentTriggerEvent: triggerEventList){
            getLogger().trace(".subscribeToRemoteDataParcels(): currentTriggerEvent->{}", currentTriggerEvent);
            DataParcelTypeDescriptor container = fhirElementTopicFactory.newTopicToken(ResourceType.Communication.name(), pegacornReferenceProperties.getPegacornDefaultFHIRVersion());
            getLogger().trace(".subscribeToRemoteDataParcels(): container->{}", container);
            DataParcelManifest manifest = new DataParcelManifest();
            manifest.setContentDescriptor(currentTriggerEvent);
            manifest.setContainerDescriptor(container);
            manifest.setEnforcementPointApprovalStatus(PolicyEnforcementPointApprovalStatusEnum.POLICY_ENFORCEMENT_POINT_APPROVAL_POSITIVE);
            manifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INBOUND_DATA_PARCEL);
            manifest.setInterSubsystemDistributable(true);
            manifest.setSourceSystem(sourceSystem);
            manifestList.add(manifest);
        }
        getLogger().trace(".subscribeToRemoteDataParcels(): Invoking pubsubBroker.subscriber() ... :)");
        if(pubSubBroker == null){
            getLogger().warn(".subscribeToRemoteDataParcels(): Warning, pubSubBroker is null");
        }
        InterSubsystemPubSubPublisherSubscriptionRegistration subscriptionRegistration = pubSubBroker.subscribe(manifestList, sourceSystem);
        getLogger().debug(".subscribeToRemoteDataParcels(): Exit, subscriptionRegistration->{}", subscriptionRegistration);
        return(subscriptionRegistration);
    }

    @Override
    protected void executePostConstructActivities() {
        if(this.mitafHL7v2SubsystemInitialised){
            return;
        }
        getLogger().info(".executePostConstructActivities(): Entry");
        List<SimpleSubscriptionItem> subscriptionList = registerSubscriptionList();
        getLogger().info(".executePostConstructActivities(): subscriberList (size)->{}", subscriptionList.size());
        for(SimpleSubscriptionItem currentSimpleSubscription: subscriptionList){
            getLogger().info(".executePostConstructActivities(): currentSimpleSubscription->{}", currentSimpleSubscription);
            List<DataParcelTypeDescriptor> descriptorList = currentSimpleSubscription.getDescriptorList();
            String currentSource = currentSimpleSubscription.getSourceSystem();
            for(DataParcelTypeDescriptor currentDescriptor: descriptorList) {
                getLogger().info(".executePostConstructActivities(): SourceSubsystem->{}, currentDescriptor->{}",currentSource, currentDescriptor);
                getLogger().info(".executePostConstructActivities(): Invoking subscribeToRemoteDataParcels()!");
                InterSubsystemPubSubPublisherSubscriptionRegistration subscriptionRegistration = subscribeToRemoteDataParcels(descriptorList, currentSource);
                getLogger().info(".executePostConstructActivities(): Subscription Registered, outcome->{}!", subscriptionRegistration);
            }
        }
        getLogger().info(".executePostConstructActivities(): Exit");
        this.mitafHL7v2SubsystemInitialised = true;
    }

    abstract protected List<SimpleSubscriptionItem> registerSubscriptionList();

}
