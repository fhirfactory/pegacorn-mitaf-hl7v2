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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents;

import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelDirectionEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelNormalisationStatusEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelValidationStatusEnum;
import net.fhirfactory.pegacorn.core.model.petasos.task.PetasosFulfillmentTask;
import net.fhirfactory.pegacorn.core.model.petasos.task.datatypes.work.datatypes.TaskWorkItemType;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.base.IPCTopologyEndpoint;
import net.fhirfactory.pegacorn.core.model.topology.nodes.WorkUnitProcessorSoftwareComponent;
import net.fhirfactory.pegacorn.internals.hl7v2.triggerevents.valuesets.HL7v2SegmentTypeEnum;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.common.HL7v2xMessageEncapsulatorBase;
import net.fhirfactory.pegacorn.petasos.core.tasks.accessors.PetasosFulfillmentTaskSharedInstance;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.EndpointMetricsAgent;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.WorkUnitProcessorMetricsAgent;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HL7v2xQueryClientResponseProcessor extends HL7v2xMessageEncapsulatorBase {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xQueryClientResponseProcessor.class);

    //
    // Constructor(s)
    //

    //
    // PostConstruct
    //

    //
    // Getters and Setters
    //

    @Override
    protected Logger getLogger(){
        return(LOG);
    }

    //
    // Business Methods
    //

    public String phase1ExtractACKContentFromExchange(String incoming, Exchange camelExchange){
        getLogger().info(".extractACKContent(): Entry, incoming --> {}", incoming);
        String responseString = camelExchange.getMessage().getHeader("CamelMllpAcknowledgementString", String.class);
        getLogger().info(".extractACKContent(): extracted response --> {}", responseString);
        return(responseString);
    }


    private PetasosFulfillmentTask phase2EncapsulateMessageIntoFulfillmentTask(String message, Exchange camelExchange){
        getLogger().debug(".encapsulateMessage(): Entry, message->{}", message);

        PetasosFulfillmentTaskSharedInstance fulfillmentTaskFromExchange = camelExchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY, PetasosFulfillmentTaskSharedInstance.class);
        PetasosFulfillmentTask fulfillmentTask = SerializationUtils.clone(fulfillmentTaskFromExchange.getInstance());

        //
        // add to Processing Plant metrics
        getProcessingPlantMetricsAgent().incrementIngresMessageCount();

        //
        // add to WUP Metrics
        WorkUnitProcessorMetricsAgent metricsAgent = camelExchange.getProperty(PetasosPropertyConstants.WUP_METRICS_AGENT_EXCHANGE_PROPERTY, WorkUnitProcessorMetricsAgent.class);
        metricsAgent.incrementIngresMessageCount();
        metricsAgent.touchLastActivityInstant();
        //
        // Add to Endpoint Metrics
        EndpointMetricsAgent endpointMetricsAgent = camelExchange.getProperty(PetasosPropertyConstants.ENDPOINT_METRICS_AGENT_EXCHANGE_PROPERTY, EndpointMetricsAgent.class);
        endpointMetricsAgent.incrementIngresMessageCount();
        endpointMetricsAgent.touchLastActivityInstant();

        getLogger().debug(".encapsulateMessage(): Extracting header details");
        String messageTriggerEvent = getDefensivePipeParser().getTriggerEvent(message);
        getLogger().trace(".encapsulateMessage(): message::messageTriggerEvent --> {}", messageTriggerEvent);
        String messageEventType = getDefensivePipeParser().getMessageCode(message);
        getLogger().trace(".encapsulateMessage(): message::messageEventType --> {}", messageEventType);
        String messageVersion = getDefensivePipeParser().getMessageVersion(message);
        getLogger().trace(".encapsulateMessage(): message::MessageVersion --> {}", messageVersion);


        try {
            //
            // Add some notifications
            String targetPort = camelExchange.getProperty(PetasosPropertyConstants.ENDPOINT_PORT_VALUE, String.class);

            String mshSegment = null;
            String pidSegment = null;
            try{
                mshSegment = getDefensivePipeParser().extractSegment(message, HL7v2SegmentTypeEnum.MSH);
                pidSegment = getDefensivePipeParser().extractSegment(message, HL7v2SegmentTypeEnum.PID);
                if(StringUtils.isEmpty(pidSegment)) {
                    pidSegment = "PID: Unknown";
                }
                if(StringUtils.isEmpty(mshSegment)){
                    mshSegment = "MSH: unknown";
                }
            } catch (Exception encodingException) {
                mshSegment = "MSH: unknown";
                pidSegment = "PID: Unknown";
            }

            String portDescription = null;
            try{
                WorkUnitProcessorSoftwareComponent workUnitProcessorSoftwareComponent = camelExchange.getProperty(PetasosPropertyConstants.WUP_TOPOLOGY_NODE_EXCHANGE_PROPERTY_NAME, WorkUnitProcessorSoftwareComponent.class);
                IPCTopologyEndpoint ingresEndpoint = workUnitProcessorSoftwareComponent.getIngresEndpoint();
                portDescription = ingresEndpoint.getParticipantDisplayName();
            } catch(Exception ex){
                if(StringUtils.isNotEmpty(targetPort)){
                    portDescription = "ServerPort:" + targetPort;
                } else{
                    portDescription = "ServerPort: Unknown";
                }
            }

            //
            // Send ITOps Console Notification about Message Arrival
            sendMessageReceivedConsoleNotification(portDescription, message, mshSegment, pidSegment, endpointMetricsAgent);

            //
            // Now actually process the UoW/Message
            UoWProcessingOutcomeEnum outcomeEnum;
            String outcomeDescription;

            //
            // Only use the first version subfield.
            String messageVersionFirstField = messageVersion;
            int indexOfFieldSeperator = messageVersion.indexOf("^");

            if (indexOfFieldSeperator != -1) {
                messageVersionFirstField = messageVersion.substring(0, indexOfFieldSeperator);
            }

            outcomeEnum = UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS;
            outcomeDescription = "All Good!";

            //
            // Create the DataParcelManifest for the message
            getLogger().trace(".encapsulateMessage(): Creating Data Parcel Descriptor (messageDescriptor)");
            DataParcelTypeDescriptor messageDescriptor = createDataParcelTypeDescriptor(messageEventType, messageTriggerEvent, messageVersionFirstField );
             getLogger().trace(".encapsulateMessage(): messageDescriptor created->{}", messageDescriptor);
            getLogger().trace(".encapsulateMessage(): Creating Data Parcel Manifest (messageManifest)");
            DataParcelManifest messageManifest = new DataParcelManifest();
            messageManifest.setContentDescriptor(messageDescriptor);
            messageManifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_FALSE);
            messageManifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATED_TRUE);
            messageManifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INFORMATION_FLOW_INBOUND_DATA_PARCEL);
            messageManifest.setSourceProcessingPlantParticipantName(getParticipantNameHolder().getSubsystemParticipantName());
            getLogger().trace(".encapsulateMessage(): messageManifest created->{}", messageManifest);

            //
            // Populate the UoWPayload
            getLogger().trace(".encapsulateMessage(): Creating Egress Payload (newPayload)");
            UoWPayload newPayload = new UoWPayload();
            newPayload.setPayload(message);
            newPayload.setPayloadManifest(messageManifest);
            getLogger().trace(".encapsulateMessage(): newPayload created->{}", newPayload);

            fulfillmentTask.getTaskWorkItem().getEgressContent().addPayloadElement(newPayload);
            fulfillmentTask.getTaskWorkItem().setProcessingOutcome(outcomeEnum);
            fulfillmentTask.getTaskWorkItem().setFailureDescription(outcomeDescription);
            //
            // All Done!
            getLogger().debug(".encapsulateMessage(): Exit, fulfillmentTask->{}", fulfillmentTask);
            return(fulfillmentTask);
        } catch (Exception ex) {
            getLogger().warn(".encapsulateMessage(): Exception occurred", ex);
            UoWPayload newPayload = new UoWPayload();
            if(message != null){
                newPayload.setPayload(message.toString());
                newPayload.setPayloadManifest(getTopicFactory().newBadDataParcelManifest());
            } else {
                newPayload.setPayload("Unable to decipher input message");
                newPayload.setPayloadManifest(getTopicFactory().newBadDataParcelManifest());
            }
            fulfillmentTask.getTaskWorkItem().getEgressContent().addPayloadElement(newPayload);
            fulfillmentTask.getTaskWorkItem().setFailureDescription(ex.toString());
            fulfillmentTask.getTaskWorkItem().setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED);
            getLogger().debug(".encapsulateMessage(): Exit, fulfillmentTask->{}", fulfillmentTask);
            return(fulfillmentTask);
        }
    }
    
    public UoW phase3ResolveUoWFromFulfillmentTask(PetasosFulfillmentTaskSharedInstance fulfillmentTask, Exchange camelExchange){
        getLogger().debug(".resolveFulfillmentTask(): Entry, fulfillmentTask->{}", fulfillmentTask);

        UoW uow = SerializationUtils.clone(fulfillmentTask.getTaskWorkItem());

        getLogger().debug(".resolveFulfillmentTask(): Exit, uow->{}", uow);
        return(uow);
    }

}
