package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.hl7;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration.BaseConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration.BaseHL72FHIRConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration.BaseHL7UpdateConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.fhir.BaseFHIRResourceCreator;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.fhir.MitafFhirMessage;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.MitafMessage;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * A wrapper around an HL7 message.
 * 
 * @author Brendan Douglas
 *
 */
public class MitafHL7Message extends MitafMessage {
	private static final Logger LOG = LoggerFactory.getLogger(MitafHL7Message.class);

	private Message theMessage;
	
	public MitafHL7Message(String theMessage) throws IOException, HL7Exception {
		try (HapiContext context = new DefaultHapiContext();) {
			PipeParser parser = context.getPipeParser();

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			this.theMessage = parser.parse(theMessage);
		} 
	}

	public MitafHL7Message(Message theMessage) {
		this.theMessage = theMessage;
	}

	@Override
	public Message getMessage() {
		return theMessage;
	}

	@Override
	public MitafFhirMessage convert(BaseConfiguration config) throws HL7Exception{
		List<Resource> resources = new ArrayList<>();
		
		BaseHL72FHIRConfiguration hl7Configuration = (BaseHL72FHIRConfiguration) config;
		
		for (BaseFHIRResourceCreator creator : hl7Configuration.getResourceCreators()) {
			resources.add(creator.create(this));
		}
		
		// Add resources to the bundle.
		// Add bundle to the message
		// Return the message.
		
		Bundle bundle = new Bundle();
		MitafFhirMessage message = new MitafFhirMessage(bundle);
		
		return message;
		
	}

	@Override
	public void update(BaseConfiguration config) throws HL7Exception {
		BaseHL7UpdateConfiguration hl7Configuration = (BaseHL7UpdateConfiguration) config;

		// Do the updates followed by any segment removals.
		
		for (BaseHL7SegmentUpdater updater : hl7Configuration.getSegmentUpdaters()) {
			updater.update(this);
		}
		
		for (BaseHL7SegmentRemover remover : hl7Configuration.getSegmentRemovers()) {
			remover.update(this);
		}
	}

	@Override
	protected Logger getLogger() {
		return LOG;
	}

	@Override
	public String toString() {
		return theMessage.toString();
	}
}
