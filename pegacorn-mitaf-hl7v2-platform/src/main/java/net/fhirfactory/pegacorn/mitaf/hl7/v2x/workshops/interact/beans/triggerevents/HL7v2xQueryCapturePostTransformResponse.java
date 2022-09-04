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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents;

import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelDirectionEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelNormalisationStatusEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelValidationStatusEnum;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.common.HL7v2xMessageEncapsulatorBase;
import net.fhirfactory.pegacorn.petasos.core.tasks.accessors.PetasosFulfillmentTaskSharedInstance;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;

@Dependent
public class HL7v2xQueryCapturePostTransformResponse extends HL7v2xMessageEncapsulatorBase {

    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xQueryCapturePostTransformResponse.class);


    //
    // Constructor(s)
    //
    public HL7v2xQueryCapturePostTransformResponse() {
    }

    //
    // Getters (and Setters)
    //

    @Override
    protected Logger getLogger() {
        return (LOG);
    }

    //
    // Business Methods
    //

    public UoW capturePostTransformMessage(String message, Exchange camelExchange){
        getLogger().debug(".registerQueryActivityStart(): Entry, message --> {}", message);
        //
        // Now we have to Inject some details into the Exchange so that the WUPEgressConduit can extract them as per standard practice
        getLogger().trace(".registerQueryActivityFinish(): [Extract fulfillmentTask from Camel Exchange] Start");
        PetasosFulfillmentTaskSharedInstance fulfillmentTaskInstance = camelExchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY, PetasosFulfillmentTaskSharedInstance.class);
        getLogger().trace(".registerQueryActivityFinish(): [Extract fulfillmentTask from Camel Exchange] Finish");

        getLogger().debug(".registerQueryActivityFinish(): [Extract Full Message Trigger] Start");
        String messageTriggerEvent = getDefensivePipeParser().getTriggerEvent(message);
        getLogger().trace(".registerQueryActivityFinish(): [Extract Full Message Trigger] messageTriggerEvent --> {}", messageTriggerEvent);
        getLogger().debug(".registerQueryActivityFinish(): [Extract Full Message Trigger] Finish");

        getLogger().debug(".registerQueryActivityFinish(): [Extract Full Message Type] Start");
        String messageEventType = getDefensivePipeParser().getMessageCode(message);
        getLogger().trace(".registerQueryActivityFinish(): [Extract Full Message Type] messageEventType --> {}", messageEventType);
        getLogger().debug(".registerQueryActivityFinish(): [Extract Full Message Type] Finish");

        getLogger().debug(".registerQueryActivityFinish(): [Extract Full Message Version] Start");
        String messageVersion = getDefensivePipeParser().getMessageVersion(message);
        getLogger().trace(".registerQueryActivityFinish(): [Extract Full Message Version] MessageVersion --> {}", messageVersion);
        getLogger().debug(".registerQueryActivityFinish(): [Extract Full Message Version] Finish");

        getLogger().debug(".registerQueryActivityFinish(): [Extract Response Version (1st Field)] Start");
        String messageVersionFirstField = messageVersion;
        int indexOfFieldSeperator = messageVersion.indexOf("^");

        if (indexOfFieldSeperator != -1) {
            messageVersionFirstField = messageVersion.substring(0, indexOfFieldSeperator);
        }
        getLogger().trace(".registerQueryActivityFinish(): [Extract Response Version (1st Field)] 1stField->{}", messageVersionFirstField);
        getLogger().debug(".registerQueryActivityFinish(): [Extract Response Version (1st Field)] Finish");

        getLogger().debug(".registerQueryActivityFinish(): [Create Data Parcel Descriptor] Start");
        DataParcelTypeDescriptor messageDescriptor = createDataParcelTypeDescriptor(messageEventType, messageTriggerEvent, messageVersionFirstField );
        getLogger().trace(".registerQueryActivityFinish(): [Create Data Parcel Descriptor] messageDescriptor->{}", messageDescriptor);
        getLogger().debug(".registerQueryActivityFinish(): [Create Data Parcel Descriptor] Finish");

        getLogger().debug(".registerQueryActivityFinish(): [Create Data Parcel Manifest] Start");
        DataParcelManifest messageManifest = new DataParcelManifest();
        messageManifest.setContentDescriptor(messageDescriptor);
        messageManifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_FALSE);
        messageManifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATED_TRUE);
        messageManifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INFORMATION_FLOW_INBOUND_DATA_PARCEL);
        getLogger().trace(".registerQueryActivityFinish(): [Create Data Parcel Manifest] messageManifest->{}", messageManifest);
        getLogger().debug(".registerQueryActivityFinish(): [Create Data Parcel Manifest] Finish");

        getLogger().debug(".registerQueryActivityFinish(): [Create Egress Payload Instant] Start");
        UoWPayload newPayload = new UoWPayload();
        newPayload.setPayload(message);
        newPayload.setPayloadManifest(messageManifest);
        getLogger().trace(".registerQueryActivityFinish(): [Create Egress Payload Instant] newPayload->{}", newPayload);
        getLogger().debug(".registerQueryActivityFinish(): [Create Egress Payload Instant] Finish");

        getLogger().debug(".registerQueryActivityFinish(): [Add Payload to Egress Work Items] Start");
        UoW uow = SerializationUtils.clone(fulfillmentTaskInstance.getTaskWorkItem());
        uow.getEgressContent().addPayloadElement(newPayload);
        uow.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);
        getLogger().debug(".registerQueryActivityFinish(): [Add Payload to Egress Work Items] Finish");

        getLogger().debug(".registerQueryActivityFinish(): Exit, uow->{}", uow);
        return(uow);
    }
}
