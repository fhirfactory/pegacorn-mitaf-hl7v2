package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transformation.hl7;

import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.hl7.BaseHL7SegmentRemover;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.rule.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveEventSegment extends BaseHL7SegmentRemover {
	private static final Logger LOG = LoggerFactory.getLogger(RemoveEventSegment.class);

	public RemoveEventSegment(Rule rule) {
		super(rule, "EVN");
	}

	@Override
	public Logger getLogger() {
		return LOG;
	}
}
