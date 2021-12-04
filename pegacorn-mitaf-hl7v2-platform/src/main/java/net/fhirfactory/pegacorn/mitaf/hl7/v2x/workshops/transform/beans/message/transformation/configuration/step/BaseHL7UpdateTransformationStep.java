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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step;

import org.slf4j.Logger;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.TrueRule;

/**
 * Base class for all HL7 update transformation steps.
 * 
 * Each step needs to update 1 segment only.  This is not enforced in the code but please
 * make sure it does.
 * 
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL7UpdateTransformationStep extends BaseMitafMessageTransformStep {

	public abstract Logger getLogger();
	
	public BaseHL7UpdateTransformationStep() {
		super(new TrueRule());
	}
	
	public BaseHL7UpdateTransformationStep(Rule rule) {
		super(rule);
	}

	
	@Override
	public void process(Message message) throws HL7Exception {

		if (rule.executeRule(message)) {			
			doUpdate(message);
		}
	}
	
	
	/**
	 * Update the segment.
	 * 
	 * @param segment
	 * @param fieldValueRetriever
	 */
	protected abstract void doUpdate(Message message) throws HL7Exception;
}
