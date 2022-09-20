/*
 * Copyright (c) 2022 ACT Health
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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans;

import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.dricats.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.dricats.interfaces.topology.ProcessingPlantInterface;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelNormalisationStatusEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelValidationStatusEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.PolicyEnforcementPointApprovalStatusEnum;
import net.fhirfactory.dricats.model.petasos.uow.UoW;
import net.fhirfactory.dricats.model.petasos.uow.UoWPayload;
import net.fhirfactory.dricats.model.petasos.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.exceptions.TransformationSoftFailureExceptionHandlerBase;
import net.fhirfactory.pegacorn.petasos.tasking.caches.local.PetasosFulfillmentTaskSharedInstance;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class HL7v2xInboundMessageTransformationExceptionHandler extends TransformationSoftFailureExceptionHandlerBase {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xInboundMessageTransformationExceptionHandler.class);

    private boolean allowingSoftFailures;
    private boolean initialised;

    private static final String ALLOW_SOFT_FAILURES_ON_TRANSFORMS = "ALLOW_SOFT_FAILURES_ON_TRANSFORMS";

    @Inject
    private ProcessingPlantInterface processingPlant;

    //
    // Constructor(s)
    //

    public HL7v2xInboundMessageTransformationExceptionHandler(){
        allowingSoftFailures = false;
        initialised = false;
    }

    //
    // Post Construct
    //

    @PostConstruct
    public void initialise(){
        getLogger().debug(".initialise(): Entry");
        if(initialised){
            // do nothing
            getLogger().debug(".initialise(): Nothing to do, already initialised!");
        } else {
            getLogger().info(".initialise(): Initialisation Start....");

            getLogger().info(".initialise(): [Check if Soft-Failure is Allowed] Start");
            String includeMessageString  = getProcessingPlant().getMeAsASoftwareComponent().getOtherConfigurationParameter(ALLOW_SOFT_FAILURES_ON_TRANSFORMS);
            if(StringUtils.isNotEmpty(includeMessageString)){
                if(includeMessageString.equalsIgnoreCase("true")){
                    setAllowingSoftFailures(true);
                }
            }
            getLogger().info(".initialise(): [Check if Soft-Failure is Allowed] Finish");

            getLogger().info(".initialise(): Initialisation Finish....");
        }
        getLogger().debug(".initialise(): Exit");
    }

    //
    // Business Methods
    //

    public UoW processException(Message message, Exchange camelExchange) {
        getLogger().info(".processException(): Entry, message->{}", message);

        //
        // 1st Check to see if the Exception has been handled
        Exception caughtException = camelExchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        getLogger().trace(".processException(): caughtException->{}", caughtException);
        String exceptionMessage = null;
        if(caughtException == null){
            getLogger().error(".processException(): Exception is null (already handled), returning null!");
            exceptionMessage = "unknown error";
        } else {
            exceptionMessage = caughtException.getMessage();
        }

        //
        // Extract the fulfillmentTask from the exchange
        PetasosFulfillmentTaskSharedInstance fulfillmentTask = camelExchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY, PetasosFulfillmentTaskSharedInstance.class);

        //
        // Log Exception
        sendExceptionNotification(exceptionMessage, camelExchange);

        //
        // Get the UoW from the fulfillmentTask (clone it...)

        UoW uow = SerializationUtils.clone(fulfillmentTask.getTaskWorkItem());
        //
        // Process the content (create "failed activity manifest for egress");
        DataParcelManifest exceptionManifest = new DataParcelManifest();
        DataParcelTypeDescriptor exceptionDescriptor = new DataParcelTypeDescriptor();
        exceptionDescriptor.setDataParcelDefiner("FHIRFactory");
        exceptionDescriptor.setDataParcelCategory("System");
        exceptionDescriptor.setDataParcelSubCategory("Exception");
        exceptionManifest.setContentDescriptor(exceptionDescriptor);
        exceptionManifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_TRUE);
        exceptionManifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATED_TRUE);
        exceptionManifest.setInterSubsystemDistributable(false);
        UoWPayload payload = new UoWPayload();
        payload.setPayloadManifest(exceptionManifest);
        String exceptionStackTrace = ExceptionUtils.getStackTrace(caughtException);
        String exceptionDescription = "Error: message-> "+ exceptionMessage + ", stack trace-> "+ exceptionStackTrace;
        payload.setPayload(exceptionDescription);
        uow.getEgressContent().addPayloadElement(payload);
        uow.setFailureDescription(exceptionDescription);

        if(isAllowingSoftFailures()){
            getLogger().debug(".processException(): Allowing for Soft-Errors, forwarding message");
            UoWPayload continuationMessage = SerializationUtils.clone(uow.getIngresContent());
            continuationMessage.getPayloadManifest().setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_TRUE);
            continuationMessage.getPayloadManifest().setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATED_TRUE);
            continuationMessage.getPayloadManifest().setEnforcementPointApprovalStatus(PolicyEnforcementPointApprovalStatusEnum.POLICY_ENFORCEMENT_POINT_APPROVAL_NEGATIVE);
            uow.getEgressContent().addPayloadElement(continuationMessage);
            uow.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SOFTFAILURE);
        } else {
            uow.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_FAILED);
        }

        getLogger().info(".processException(): Exit, uow->{}", uow);
        return(uow);

    }

    //
    // Getters and Setters
    //

    @Override
    protected Logger getLogger(){
        return(LOG);
    }

    protected boolean isAllowingSoftFailures() {
        return allowingSoftFailures;
    }

    protected void setAllowingSoftFailures(boolean allowingSoftFailures) {
        this.allowingSoftFailures = allowingSoftFailures;
    }

    protected ProcessingPlantInterface getProcessingPlant(){
        return(processingPlant);
    }
}
