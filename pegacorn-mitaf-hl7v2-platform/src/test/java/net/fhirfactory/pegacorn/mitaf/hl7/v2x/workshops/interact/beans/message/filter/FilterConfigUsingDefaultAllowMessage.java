package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter.annotation.FilterMessage;


@FilterMessage(messageType = "ADT_A01") // filterConditionClass not specified so use the default allow message
public class FilterConfigUsingDefaultAllowMessage extends Filter {
	private static final Logger LOG = LoggerFactory.getLogger(FilterConfigUsingDefaultAllowMessage.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
