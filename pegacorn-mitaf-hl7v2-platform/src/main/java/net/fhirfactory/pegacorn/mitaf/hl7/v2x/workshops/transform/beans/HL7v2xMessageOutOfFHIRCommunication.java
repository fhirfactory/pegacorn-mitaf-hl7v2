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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.hl7.fhir.r4.model.Communication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.hl7v2.HL7Exception;
import net.fhirfactory.pegacorn.components.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.components.dataparcel.valuesets.DataParcelNormalisationStatusEnum;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.BaseMessageTransform;
import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWPayload;
import net.fhirfactory.pegacorn.petasos.model.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.util.FHIRContextUtility;

@ApplicationScoped
public class HL7v2xMessageOutOfFHIRCommunication {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xMessageOutOfFHIRCommunication.class);

    private IParser fhirResourceParser;

    protected Logger getLogger(){return(LOG);}

    @Inject
    protected FHIRContextUtility fhirContextUtility;
    
    @Inject
    protected BaseMessageTransform messageTransform;

    @PostConstruct
    public void initialise(){
        fhirResourceParser = fhirContextUtility.getJsonParser().setPrettyPrint(true);
    }

    public UoW extractMessage(UoW uow) throws IOException, HL7Exception {
        getLogger().debug(".extractMessage(): Entry, uow->{}", uow);

        getLogger().trace(".extractMessage(): Extracting payload from uow (UoW)");
        String communicationAsString = uow.getIngresContent().getPayload();

        getLogger().trace(".extractMessage(): Converting into (FHIR::Communication) from JSON String");
        Communication communication = fhirResourceParser.parseResource(Communication.class, communicationAsString);

        getLogger().trace(".extractMessage(): Pull the HL7v2x Message (as Text) from the Communication Payload");
        Communication.CommunicationPayloadComponent communicationPayload = communication.getPayloadFirstRep();
        String contentMessage = communicationPayload.getContentStringType().getValue();
                
        getLogger().trace(".extractMessage(): Clone the content for injection into the UoW egress payload");
        String clonedMessage = SerializationUtils.clone(contentMessage);

        getLogger().trace(".extractMessage(): Create the egress payload (UoWPayload) to contain the message");
        UoWPayload newPayload = new UoWPayload();

        getLogger().trace(".extractMessage(): Clone the manifest (DataParcelManifest) of the incoming payload");
        DataParcelManifest newManifest = SerializationUtils.clone(uow.getIngresContent().getPayloadManifest());

        getLogger().trace(".extractMessage(): Now, set the containerDescriptor to null, as we've removed payload from the Communication resource");
        newManifest.setContainerDescriptor(null);
        newManifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_TRUE);

        getLogger().trace(".extractMessage(): Populate the new Egress payload object");
        newPayload.setPayload(clonedMessage);
        newPayload.setPayloadManifest(newManifest);

        getLogger().trace(".extractMessage(): Add the new Egress payload to the UoW");
        uow.getEgressContent().addPayloadElement(newPayload);

        getLogger().trace(".extractMessage(): Assign the processing outcome to the UoW");
        uow.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);

        getLogger().debug(".extractMessage(): Exit, uow->{}", uow);
        return (uow);
    }    
}