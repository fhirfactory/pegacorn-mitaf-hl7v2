package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v231.message.ADT_A01;
import ca.uhn.hl7v2.model.v231.segment.NK1;
import ca.uhn.hl7v2.model.v231.segment.PID;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.ADTA01UpdateConfigurationEgres;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.BaseHL7MessageTransformationConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.ConfigurationUtil;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.DefaultHL7UpdateConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.Direction;

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
	public void testUpdateMessage() {
		try (HapiContext context = new DefaultHapiContext();) {
			String hl7 = Files.readString(Paths.get("src/test/resources/hl7/ADT_A01.txt"));
			hl7 = hl7.replaceAll("\n", "\r");


			BaseHL7MessageTransformationConfiguration configuration = (BaseHL7MessageTransformationConfiguration) ConfigurationUtil.getConfiguration("net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation", Direction.EGRES, "ADT_A01");
				
			HL7MessageTransformation transformation = new HL7MessageTransformation(hl7, configuration);

			assertTrue(configuration instanceof ADTA01UpdateConfigurationEgres);

			assertEquals(1, configuration.getSegmentsToBeUpdated().size());
			assertEquals(1, configuration.getSegmentsToBeRemoved().size());
			assertEquals(1, configuration.getSegmentsToBeAdded().size());
			
			LOG.info("HL7 before transformation: {}", hl7);
			
			Message message = transformation.transform();

			LOG.info("HL7 after transformation: {}", message);

			
			
			// Make sure the name has been updated
			
			PID pidSegment = ((ADT_A01) message).getPID();
			assertEquals("Peter", pidSegment.getPatientName()[0].getGivenName().getValue());
			
			// Check to see if the new NK1 segment exists.
			NK1 nk1Segment = (NK1) message.getMessage().get("NK1");
			assertEquals("Employed full time", nk1Segment.getJobStatus().getValue());

			// Make sure the EVN segment has been removed
			assertFalse(message.getMessage().toString().contains("EVN"));
			
		} catch (HL7Exception e) {
			fail("Unable to process HL7 message", e);
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		}
	}
	
	
	/**
	 * Test to make sure the default configuration is used if no configuration class is found.
	 */
	@Test
	public void testDefaultConfiguration() {
		try (HapiContext context = new DefaultHapiContext();) {

			String hl7 = Files.readString(Paths.get("src/test/resources/hl7/VXU_V04.txt"));

			BaseHL7MessageTransformationConfiguration configuration = (BaseHL7MessageTransformationConfiguration) ConfigurationUtil.getConfiguration("net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation", Direction.EGRES, "VXU_V04");
			
			HL7MessageTransformation transformation = new HL7MessageTransformation(hl7, configuration);
			transformation.transform();
			
			assertTrue(configuration instanceof DefaultHL7UpdateConfiguration);
		} catch (HL7Exception e) {
			fail("Unable to process HL7 message", e);
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		}		
	}
}
