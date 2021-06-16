package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.hl7;

import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.rule.Rule;
import org.slf4j.Logger;

import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.BaseDataRetriever;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.BaseMitafMessageUpdater;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.MitafMessage;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractGroup;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.DataTypeException;

/**
 * Base class for all segment field update classes. This is intended for only
 * basic updates.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL7SegmentUpdater extends BaseMitafMessageUpdater {
	protected String segmentCode;

	public abstract Logger getLogger();
	
	public BaseHL7SegmentUpdater(Rule rule, String segmentCode, BaseDataRetriever<MitafHL7Message> fieldValueRetriever) {
		super(rule);

		this.fieldValueRetriever = fieldValueRetriever;
		this.segmentCode = segmentCode;
	}

	protected AbstractSegment getSegment(MitafMessage message) throws HL7Exception {
		AbstractGroup group = (AbstractGroup) message.getMessage();

		return (AbstractSegment) group.get(segmentCode);
	}

	/**
	 * Do the transformation.
	 * 
	 * @param message
	 */
	public void doTransformation(MitafHL7Message message) throws HL7Exception {
		if (rule.executeRule(message)) {
			update(message);
		} else {
			getLogger().info("Transformation will not be applied");
		}
	}

	protected BaseDataRetriever<MitafHL7Message> fieldValueRetriever;


	@Override
	public MitafMessage update(MitafMessage message) throws HL7Exception {
		AbstractSegment segment = getSegment(message);

		doUpdate(segment, fieldValueRetriever);

		return message;
	}

	/**
	 * Update the segment.
	 * 
	 * @param segment
	 * @param fieldValueRetriever
	 */
	protected abstract void doUpdate(AbstractSegment segment, BaseDataRetriever<MitafHL7Message> fieldValueRetriever) throws DataTypeException;
}
