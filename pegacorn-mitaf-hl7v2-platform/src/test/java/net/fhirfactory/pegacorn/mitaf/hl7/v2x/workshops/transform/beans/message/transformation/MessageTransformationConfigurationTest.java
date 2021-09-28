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
import ca.uhn.hl7v2.model.v231.segment.PID;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.ADTA01TransformationConfigurationEgres;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.BaseHL7MessageTransformationConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.ConfigurationUtil;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.DefaultHL7TransformationConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.Direction;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.MDMT02TransformationConfigurationEgres;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.ORMTransformationConfigurationEgres;

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

			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			Message message = parser.parse(hl7);

			BaseHL7MessageTransformationConfiguration configuration = (BaseHL7MessageTransformationConfiguration) ConfigurationUtil.getConfiguration("net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation", Direction.EGRES, message.getName());
				
			HL7MessageTransformation transformation = new HL7MessageTransformation(hl7, configuration);

			assertTrue(configuration instanceof ADTA01TransformationConfigurationEgres);

			assertEquals(2, configuration.getMessageUpdateSteps().size());
			assertEquals(1, configuration.getSegmentsToBeRemoved().size());
			
			LOG.info("HL7 before transformation: {}", hl7);
			
			message = transformation.transform();

			LOG.info("HL7 after transformation: {}", message);

			// Make sure the name has been updated
			
			PID pidSegment = ((ADT_A01) message).getPID();
			assertEquals("Peter", pidSegment.getPatientName()[0].getGivenName().getValue());
			assertEquals("Anderson", pidSegment.getPatientName()[0].getFamilyLastName().getFamilyName().getValue());
			

			// Make sure the EVN segment has been removed
			assertFalse(message.getMessage().toString().contains("EVN"));
			
		} catch (HL7Exception e) {
			fail("Unable to process HL7 message", e);
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		}
	}
	
	
	/**
	 * Make sure a generic message config can be found eg. ORM instead of ORM^O01.
	 */
	@Test
	public void testGenericMessageConfig() {
		try (HapiContext context = new DefaultHapiContext();) {
			String hl7 = Files.readString(Paths.get("src/test/resources/hl7/ORM_O01.txt"));
			hl7 = hl7.replaceAll("\n", "\r");
			
			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			Message message = parser.parse(hl7);

			BaseHL7MessageTransformationConfiguration configuration = (BaseHL7MessageTransformationConfiguration) ConfigurationUtil.getConfiguration("net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation", Direction.EGRES, message.getName());
			
			HL7MessageTransformation transformation = new HL7MessageTransformation(hl7, configuration);
			
			message = transformation.transform();
			
			assertTrue(configuration instanceof ORMTransformationConfigurationEgres);
			
			// The message will still contain this segment because of the False rule configured in the annotation.
			assertTrue(message.toString().contains("ZDS"));
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		} catch(HL7Exception e) {
			fail("Unable to process HL7 message", e);			
		}
	}	
	
	
	/**
	 * Make sure a generic message config can be found eg. ORM instead of ORM^O01.
	 */
	@Test
	public void testRemoveAllRepititionsg() {
		try (HapiContext context = new DefaultHapiContext();) {
			String hl7 = Files.readString(Paths.get("src/test/resources/hl7/MDM_T02.txt"));
			hl7 = hl7.replaceAll("\n", "\r");
			
			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			Message message = parser.parse(hl7);

			BaseHL7MessageTransformationConfiguration configuration = (BaseHL7MessageTransformationConfiguration) ConfigurationUtil.getConfiguration("net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation", Direction.EGRES, message.getName());
			
			HL7MessageTransformation transformation = new HL7MessageTransformation(hl7, configuration);
			
			message = transformation.transform();
			
			assertTrue(configuration instanceof MDMT02TransformationConfigurationEgres);
			
			// All the QBR segments should be removed.
			assertFalse(message.toString().contains("OBR"));
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		} catch(HL7Exception e) {
			fail("Unable to process HL7 message", e);			
		}
	}	
	
	
	/**
	 * Test to make sure the default configuration is used if no configuration class is found.
	 */
	@Test
	public void testDefaultConfiguration() {
		try (HapiContext context = new DefaultHapiContext();) {

			String hl7 = Files.readString(Paths.get("src/test/resources/hl7/VXU_V04.txt"));
			
			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			Message message = parser.parse(hl7);

			BaseHL7MessageTransformationConfiguration configuration = (BaseHL7MessageTransformationConfiguration) ConfigurationUtil.getConfiguration("net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation", Direction.EGRES, message.getName());
			
			HL7MessageTransformation transformation = new HL7MessageTransformation(hl7, configuration);
			transformation.transform();
			
			assertTrue(configuration instanceof DefaultHL7TransformationConfiguration);
		} catch (HL7Exception e) {
			fail("Unable to process HL7 message", e);
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		}		
	}
}
