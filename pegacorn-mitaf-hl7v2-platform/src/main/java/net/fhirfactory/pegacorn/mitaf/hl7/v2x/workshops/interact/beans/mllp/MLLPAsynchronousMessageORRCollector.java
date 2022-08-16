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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.mllp;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.ORR_O02;
import ca.uhn.hl7v2.model.v24.segment.MSA;
import java.util.ArrayList;
import java.util.List;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.caches.ProcessingPlantAsynchronousCacheDM;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.HL7v2MessageAsTextToHL7V2xMessage;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import net.fhirfactory.pegacorn.internals.hl7v2.interfaces.HL7v2xInformationExtractionInterface;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.HL7MessageUtils;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model.Field;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model.HL7Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model.MSHSegment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model.PIDSegment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model.Segment;

@ApplicationScoped
public class MLLPAsynchronousMessageORRCollector {

    private static final Logger LOG = LoggerFactory.getLogger(MLLPAsynchronousMessageORRCollector.class);

    @Inject
    private ProcessingPlantAsynchronousCacheDM asynchronousACKCacheDM;

    @Inject
    private HL7v2MessageAsTextToHL7V2xMessage hL7v2MessageAsTextToHL7V2xMessage;

    @Inject
    private HL7v2xInformationExtractionInterface informationExtractionInterface;

    public UoW extractAndSaveACKMessage(UoW incomingUoW, Exchange camelExchange) throws HL7Exception {
        LOG.warn(".extractAndSaveACKMessage(): Entry, incomingUoW->{}", incomingUoW);
        String messageAsString = incomingUoW.getIngresContent().getPayload();
        Message message = hL7v2MessageAsTextToHL7V2xMessage.convertToMessage(incomingUoW, camelExchange);
        ORR_O02 ackMessage = (ORR_O02) message;
        MSA msa = ackMessage.getMSA();

        String messageControlId = msa.getMessageControlID().getValueOrEmpty();
        asynchronousACKCacheDM.addAckMessage(messageControlId + "-ACK", messageAsString);
        LOG.warn("Add ACK message to asynchronous ACK cache: messageControlId->{}, ackMessage->{}", messageControlId, messageAsString);

        return incomingUoW;
    }

    public Message extractAndTransformMessage(UoW incomingUoW, Exchange camelExchange) throws HL7Exception, Exception {
        Message message = hL7v2MessageAsTextToHL7V2xMessage.convertToMessage(incomingUoW, camelExchange);
        ORR_O02 ackMessage = (ORR_O02) message;
        MSA msa = ackMessage.getMSA();

        String messageControlId = msa.getMessageControlID().getValueOrEmpty();
        String outgoingMessage = asynchronousACKCacheDM.getAckMessage(messageControlId + "-MSG");
        LOG.warn("Get outgoing message from asynchronous ACK cache: messageControlId->{}, Message->{}", messageControlId, outgoingMessage);

        HL7Message orrMessage = HL7MessageUtils.getHL7Message(message);
        HL7Message oruMessage = HL7MessageUtils.getHL7Message(informationExtractionInterface.convertToHL7v2Message(outgoingMessage));

        MSHSegment orrMsh = orrMessage.getMSHSegment();
        orrMsh.getField(9).setValue("ORU^R01");

        Segment MSA = orrMessage.getSegment("MSA");
        Field msa_3 = MSA.getField(3);

        PIDSegment oruPid = oruMessage.getPIDSegment();
        Segment ORC = oruMessage.getSegment("ORC");
        String orc_2_1 = ORC.getSubField(2, 1).toString();

        HL7Message transformedOruMessage = new HL7Message(orrMessage.getSourceMessage());
        transformedOruMessage.removeAllSegments("MSA");
        List segments = new ArrayList();

        // TODO - Attempt to extract transform method to freemarker to make dynamic.
        String orcString = "ORC|RE|" + orc_2_1 + "^NATA2508^2508^AUSNATA||1";
        Segment orc = new Segment(orcString, transformedOruMessage);

        String obrString = "OBR|1|" + orc_2_1 + "^NATA2508^2508^AUSNATA||5491000179105^^SCT|||||||||" + msa_3;
        Segment obr = new Segment(obrString, transformedOruMessage);

        String obxString = "OBX|1|FT|PATHHIST^^NCSRLRR||" + msa_3;
        Segment obx = new Segment(obxString, transformedOruMessage);

        segments.add(orrMsh);
        segments.add(oruPid);
        segments.add(orc);
        segments.add(obr);
        segments.add(obx);
        transformedOruMessage.setSegments(segments);

        return transformedOruMessage.getSourceMessage();
    }
}
