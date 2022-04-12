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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.validate.beans;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelExternallyDistributableStatusEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelValidationStatusEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.PolicyEnforcementPointApprovalStatusEnum;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HL7v2xValidationPoint {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xValidationPoint.class);

    protected Logger getLogger(){
        return(LOG);
    }

    ObjectMapper jsonMapper;

    public HL7v2xValidationPoint(){
        jsonMapper = new ObjectMapper();
    }

    public UoW validateOutboundMessage(UoW uow, Exchange camelExchange){
        getLogger().debug(".validateOutboundMessage(): Entry, uow->{}", uow);
        if(uow == null){
            return(null);
        }
        if(!uow.hasIngresContent()){
            return(uow);
        }

        UoWPayload validatedPayload = SerializationUtils.clone(uow.getIngresContent());
        validatedPayload.getPayloadManifest().setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATED_TRUE);
        validatedPayload.getPayloadManifest().setExternallyDistributable(DataParcelExternallyDistributableStatusEnum.DATA_PARCEL_EXTERNALLY_DISTRIBUTABLE_TRUE);
        uow.getEgressContent().addPayloadElement(validatedPayload);
        uow.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);

        getLogger().debug(".validateOutboundMessage(): Exit, uow->{}", uow);
        return(uow);
    }
}
