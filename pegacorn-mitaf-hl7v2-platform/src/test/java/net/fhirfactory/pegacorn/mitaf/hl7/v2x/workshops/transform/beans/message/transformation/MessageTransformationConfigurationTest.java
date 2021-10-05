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
	 * Tests updating a message.
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
			
			MessageTransformationBeanWithOneConfigPath transformation = new MessageTransformationBeanWithOneConfigPath();
			
			transformation.doEgressTransform(message);

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
	 * Make sure multiple segments can be removed.
	 */
	@Test
	public void testRemoveAllSegments() {
		try (HapiContext context = new DefaultHapiContext();) {
			String hl7 = Files.readString(Paths.get("src/test/resources/hl7/MDM_T02.txt"));
			hl7 = hl7.replaceAll("\n", "\r");
			
			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			Message message = parser.parse(hl7);

			MessageTransformationBeanWithOneConfigPath transformation = new MessageTransformationBeanWithOneConfigPath();
			
			transformation.doEgressTransform(message);
		
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
			hl7 = hl7.replaceAll("\n", "\r");
			
			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			Message message = parser.parse(hl7);
			String messageBeforeTransformStr = message.toString();
			

			MessageTransformationBeanWithOneConfigPath transformation = new MessageTransformationBeanWithOneConfigPath();
			
			transformation.doEgressTransform(message);
			
			assertEquals(messageBeforeTransformStr, message.toString());
			
			// Make sure we are comparing different objects.
			assertFalse(messageBeforeTransformStr == message.toString());
		} catch (HL7Exception e) {
			fail("Unable to process HL7 message", e);
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		}		
	}
	
	
	
	/**
	 * Test to make sure the transformation rules in multiple config classes can be executed.
	 */
	@Test
	public void testUpdateMessageMultiplePackages() {
		try (HapiContext context = new DefaultHapiContext();) {
			String hl7 = Files.readString(Paths.get("src/test/resources/hl7/ADT_A01.txt"));
			hl7 = hl7.replaceAll("\n", "\r");

			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			Message message = parser.parse(hl7);
			
			MessageTransformationBeanWithMultipleConfigPaths transformation = new MessageTransformationBeanWithMultipleConfigPaths();
			
			transformation.doEgressTransform(message);

			// Make sure the name has been updated.
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
	 * Test to make sure multiple message types can use the same transformation config class.
	 */
	@Test
	public void testMultipleMessageTypesOnConfigClass() {
		try (HapiContext context = new DefaultHapiContext();) {
			String adt = Files.readString(Paths.get("src/test/resources/hl7/ADT_A01.txt"));
			adt = adt.replaceAll("\n", "\r");
			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);
			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			Message adtMessage = parser.parse(adt);
			
			MessageTransformationBeanMultipleMessageTypes transformation = new MessageTransformationBeanMultipleMessageTypes();
			
			transformation.doEgressTransform(adtMessage);
			
			// Make sure the PID segment has been removed
			assertFalse(adtMessage.getMessage().toString().contains("PID"));
			
			String orm = Files.readString(Paths.get("src/test/resources/hl7/MDM_T02.txt"));
			orm = orm.replaceAll("\n", "\r");
			parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);
			cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			Message ormMessage = parser.parse(orm);
			
			transformation.doEgressTransform(ormMessage);
			
			// Make sure the PID segment has been removed
			assertFalse(ormMessage.getMessage().toString().contains("PID"));			
			
		} catch (HL7Exception e) {
			fail("Unable to process HL7 message", e);
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		}
	}
	
	
	/**
	 * Test to make sure the transformation rules in can be executed when the message type has a wildcard character.
	 */
	@Test
	public void testUpdateWildcareMessageTypeConfig() {
		try (HapiContext context = new DefaultHapiContext();) {
			String hl7 = Files.readString(Paths.get("src/test/resources/hl7/ADT_A01.txt"));
			hl7 = hl7.replaceAll("\n", "\r");

			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			Message message = parser.parse(hl7);
			
			MessageTransformationBeanWithWildcardConfig transformation = new MessageTransformationBeanWithWildcardConfig();
			
			transformation.doEgressTransform(message);
	
			// Make sure the PID segment has been removed
			assertFalse(message.getMessage().toString().contains("PID"));
			
		} catch (HL7Exception e) {
			fail("Unable to process HL7 message", e);
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		}
	}
	
	
	/**
	 * Tests the required segment codes annotation.  All segments which are not one of the 
	 * required ones are removed.
	 */
	@Test
	public void testRequiredSegmentCodes() {
		try (HapiContext context = new DefaultHapiContext();) {
			String hl7 = Files.readString(Paths.get("src/test/resources/hl7/ADT_A01.txt"));
			hl7 = hl7.replaceAll("\n", "\r");

			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			Message message = parser.parse(hl7);
			
			MessageTransformationBeanWithRequiredSegmentsConfig transformation = new MessageTransformationBeanWithRequiredSegmentsConfig();
			
			transformation.doEgressTransform(message);
	
			// Make sure the following 2 segments have been removed
			assertFalse(message.getMessage().toString().contains("PID"));
			assertFalse(message.getMessage().toString().contains("PV1"));
			
			// Make sure the following 2 segments still exist
			assertTrue(message.getMessage().toString().contains("MSH"));
			assertTrue(message.getMessage().toString().contains("EVN"));
			
		} catch (HL7Exception e) {
			fail("Unable to process HL7 message", e);
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		}		
	}
	
	
	/**
	 * Tests the false rule.  Make sure the transformation does not occur.
	 */
	@Test
	public void testFalseRule() {
		try (HapiContext context = new DefaultHapiContext();) {
			String hl7 = Files.readString(Paths.get("src/test/resources/hl7/ADT_A01.txt"));
			hl7 = hl7.replaceAll("\n", "\r");

			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			Message message = parser.parse(hl7);
			
			MessageTransformationBeanWithRequiredSegmentsFalseRuleConfig transformation = new MessageTransformationBeanWithRequiredSegmentsFalseRuleConfig();
			
			transformation.doEgressTransform(message);
	
			// Make sure the following 4 segments have not been removed
			assertTrue(message.getMessage().toString().contains("PID"));
			assertTrue(message.getMessage().toString().contains("PV1"));
			assertTrue(message.getMessage().toString().contains("MSH"));
			assertTrue(message.getMessage().toString().contains("EVN"));
			
			// Make sure the name has not been updated
			PID pidSegment = ((ADT_A01) message).getPID();
			assertEquals("ADAM", pidSegment.getPatientName()[0].getGivenName().getValue());
			assertEquals("EVERYMAN", pidSegment.getPatientName()[0].getFamilyLastName().getFamilyName().getValue());
			
		} catch (HL7Exception e) {
			fail("Unable to process HL7 message", e);
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		}		
	}
}
