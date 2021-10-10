package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter.annotation.FilterMessage;


@FilterMessage(messageType = "ADT_*", filterConditionClass = PIDSegmentExistsFilterCondition.class)
@FilterMessage(messageType = "MDM_T02", filterConditionClass = EVNSegmentExistsFilterCondition.class)
public class SegmentFilterConfig extends Filter {
	private static final Logger LOG = LoggerFactory.getLogger(SegmentFilterConfig.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
