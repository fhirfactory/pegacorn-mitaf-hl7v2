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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans;

import net.fhirfactory.pegacorn.petasos.model.uow.UoW;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;

@Dependent
public class HL7v2MessageExtractor {
	private static final Logger LOG = LoggerFactory.getLogger(HL7v2MessageExtractor.class);

	public String convertToMessage(UoW incomingUoW, Exchange camelExchange) {
		LOG.debug(".convertToMessage(): Entry, incomingUoW->{}", incomingUoW);
		String messageAsString = incomingUoW.getIngresContent().getPayload();
		//
		// Because auditing is not running yet
		// Remove once Auditing is in place
		//
		//getLogger().info("IncomingMessage-----------------------------------------------------------------");
		LOG.warn("OutgoingMessage->{}", messageAsString); // Log at WARN level so always seen in TEST
		//getLogger().info("IncomingMessage-----------------------------------------------------------------");
		//
		//
		//
		LOG.debug(".convertToMessage(): Entry, messageAsString->{}", messageAsString);
		return (messageAsString);
	}
}
