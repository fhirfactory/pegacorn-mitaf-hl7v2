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

import ca.uhn.fhir.parser.IParser;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.util.FHIRContextUtility;
import org.apache.camel.Exchange;
import org.hl7.fhir.r4.model.Communication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class UoWToFHIRCommunication {
    private static final Logger LOG = LoggerFactory.getLogger(UoWToFHIRCommunication.class);

    private IParser fhirParser;

    @PostConstruct
    public void initialise(){
        fhirParser = fhirContextUtility.getJsonParser().setPrettyPrint(true);
    }

    @Inject
    private FHIRContextUtility fhirContextUtility;

    public Communication extractCommunicationResource(UoW uow, Exchange camelExchange){
        LOG.debug(".extractCommunicationResource(): Entry, uow->{}", uow);
        LOG.trace(".extractCommunicationResource(): Extracting payload from uow (UoW)");
        String communicationAsString = uow.getIngresContent().getPayload();
        LOG.trace(".packageCommunicationResource(): Converting into (FHIR::Communication) from JSON String");
        Communication communication = (Communication)fhirParser.parseResource(communicationAsString);
        LOG.debug(".packageCommunicationResource(): Exit, communication->{}", communication);
        return(communication);
    }
}
