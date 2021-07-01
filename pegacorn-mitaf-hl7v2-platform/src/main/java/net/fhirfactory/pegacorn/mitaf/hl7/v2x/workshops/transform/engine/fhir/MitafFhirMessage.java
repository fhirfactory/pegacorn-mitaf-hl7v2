package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.fhir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.MitafMessage;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.BaseConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.BaseFHIR2HL7Configuration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.BaseFHIRUpdateConfiguration;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.BaseMitafMessageUpdater;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.hl7.BaseHL7SegmentCreator;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.hl7.MitafHL7Message;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.parser.Parser;

/**
 * A wrapper around an FHIR message.
 * 
 * @author Brendan Douglas
 *
 */
public class MitafFhirMessage extends MitafMessage {
	private static final Logger LOG = LoggerFactory.getLogger(MitafFhirMessage.class);

	private Bundle bundle;

	public MitafFhirMessage(Bundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public Bundle getMessage() {
		return bundle;
	}

	@Override
	public MitafHL7Message convert(BaseConfiguration config) throws HL7Exception {
		BaseFHIR2HL7Configuration hl7Configuration = (BaseFHIR2HL7Configuration) config;

		List<String> segments = new ArrayList<>();

		// Create segments based on the configuration and convert to a string
		for (BaseHL7SegmentCreator segmentCreator : hl7Configuration.getSegmentCreators()) {
			segments.add(segmentCreator.create(bundle).encode());
		}

		try (HapiContext context = new DefaultHapiContext()) {
			Parser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);
			parser.getParserConfiguration().setEncodeEmptyMandatoryFirstSegments(true);

			return new MitafHL7Message(parser.parse(String.join(System.lineSeparator(), segments)));
		} catch (IOException e) {
			throw new RuntimeException("Unable to convert FHIR bundle to a HL7 message", e);
		}
	}

	@Override
	public void update(BaseConfiguration config) throws HL7Exception {
		BaseFHIRUpdateConfiguration fhirConfiguration = (BaseFHIRUpdateConfiguration) config;

		for (BaseMitafMessageUpdater updater : fhirConfiguration.getResourceUpdaters()) {
			updater.update(this);
		}
	}

	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
