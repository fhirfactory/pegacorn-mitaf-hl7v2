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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.capabilities;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import net.fhirfactory.pegacorn.core.model.petasos.task.PetasosActionableTask;
import net.fhirfactory.pegacorn.core.model.petasos.task.datatypes.fulfillment.valuesets.FulfillmentExecutionStatusEnum;
import net.fhirfactory.pegacorn.core.model.petasos.task.datatypes.performer.datatypes.TaskPerformerTypeType;
import net.fhirfactory.pegacorn.core.model.petasos.wup.valuesets.PetasosTaskExecutionStatusEnum;
import net.fhirfactory.pegacorn.core.model.topology.nodes.WorkUnitProcessorSoftwareComponent;
import net.fhirfactory.pegacorn.petasos.core.tasks.accessors.PetasosActionableTaskSharedInstance;
import net.fhirfactory.pegacorn.petasos.core.tasks.accessors.PetasosActionableTaskSharedInstanceAccessorFactory;
import net.fhirfactory.pegacorn.petasos.core.tasks.accessors.PetasosFulfillmentTaskSharedInstance;
import net.fhirfactory.pegacorn.petasos.core.tasks.factories.PetasosActionableTaskFactory;
import net.fhirfactory.pegacorn.petasos.core.tasks.factories.PetasosFulfillmentTaskFactory;
import net.fhirfactory.pegacorn.petasos.core.tasks.management.local.LocalPetasosActionableTaskActivityController;
import net.fhirfactory.pegacorn.petasos.core.tasks.management.local.LocalPetasosFulfilmentTaskActivityController;
import net.fhirfactory.pegacorn.petasos.oam.reporting.tasks.agents.WorkUnitProcessorTaskReportAgent;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import net.fhirfactory.pegacorn.core.model.petasos.task.PetasosFulfillmentTask;
import net.fhirfactory.pegacorn.core.model.petasos.task.datatypes.work.datatypes.TaskWorkItemType;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import org.apache.camel.Exchange;

import java.time.Instant;
import java.util.ArrayList;

@ApplicationScoped
public class HL7v2xQueryCapabilityFulfillerRequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xQueryCapabilityFulfillerRequestHandler.class);

    private HapiContext hapiContext;

    @Inject
    private PetasosActionableTaskSharedInstanceAccessorFactory actionableTaskSharedInstanceFactory;

    @Inject
    private PetasosFulfillmentTaskFactory fulfillmentTaskFactory;

    @Inject
    private PetasosActionableTaskFactory actionableTaskFactory;

    @Inject
    private LocalPetasosActionableTaskActivityController actionableTaskActivityController;

    @Inject
    private LocalPetasosFulfilmentTaskActivityController fulfilmentTaskBroker;

    //
    // Constructor(s)
    //
    
    //
    // PostConstruct
    //
    
    @PostConstruct
    public void initialise(){
        hapiContext = new DefaultHapiContext();
    }
    
    //
    // Business Methods
    //

    /**
     * This method receives the PetasosFulfillmentTask from the calling (Capability Client) and
     * generates a new PetasosFulfillmentTask & PetasosActionableTask for the local actions.
     *
     * @param fulfillmentTask The "calling" (Capability Client) PetasosFulfillmentTask
     * @param softwareComponent
     * @param taskReportAgent
     * @return The UoW associated with the "New" PetasosFulfillmentTask
     */
    public PetasosFulfillmentTaskSharedInstance createLocalTasks(PetasosFulfillmentTask fulfillmentTask, WorkUnitProcessorSoftwareComponent softwareComponent, WorkUnitProcessorTaskReportAgent taskReportAgent){
        getLogger().debug(".createLocalTasks(): Entry, fulfillmentTask->{}", fulfillmentTask);

        getLogger().trace(".forwardTask(): Create PetasosActionableTask (for Local Activity): Start");
        TaskWorkItemType taskWorkItem = SerializationUtils.clone(fulfillmentTask.getTaskWorkItem());
        PetasosActionableTask localActionableTask = actionableTaskFactory.newMessageBasedActionableTask(fulfillmentTask, taskWorkItem);
        TaskPerformerTypeType downstreamPerformerType = new TaskPerformerTypeType();
        downstreamPerformerType.setKnownFulfillerInstance(softwareComponent.getComponentID());
        downstreamPerformerType.setRequiredPerformerType(softwareComponent.getNodeFunctionFDN());
        downstreamPerformerType.setRequiredParticipantName(softwareComponent.getParticipantName());
        downstreamPerformerType.setRequiredPerformerTypeDescription(softwareComponent.getParticipantDisplayName());
        if(!localActionableTask.hasTaskPerformerTypes()){
            localActionableTask.setTaskPerformerTypes(new ArrayList<>());
        }
        localActionableTask.getTaskPerformerTypes().add(downstreamPerformerType);
        getLogger().trace(".forwardTask(): Create PetasosActionableTask (for Local Activity): Finish");
        getLogger().trace(".forwardTask(): Register PetasosActionableTask (for Local Activity): Start");
        localActionableTask.setCreationInstant(Instant.now());
        localActionableTask.setUpdateInstant(Instant.now());
        PetasosActionableTaskSharedInstance petasosActionableTaskSharedInstance = actionableTaskActivityController.registerActionableTask(localActionableTask);
        getLogger().trace(".forwardTask(): Register PetasosActionableTask (for Local Activity): Finish");
        getLogger().trace(".forwardTask(): Send ITOps Notification (for LocalTask): Start");
        if(taskReportAgent != null){
            taskReportAgent.sendITOpsTaskReport(localActionableTask);
        }
        getLogger().trace(".forwardTask(): Send ITOps Notification (for LocalTask): Finish");
        getLogger().trace(".forwardTask(): Create PetasosFulfillmentTask (for Local Activity): Start");
        PetasosFulfillmentTask localFulfillmentTask = fulfillmentTaskFactory.newFulfillmentTask(localActionableTask, softwareComponent);
        getLogger().trace(".forwardTask(): Create PetasosFulfillmentTask (for Local Activity): Finish");
        getLogger().trace(".forwardTask(): Register PetasosFulfillmentTask: Start");
        localFulfillmentTask.setUpdateInstant(Instant.now());
        localFulfillmentTask.setCreationInstant(Instant.now());
        localFulfillmentTask.getTaskFulfillment().setRegistrationInstant(Instant.now());
        PetasosFulfillmentTaskSharedInstance petasosFulfillmentSharedInstance = fulfilmentTaskBroker.registerFulfillmentTask(localFulfillmentTask, false);
        getLogger().trace(".forwardTask(): Register PetasosFulfillmentTask: Finish");

        //
        //

        getLogger().debug(".createLocalTasks(): Exit, new fulfillmentTask->{}", localFulfillmentTask);
        return(petasosFulfillmentSharedInstance);
    }



    
    /**
     * This method takes the local PetasosFulfillmentTask, now containing the
     * response from the Query-Server, and embeds this response into the original
     * "calling" PetasosFulfillmentTask. It then returns the (now updated)
     * "calling" PetasosFulfillmentTask.
     * 
     * @param fulfillmentTask The "local" PetasosFulfillmentTask
     * @param camelExchange
     * @return The "calling" PetasosFulfillmentTask (with update egress content)
     */
    public UoW processCapabilityTaskResponse(PetasosFulfillmentTask fulfillmentTask, Exchange camelExchange){
        
        
        UoW uow = fulfillmentTask.getTaskWorkItem();
        return(uow);
    }



    //
    // Getters (and Setters)
    //

    public HapiContext getHAPIContext() {
        return hapiContext;
    }
    
    protected Logger getLogger(){
        return(LOG);
    }
}
