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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.mllp;

import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;

@Dependent
public class MLLPAckSpecification {
	private static final Logger LOG = LoggerFactory.getLogger(MLLPAckSpecification.class);

	//
	// Constructor(s)
	//

	public MLLPAckSpecification(){
	}

	//
	// Getters (and Setters)
	//

    protected Logger getLogger(){
        return(LOG);
    }

    //
	// Business Methods
	//

	public UoW setMLLPResponseCode(UoW incomingUoW, Exchange camelExchange) {
		getLogger().debug(".setMLLPResponseCode(): Entry, incomingUoW->{}", incomingUoW);

		if(incomingUoW.hasProcessingOutcome()) {
			if (incomingUoW.getProcessingOutcome().equals(UoWProcessingOutcomeEnum.UOW_OUTCOME_CANCELLED)) {
				// The AR Code will send an Application Reject response
				// see https://camel.apache.org/components/3.18.x/mllp-component.html
				camelExchange.setProperty("CamelMllpAcknowledgementType", "AR");
				camelExchange.setProperty("CamelMllpAcknowledgementMsaText", "System is in SUSPEND Model");
			}
		}
		getLogger().debug(".setMLLPResponseCode(): Exit, incomingUoW->{}", incomingUoW);
		return (incomingUoW);
	}
}
