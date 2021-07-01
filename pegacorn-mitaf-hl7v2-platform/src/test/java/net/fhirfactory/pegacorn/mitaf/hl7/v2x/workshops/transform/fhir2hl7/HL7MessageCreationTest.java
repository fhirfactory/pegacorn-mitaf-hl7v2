package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.fhir2hl7;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.BaseFHIR2HL7Configuration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.ConfigurationType;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.ConfigurationUtil;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.Direction;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.fhir.MitafFhirMessage;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.hl7.MitafHL7Message;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;

public class HL7MessageCreationTest {
	private static final Logger LOG = LoggerFactory.getLogger(HL7MessageCreationTest.class);

	@Test
	public void testMessageCreation() {
		try {
			BaseFHIR2HL7Configuration configuration = (BaseFHIR2HL7Configuration) ConfigurationUtil.getConfiguration(
			        ConfigurationType.FHIR_TO_HL7, "au.gov.act.hd.aether.mitaf.commoncode.transform.fhir2hl7", Direction.EGRES, "ADT_A01");

			//TODO create a bundle.  Currently the test segment creators just return a hl7 segment without conversion from a bundle.
			MitafFhirMessage message = new MitafFhirMessage(new Bundle());

			MitafHL7Message hl7Message = message.convert(configuration);

			assertNotNull(hl7Message.getMessage().get("MSH"));
			assertNotNull(hl7Message.getMessage().get("EVN"));
			assertNotNull(hl7Message.getMessage().get("PID"));
			assertNotNull(hl7Message.getMessage().get("NK1"));
			assertNotNull(hl7Message.getMessage().get("PV1"));

			LOG.info("HL7 message: {}", hl7Message.toString());

		} catch (HL7Exception e) {
			fail("Unable to create HL7 message");
		}
	}
}
