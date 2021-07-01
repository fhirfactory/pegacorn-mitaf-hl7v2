package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.fhir2hl7;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.hl7.BaseHL7SegmentCreator;
import org.hl7.fhir.r4.model.Bundle;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.idgenerator.NanoTimeGenerator;

public class CreateNK1Segment extends BaseHL7SegmentCreator {

	@Override
	public Segment create(Bundle source) throws HL7Exception {
		
		//TODO change this to read from a bundle but this is good enough for now as the test just is to make sure a hl7 message can be constructed from individual segments using the config.
		String hl7 = null;

		try {
			hl7 = Files.readString(Paths.get("src/test/resources/hl7/ADT_A01.txt"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		HapiContext context = new DefaultHapiContext();
		Parser parser = context.getPipeParser();
		parser.getParserConfiguration().setValidating(false);
		parser.getParserConfiguration().setEncodeEmptyMandatoryFirstSegments(true);
		NanoTimeGenerator timeBasedIdGenerator = new NanoTimeGenerator();
		parser.getParserConfiguration().setIdGenerator(timeBasedIdGenerator);

		Message message = parser.parse(hl7);

		return (Segment) message.get("NK1");
	}
}
