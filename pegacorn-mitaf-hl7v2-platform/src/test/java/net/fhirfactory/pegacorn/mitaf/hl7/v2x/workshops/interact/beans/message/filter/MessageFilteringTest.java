package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.FilterType;

public class MessageFilteringTest {
	
	@Test
	public void testTrueFilterADT() {
		try (HapiContext context = new DefaultHapiContext();) {
			String hl7 = Files.readString(Paths.get("src/test/resources/hl7/ADT_A01.txt"));
			hl7 = hl7.replaceAll("\n", "\r");

			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			Message message = parser.parse(hl7);
			
			SegmentFilterConfig filter = new SegmentFilterConfig();
			assertTrue(filter.doFilter(message, FilterType.POST_TRANSFORMATION));
		} catch (HL7Exception e) {
			fail("Unable to process HL7 message", e);
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		}				
	}
	
	
	@Test
	public void testFalseFilterADT() {
		try (HapiContext context = new DefaultHapiContext();) {
			String hl7 = Files.readString(Paths.get("src/test/resources/hl7/MDM_T02.txt"));
			hl7 = hl7.replaceAll("\n", "\r");

			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			Message message = parser.parse(hl7);
			
			SegmentFilterConfig filter = new SegmentFilterConfig();
			assertFalse(filter.doFilter(message, FilterType.POST_TRANSFORMATION));
		} catch (HL7Exception e) {
			fail("Unable to process HL7 message", e);
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		}				
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testDefaultAllowMessageT() {
		try (HapiContext context = new DefaultHapiContext();) {
			String hl7 = Files.readString(Paths.get("src/test/resources/hl7/ADT_A01.txt"));
			hl7 = hl7.replaceAll("\n", "\r");

			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			Message message = parser.parse(hl7);
			
			FilterConfigUsingDefaultAllowMessage filter = new FilterConfigUsingDefaultAllowMessage();
			assertTrue(filter.doFilter(message, FilterType.POST_TRANSFORMATION));
		} catch (HL7Exception e) {
			fail("Unable to process HL7 message", e);
		} catch (IOException e) {
			fail("Unable to read HL7 message", e);
		}				
	}

}
