package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import net.fhirfactory.pegacorn.csv.core.CSV;
import net.fhirfactory.pegacorn.internals.hl7v2.HL7Message;

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
	 * Returns a {@link Message} from a {@link HL7Message}
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static Message getMessage(HL7Message hl7Message) throws Exception {
		return hl7Message.getSourceMessage();
	}

	
	/**
	 * Returns a {@link HL7Message} from a {@link Message}
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static HL7Message getHL7Message(Message message) throws Exception {
		return new HL7Message(message);
	}
	
	
	/**
	 * Returns a {@link HL7Message} from a String
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static HL7Message getHL7Message(String messageStr) throws Exception {
		Message message = getMessage(messageStr);
		
		return getHL7Message(message);
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
