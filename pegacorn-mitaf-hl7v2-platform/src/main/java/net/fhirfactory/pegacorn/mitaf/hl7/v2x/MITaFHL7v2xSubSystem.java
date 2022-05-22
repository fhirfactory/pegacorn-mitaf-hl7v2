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

import net.fhirfactory.pegacorn.core.model.topology.role.ProcessingPlantRoleEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.*;
import net.fhirfactory.pegacorn.core.model.petasos.participant.PetasosParticipantRegistration;
import net.fhirfactory.pegacorn.core.model.petasos.task.datatypes.work.datatypes.TaskWorkItemManifestType;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.model.SimpleSubscriptionItem;
import net.fhirfactory.pegacorn.processingplant.ProcessingPlant;
import org.hl7.fhir.r4.model.ResourceType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class MITaFHL7v2xSubSystem extends ProcessingPlant {

    private boolean mitafHL7v2SubsystemInitialised;


    public MITaFHL7v2xSubSystem(){
        super();
        mitafHL7v2SubsystemInitialised = false;
    }

    @Override
    protected void executePostConstructActivities() {
        getLogger().debug(".executePostConstructActivities(): Entry");
        if(this.mitafHL7v2SubsystemInitialised){
            getLogger().debug(".executePostConstructActivities(): Exit, already initialised");
            return;
        }
        getLogger().info(".executePostConstructActivities(): Entry");
        List<SimpleSubscriptionItem> subscriptionList = registerSubscriptionList();
        getLogger().info(".executePostConstructActivities(): subscriberList (size)->{}", subscriptionList.size());
        Set<TaskWorkItemManifestType> manifestList = new HashSet<>();
        for(SimpleSubscriptionItem currentSimpleSubscription: subscriptionList) {
            getLogger().info(".executePostConstructActivities(): currentSimpleSubscription->{}", currentSimpleSubscription);
            List<DataParcelTypeDescriptor> descriptorList = currentSimpleSubscription.getDescriptorList();
            String currentSource = currentSimpleSubscription.getSourceSystem();
            for (DataParcelTypeDescriptor currentDescriptor : descriptorList) {
                getLogger().info(".executePostConstructActivities(): SourceSubsystem->{}, currentDescriptor->{}", currentSource, currentDescriptor);
                getLogger().info(".executePostConstructActivities(): Invoking subscribeToRemoteDataParcels()!");

                getLogger().info(".executePostConstructActivities(): currentDescriptor->{}", currentDescriptor);
                DataParcelTypeDescriptor container = getFHIRElementTopicFactory().newTopicToken(ResourceType.Communication.name(), getPegacornReferenceProperties().getPegacornDefaultFHIRVersion());
                getLogger().info(".executePostConstructActivities(): container->{}", container);
                TaskWorkItemManifestType manifest = new TaskWorkItemManifestType();
                manifest.setContentDescriptor(currentDescriptor);
                manifest.setEnforcementPointApprovalStatus(PolicyEnforcementPointApprovalStatusEnum.POLICY_ENFORCEMENT_POINT_APPROVAL_POSITIVE);
                manifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INFORMATION_FLOW_INBOUND_DATA_PARCEL);
                manifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_TRUE);
                manifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATED_TRUE);
                manifest.setDataParcelType(DataParcelTypeEnum.GENERAL_DATA_PARCEL_TYPE);
                manifest.setInterSubsystemDistributable(true);
                manifest.setSourceSystem(currentSource);
                manifest.setSourceProcessingPlantParticipantName(currentSimpleSubscription.getSourceSubsystemParticipantName());
                manifestList.add(manifest);
            }
        }
        getLogger().info(".executePostConstructActivities(): Registration Processing Plant Petasos Participant ... :)");
        PetasosParticipantRegistration participantRegistration = getLocalPetasosParticipantCacheIM().registerPetasosParticipant(getMeAsASoftwareComponent(), new HashSet<>(), manifestList);
        getLogger().info(".executePostConstructActivities(): Registration Processing Plant Petasos Participant, registration->{}!", participantRegistration);
        getLogger().info(".executePostConstructActivities(): Exit");
        this.mitafHL7v2SubsystemInitialised = true;
    }

    abstract protected List<SimpleSubscriptionItem> registerSubscriptionList();

    @Override
    public ProcessingPlantRoleEnum getProcessingPlantCapability() {
        return (ProcessingPlantRoleEnum.PETASOS_SERVICE_PROVIDER_MITAF_GENERAL);
    }
}
