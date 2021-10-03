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
package net.fhirfactory.pegacorn.mitaf.hl7.v24.interact.beans;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.v24.message.QRY_A19;
import ca.uhn.hl7v2.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HL7v24A19QueryMessageEncapsulator {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v24A19QueryMessageEncapsulator.class);

    private HapiContext hapiContext;

    @PostConstruct
    public void initialise(){
        hapiContext = new DefaultHapiContext();
    }

    public String buildA19Request(String requestMsg) {
        LOG.info(".buildA19Request(): Entry Query Received --> {}", requestMsg);

        Parser parser = getHAPIContext().getPipeParser();
        parser.getParserConfiguration().setValidating(false);
        parser.getParserConfiguration().setEncodeEmptyMandatoryFirstSegments(true);

        QRY_A19 query = null;
        String queryString = new String();
        try {
            query = (QRY_A19) parser.parse(requestMsg);
            queryString = query.printStructure();
            LOG.info(".buildA19Request(): query --> {}", queryString);
            String encodedString = query.encode();
            return (encodedString);
        } catch (Exception ex) {
            LOG.info(".buildA19Request(): Something went wrong with parsing --> {}", ex);
        }
        return (null);
    }

    //
    // Getters (and Setters)
    //

    public HapiContext getHAPIContext() {
        return hapiContext;
    }
}
