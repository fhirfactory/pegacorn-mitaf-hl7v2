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

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.interfaces.topology.ProcessingPlantInterface;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelDirectionEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelNormalisationStatusEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelValidationStatusEnum;
import net.fhirfactory.pegacorn.core.model.petasos.participant.ProcessingPlantPetasosParticipantNameHolder;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.HL7V2XTopicFactory;
import net.fhirfactory.pegacorn.internals.hl7v2.helpers.UltraDefensivePipeParser;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.datatypes.MLLPMessageActivityParcel;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgent;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgentAccessor;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class HL7v2xTriggerEventIngresProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xTriggerEventIngresProcessor.class);

    private HapiContext context;
    private DateTimeFormatter timeFormatter;
    private boolean initialised;
    private boolean includeFullHL7MessageInLog;
    private Integer maxHL7MessageSize;

    @Inject
    private HL7V2XTopicFactory topicFactory;

    @Inject
    private ProcessingPlantInterface processingPlant;

    @Inject
    private ProcessingPlantPetasosParticipantNameHolder participantNameHolder;

    @Inject
    private ProcessingPlantMetricsAgentAccessor processingPlantMetricsAgentAccessor;

    @Inject
    private UltraDefensivePipeParser defensivePipeParser;

    //
    // Constructor(s)
    //

    public HL7v2xTriggerEventIngresProcessor() {
        context = new DefaultHapiContext();
        timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.of(PetasosPropertyConstants.DEFAULT_TIMEZONE));
        maxHL7MessageSize = 64000;
        includeFullHL7MessageInLog = false;
        initialised = false;
    }

    //
    // Post Construct
    //

    @PostConstruct
    public void initialise(){
        getLogger().debug(".initialise(): Entry");
        if(initialised){
            getLogger().debug(".initialise(): Nothing to do, already initialised!");
        } else {
            getLogger().info(".initialise(): Start");
            getLogger().info(".initialise(): [Check if Full HL7 Message to be included in Log] Start");
            String includeMessageString  = getProcessingPlant().getMeAsASoftwareComponent().getOtherConfigurationParameter(PetasosPropertyConstants.INCLUDE_FULL_HL7_MESSAGE_IN_LOG);
            if(StringUtils.isNotEmpty(includeMessageString)){
                if(includeMessageString.equalsIgnoreCase("true")){
                    setIncludeFullHL7MessageInLog(true);
                }
            }
            getLogger().info(".initialise(): [Check if Full HL7 Message to be included in Log] include->{}", isIncludeFullHL7MessageInLog());
            getLogger().info(".initialise(): [Check if Full HL7 Message to be included in Log] Finish");
            getLogger().info(".initialise(): [Check Size Of HL7 Message to be included in Log] Start");
            String messageMaximumSize  = getProcessingPlant().getMeAsASoftwareComponent().getOtherConfigurationParameter(PetasosPropertyConstants.MAXIMUM_HL7_MESSAGE_SIZE_IN_LOG);
            if(StringUtils.isNotEmpty(messageMaximumSize)){
                Integer messageMaxSize = Integer.getInteger(messageMaximumSize);
                if(messageMaxSize != null){
                    setMaxHL7MessageSize(messageMaxSize);
                }
            }
            getLogger().info(".initialise(): [Check Size Of HL7 Message to be included in Log] MaximumSize->{}", getMaxHL7MessageSize());
            getLogger().info(".initialise(): [Check Size Of HL7 Message to be included in Log] Finish");
            getLogger().info(".initialise(): Finish");
            this.initialised = true;
        }
        getLogger().debug(".initialise(): Exit");
    }


    //
    // Getters (and Setters)
    //

    protected HL7V2XTopicFactory getTopicFactory(){
        return(topicFactory);
    }

    protected DateTimeFormatter getTimeFormatter(){
        return(timeFormatter);
    }

    protected Logger getLogger(){
        return(LOG);
    }

    protected ProcessingPlantInterface getProcessingPlant(){
        return(processingPlant);
    }

    protected boolean isIncludeFullHL7MessageInLog() {
        return includeFullHL7MessageInLog;
    }

    protected void setIncludeFullHL7MessageInLog(boolean includeFullHL7MessageInLog) {
        this.includeFullHL7MessageInLog = includeFullHL7MessageInLog;
    }

    protected Integer getMaxHL7MessageSize() {
        return maxHL7MessageSize;
    }

    protected void setMaxHL7MessageSize(Integer maxHL7MessageSize) {
        this.maxHL7MessageSize = maxHL7MessageSize;
    }

    //
    // Business Functions
    //


    public UoW encapsulateTriggerEvent(MLLPMessageActivityParcel incomingMessageActivity, Exchange exchange) {

        LOG.debug(".encapsulateTriggerEvent(): Entry, incomingMessageActivity->{}", incomingMessageActivity);

        if(incomingMessageActivity.getUow().getProcessingOutcome().equals(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS)) {

            try {

                //
                // Now actually process the UoW/Message
                UoWProcessingOutcomeEnum outcomeEnum;
                String outcomeDescription;

                //
                // Only use the first version subfield.
                String messageVersionFirstField = incomingMessageActivity.getMllpVersionId();
                int indexOfFieldSeperator = messageVersionFirstField.indexOf("^");

                if (indexOfFieldSeperator != -1) {
                    messageVersionFirstField = messageVersionFirstField.substring(0, indexOfFieldSeperator);
                }

                outcomeEnum = UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS;
                outcomeDescription = "All Good!";

                //
                // Create the DataParcelManifest for the incomingMessageActivity
                LOG.trace(".encapsulateTriggerEvent(): Creating Data Parcel Descriptor (messageDescriptor)");
                DataParcelTypeDescriptor messageDescriptor = createDataParcelTypeDescriptor(incomingMessageActivity.getMllpEventType(), incomingMessageActivity.getMllpTriggerEvent(), messageVersionFirstField);
                if (StringUtils.isNotEmpty(incomingMessageActivity.getUow().getIngresContent().getPayloadManifest().getContentDescriptor().getDataParcelDiscriminatorType())) {
                    messageDescriptor.setDataParcelDiscriminatorType(incomingMessageActivity.getUow().getIngresContent().getPayloadManifest().getContentDescriptor().getDataParcelDiscriminatorType());
                }
                if (StringUtils.isNotEmpty(incomingMessageActivity.getUow().getIngresContent().getPayloadManifest().getContentDescriptor().getDataParcelDiscriminatorValue())) {
                    messageDescriptor.setDataParcelDiscriminatorValue(incomingMessageActivity.getUow().getIngresContent().getPayloadManifest().getContentDescriptor().getDataParcelDiscriminatorValue());
                }
                LOG.trace(".encapsulateTriggerEvent(): messageDescriptor created->{}", messageDescriptor);
                LOG.trace(".encapsulateTriggerEvent(): Creating Data Parcel Manifest (messageManifest)");
                DataParcelManifest messageManifest = new DataParcelManifest();
                messageManifest.setContentDescriptor(messageDescriptor);
                if (StringUtils.isNotEmpty(incomingMessageActivity.getUow().getIngresContent().getPayloadManifest().getSourceSystem())) {
                    if (incomingMessageActivity.getUow().getIngresContent().getPayloadManifest().getSourceSystem().contentEquals(DataParcelManifest.WILDCARD_CHARACTER)) {
                        messageManifest.setSourceSystem(null);
                    } else {
                        messageManifest.setSourceSystem(incomingMessageActivity.getUow().getIngresContent().getPayloadManifest().getSourceSystem());
                    }
                }
                messageManifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_FALSE);
                messageManifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATED_TRUE);
                messageManifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INFORMATION_FLOW_INBOUND_DATA_PARCEL);
                messageManifest.setSourceProcessingPlantParticipantName(participantNameHolder.getSubsystemParticipantName());
                LOG.trace(".encapsulateTriggerEvent(): messageManifest created->{}", messageManifest);

                //
                // Populate the UoWPayload
                LOG.trace(".encapsulateTriggerEvent(): Creating Egress Payload (newPayload)");
                UoWPayload newPayload = new UoWPayload();
                newPayload.setPayload(incomingMessageActivity.getUow().getIngresContent().getPayload());
                newPayload.setPayloadManifest(messageManifest);
                LOG.trace(".encapsulateTriggerEvent(): newPayload created->{}", newPayload);

                //
                // Create the UoW
                LOG.trace(".encapsulateTriggerEvent(): creating a new Unit of Work (newUoW)");
                UoW newUoW = new UoW(incomingMessageActivity.getUow());
                newUoW.getEgressContent().addPayloadElement(newPayload);
                newUoW.setProcessingOutcome(outcomeEnum);
                newUoW.setFailureDescription(outcomeDescription);

                //
                // All Done!
                LOG.debug(".encapsulateTriggerEvent(): Exit, newUoW created ->{}", newUoW);
                return (newUoW);
            } catch (Exception ex) {
                LOG.warn(".encapsulateTriggerEvent(): Exception occurred", ex);
                UoWPayload newPayload = new UoWPayload();
                if (incomingMessageActivity != null) {
                    newPayload.setPayload(incomingMessageActivity.toString());
                    newPayload.setPayloadManifest(topicFactory.newBadDataParcelManifest());
                } else {
                    newPayload.setPayload("Unable to decipher input incomingMessageActivity");
                    newPayload.setPayloadManifest(topicFactory.newBadDataParcelManifest());
                }
                UoW newUoW = new UoW(incomingMessageActivity.getUow());
                newUoW.setIngresContent(newPayload);
                newUoW.setFailureDescription(ex.toString());
                newUoW.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED);
                LOG.debug(".encapsulateTriggerEvent(): Exit, newUoW created ->{}", newUoW);
                return (newUoW);
            }
        } else {
            LOG.debug(".encapsulateTriggerEvent(): Exit, UoW is a Failure ->{}", incomingMessageActivity.getUow());
            return(incomingMessageActivity.getUow());
        }
    }



    //
    // Getters (and Setters)
    //

    protected ProcessingPlantMetricsAgent getProcessingPlantMetricsAgent(){
        return(processingPlantMetricsAgentAccessor.getMetricsAgent());
    }


    public boolean triggerIsSupported(String trigger) {
        return true;
    }


    public DataParcelTypeDescriptor createDataParcelTypeDescriptor(String messageEventType, String messageTriggerEvent, String version) {
        DataParcelTypeDescriptor descriptor = getTopicFactory().newDataParcelDescriptor(messageEventType, messageTriggerEvent, version);
        return (descriptor);
    }
}
