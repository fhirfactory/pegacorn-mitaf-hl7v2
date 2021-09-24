package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step.BaseHL7RemoveSegmentTransformationStep;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveEventSegment extends BaseHL7RemoveSegmentTransformationStep {
	private static final Logger LOG = LoggerFactory.getLogger(RemoveEventSegment.class);

	public RemoveEventSegment(Rule rule) {
		super(rule, "EVN");
	}

	@Override
	public Logger getLogger() {
		return LOG;
	}
}
