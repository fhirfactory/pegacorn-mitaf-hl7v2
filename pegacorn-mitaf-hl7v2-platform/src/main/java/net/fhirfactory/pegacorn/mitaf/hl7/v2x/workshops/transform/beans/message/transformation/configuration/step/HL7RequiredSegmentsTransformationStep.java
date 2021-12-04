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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.MessageUtils;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.TrueRule;

/**
 * A class which removes segments which are not one of the allowed segments.
 * 
 * @author Brendan Douglas
 *
 */
public class HL7RequiredSegmentsTransformationStep extends BaseMitafMessageTransformStep {
	private static final Logger LOG = LoggerFactory.getLogger(HL7RequiredSegmentsTransformationStep.class);
	
	protected List<String>allowedSegmentCodes;
	
	public HL7RequiredSegmentsTransformationStep(List<String> segmentCodes) {
		this(segmentCodes, new TrueRule());
	}

	public HL7RequiredSegmentsTransformationStep(List<String> allowedSegmentCodes, Rule rule) {
		super(rule);
		
		this.allowedSegmentCodes = allowedSegmentCodes;
	}

	@Override
	public void process(Message message) throws HL7Exception {

		if (rule.executeRule(message)) {
			MessageUtils.removeNotRequiredSegments(message, allowedSegmentCodes);
		}
	}

	
	@Override
	public Logger getLogger() {
		return LOG;
	}
}
