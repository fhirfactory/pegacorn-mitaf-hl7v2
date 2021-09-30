package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import java.io.IOException;

import org.slf4j.Logger;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.BaseHL7MessageTransformationConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.ConfigurationUtil;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.Direction;

/**
 * Performs message transformations.  An appropriate transformation class is instantiated
 * based on the message type and the message flow.
 * 
 * An implementation of this class is required even if no transformation is needed.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseMessageTransform {
	
	protected abstract Logger getLogger();
	
	protected String getBaseConfigurationPackageName() {	
		return this.getClass().getPackageName();
	}

	public Message doTransform(Message message, Direction direction) throws HL7Exception, IOException {
		BaseHL7MessageTransformationConfiguration configuration = (BaseHL7MessageTransformationConfiguration) ConfigurationUtil.getConfiguration(getBaseConfigurationPackageName(), direction, message.getName());
		
		HL7MessageTransformation transformation = new HL7MessageTransformation(message, configuration);

		return transformation.transform();
	}
	
	
	private Message doTransform(String message, Direction direction) throws HL7Exception, IOException {
		try (HapiContext context = new DefaultHapiContext();) {
			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			
			return doTransform(parser.parse(message), direction);
		} 
	}
	
	
	public Message doIngresTransform(Message message) throws HL7Exception, IOException {
		return doTransform(message, Direction.INGRES);
	}
	
	
	public Message doIngresTransform(String message) throws HL7Exception, IOException {
		return doTransform(message, Direction.INGRES);
	}
	
	
	public Message doEgressTransform(Message message) throws HL7Exception, IOException {
		return doTransform(message, Direction.EGRESS);
	}
	
	
	public Message doEgressTransform(String message) throws HL7Exception, IOException {
		return doTransform(message, Direction.EGRESS);
	}
}
