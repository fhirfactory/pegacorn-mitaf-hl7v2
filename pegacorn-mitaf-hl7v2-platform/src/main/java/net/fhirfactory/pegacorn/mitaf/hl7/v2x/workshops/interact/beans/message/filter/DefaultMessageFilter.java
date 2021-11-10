package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default message filter.  If a mitaf instance does not provide an implementation of {@link Filter}
 * then this is used.  Currently does no filtering.
 * 
 * @author Brendan Douglas
 *
 */
@ApplicationScoped
public class DefaultMessageFilter extends Filter {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMessageFilter.class);

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
