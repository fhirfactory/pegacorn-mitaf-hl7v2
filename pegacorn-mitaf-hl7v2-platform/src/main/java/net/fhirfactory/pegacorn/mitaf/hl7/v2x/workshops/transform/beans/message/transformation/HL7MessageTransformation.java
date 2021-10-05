package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.AbstractGroup;
import ca.uhn.hl7v2.model.AbstractSegment;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.BaseHL7MessageTransformationConfiguration;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step.BaseHL7UpdateTransformationStep;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step.BaseMitafMessageTransformStep;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step.HL7RemoveSegmentTransformationStep;

/**
 * Does the actual transformation by executing the transformation steps.
 * 
 * @author Brendan Douglas
 *
 */
public class HL7MessageTransformation  {
	private static final Logger LOG = LoggerFactory.getLogger(HL7MessageTransformation.class);

	private Message message;
	private BaseHL7MessageTransformationConfiguration config;
	
	public HL7MessageTransformation(String mesage, BaseHL7MessageTransformationConfiguration config) throws IOException, HL7Exception {
		this.config = config;
		
		try (HapiContext context = new DefaultHapiContext();) {
			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);
			this.message = parser.parse(mesage);
		} 
	}
	
	public HL7MessageTransformation(Message message, BaseHL7MessageTransformationConfiguration config) throws IOException, HL7Exception {
		this.config = config;
		this.message = message;
	}

	
	/**
	 * Execute the transformation steps.
	 * 
	 * @return
	 * @throws HL7Exception
	 */
	public Message transform() throws HL7Exception {
		for (BaseMitafMessageTransformStep transformationStep : config.getTransformationSteps()) {
			transformationStep.process(message);
		}
				
		return message;
	}

	
	protected Logger getLogger() {
		return LOG;
	}

	@Override
	public String toString() {
		return message.toString();
	}

	
	protected AbstractSegment getSegment(String segmentCode) throws HL7Exception {
		AbstractGroup group = (AbstractGroup) message;

		return (AbstractSegment) group.get(segmentCode);
	}
}