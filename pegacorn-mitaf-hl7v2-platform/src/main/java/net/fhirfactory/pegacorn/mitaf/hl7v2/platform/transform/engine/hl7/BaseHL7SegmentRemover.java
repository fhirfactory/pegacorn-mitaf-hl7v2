package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.hl7;

import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.BaseMitafMessageUpdater;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.MitafMessage;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.rule.Rule;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractGroup;

/**
 * Base class for all segment removal classes.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL7SegmentRemover extends BaseMitafMessageUpdater {
	protected String segmentCode;

	public BaseHL7SegmentRemover(Rule rule, String segmentCode) {
		super(rule);
		
		this.segmentCode = segmentCode;
	}

	@Override
	public MitafMessage update(MitafMessage message) throws HL7Exception {
		AbstractGroup group = (AbstractGroup) message.getMessage();
		group.removeRepetition(segmentCode, 0);

		return message;
	}
}
