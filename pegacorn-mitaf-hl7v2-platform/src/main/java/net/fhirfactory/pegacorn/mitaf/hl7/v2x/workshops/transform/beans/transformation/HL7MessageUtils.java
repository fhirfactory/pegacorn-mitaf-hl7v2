package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import net.fhirfactory.pegacorn.csv.core.CSV;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model.HL7Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model.Segment;

/**
 * Utility methods to transform a messages.
 * 
 * @author Brendan Douglas
 *
 */
public class HL7MessageUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(HL7MessageUtils.class);
	
	
	/**
	 * Returns a {@link Message} from a String
	 * 
	 * @param message
	 * @return
	 */
	public static Message getMessage(String message) throws Exception {
		try (HapiContext context = new DefaultHapiContext();) {    
            PipeParser parser = context.getPipeParser();
            parser.getParserConfiguration().setValidating(false);
    
            ModelClassFactory cmf = new DefaultModelClassFactory();
            context.setModelClassFactory(cmf);
            
            Message inputMessage = parser.parse(message);
            
            return inputMessage;
		}
	} 

	
	/**
	 * Returns a pegacorn HL7 message object from message.
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static HL7Message getHL7Message(Message message) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		
		return hl7Message;
	}

	
	/**
	 * 
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static String getMessageCode(Message message) throws Exception {
		return get(message, "MSH-9-1");
	}

	
	/**
	 * Gets the message type.
	 * 
	 * @param message
	 * @return
	 */
	public static String getType(Message message) {
		return message.getName();
	}
	
	
	/**
	 * Is the message of the supplied type?
	 * 
	 * @param message
	 * @param messageType
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	public static boolean isType(Message message, String messageType) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		return hl7Message.isType(messageType);
	}

	
	/**
	 * Set the target field to the supplied value.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param value
	 * @throws HL7Exception
	 */
	@Deprecated
	public static void set(Message message, String targetPathSpec, String value) throws Exception {	
		HL7TerserBasedUtils.set(message, targetPathSpec, value);
	}
	
	
	/**
	 * Set a field value from a string with variables.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @param seperator
	 * @param values
	 * @throws HL7Exception
	 */
	@Deprecated
	public static void set(Message message, String targetPathSpec, String value, String ... params) throws Exception {
		Terser terser = new Terser(message);
		
		String finalValue = String.format(value, (Object[])params);
		terser.set(targetPathSpec, finalValue);
	}
	
	
	/**
	 * Gets a field value.
	 * 
	 * @param message
	 * @param sourcePathSpec
	 * @return
	 * @throws HL7Exception
	 */
	@Deprecated
	public static String get(Message message, String sourcePathSpec) throws Exception {	
		Terser terser = new Terser(message);
		return terser.get(sourcePathSpec);
	}
	
	
	/**
	 * Check if a segment exists.
	 * 
	 * @param message
	 * @param segment
	 * @return
	 */
	@Deprecated
	public static boolean doesSegmentExist(Message message, String segment) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		return hl7Message.doesSegmentExist(segment);
	}
	
	
	/**
	 * Returns a segment.
	 * 
	 * @param message
	 * @param segmentName
	 * @param occurrence
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	public static Segment getSegment(Message message, String segmentName, int occurrence) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		return hl7Message.getSegment(segmentName, occurrence);
	}
	
	
	/**
	 * Returns the 1st occurrence of a segment.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	public static Segment getSegment(Message message, String segmentName) throws Exception {		
		return getSegment(message, segmentName, 0);
	}
	
	
	/**
	 * Returns a segment at the specified index.
	 * 
	 * @param message
	 * @param segmentIndex
	 * @return
	 */
	@Deprecated
	public static Segment getSegment(Message message, int segmentIndex) {
		HL7Message hl7Message = new HL7Message(message);
		return hl7Message.getSegment(segmentIndex);
	}
	
	
	/**
	 * Returns all matching segments.
	 * 
	 * @param message
	 * @param segmentName
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	public static List<Segment> getSegments(Message message, String segmentName) throws Exception {
		HL7Message hl7Message = new HL7Message(message);
		return hl7Message.getSegments(segmentName);		
	}
	
	/**
	 * Clear a single field value including all repetitions.
	 * 
	 * @param message
	 * @param targetPathSpec
	 * @throws HL7Exception
	 */
    @Deprecated
	public static void clear(Message message, String targetPathSpec) throws Exception {
		HL7TerserBasedUtils.clear(message, targetPathSpec);
	}

	
	/**
	 * Constructs and returns a CSV class which can be used in the transformation templates.
	 * 
	 * @param classname
	 * @param csvContent
	 * @return
	 */
	public static CSV getCSV(String classname, String csvContent) {
		try {
			Class<?> csvClass = Class.forName(classname);
			Constructor<?> csvClassConstructor = csvClass.getConstructor(String.class);
			CSV csv = (CSV) csvClassConstructor.newInstance(new Object[] {csvContent});
			
			return csv;
		} catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
			LOG.info("Unable to construct csv class: {} ", classname);		
		}
		
		return null;
	}
}
