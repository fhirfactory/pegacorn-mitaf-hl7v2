package net.fhirfactory.pegacorn.mitaf.hl7.v2x.transformation.hl7;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.transformation.hl7.configuration.ADTA01UpdateConfigurationEgres;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.hl7.MitafHL7Message;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.BaseHL7UpdateConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.ConfigurationType;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.ConfigurationUtil;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.DefaultHL7UpdateConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.Direction;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.v231.segment.PID;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * Tests to make sure the message transformation configuration can instantiate
 * the appropriate transformation class.
 * 
 * @author Brendan Douglas
 *
 */
public class MessageTransformationConfigurationTest {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransformationConfigurationTest.class);

	/**
	 * Makes sure a segment from the HL7 message can be removed.
	 */
	@Test
	public void testRemoveSegment() {
		try (HapiContext context = new DefaultHapiContext();) {
			PipeParser parser = context.getPipeParser();

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);

			String hl7 = Files.readString(Paths.get("src/test/resources/hl7/ADT_A01.txt"));

			MitafHL7Message message = new MitafHL7Message(parser.parse(hl7));

			PID pidSegment = (PID) message.getMessage().get("PID");
			assertEquals("ADAM", pidSegment.getPatientName()[0].getGivenName().getValue());

			BaseHL7UpdateConfiguration configuration = (BaseHL7UpdateConfiguration) ConfigurationUtil.getConfiguration(
			        ConfigurationType.UPDATE_HL7, "au.gov.act.hd.aether.mitaf.commoncode.transformation.hl7", Direction.EGRES,
			        message.getMessage().getName());

			assertTrue(configuration instanceof ADTA01UpdateConfigurationEgres);

			assertEquals(1, configuration.getSegmentUpdaters().size());
			assertEquals(1, configuration.getSegmentRemovers().size());

			LOG.info("HL7 before transformation: {}", message.getMessage().toString());

			message.update(configuration);

			LOG.info("HL7 after transformation: {}", message.getMessage().toString());

			assertEquals("Peter", pidSegment.getPatientName()[0].getGivenName().getValue());
		} catch (HL7Exception e) {
			fail("Unable to process HL7 message", e);
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		}
	}
	
	
	/**
	 * Test to make sure the default configuration is used if no configuration classs is found.
	 */
	@Test
	public void testDefaultConfiguration() {
		try (HapiContext context = new DefaultHapiContext();) {
			PipeParser parser = context.getPipeParser();

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);

			String hl7 = Files.readString(Paths.get("src/test/resources/hl7/VXU_V04.txt"));

			MitafHL7Message message = new MitafHL7Message(parser.parse(hl7));

			BaseHL7UpdateConfiguration configuration = (BaseHL7UpdateConfiguration) ConfigurationUtil.getConfiguration(
			        ConfigurationType.UPDATE_HL7, "au.gov.act.hd.aether.mitaf.commoncode.transformation.hl7", Direction.EGRES,
			        message.getMessage().getName());

			assertTrue(configuration instanceof DefaultHL7UpdateConfiguration);
		} catch (HL7Exception e) {
			fail("Unable to process HL7 message", e);
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		}		
	}
}
