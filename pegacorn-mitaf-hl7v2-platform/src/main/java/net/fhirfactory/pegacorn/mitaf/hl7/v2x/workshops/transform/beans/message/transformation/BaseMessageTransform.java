/*
 * Copyright (c) 2021 ACT Health
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.ConfigPath;
/**
 * Performs message transformations.  Instantiates one or more transformation config classes and executes ALL the steps.
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
	
		List<String>packageNames = new ArrayList<>(); 
		
		// Get the class in the hierarchy so we can get the config location
		// annotations.
		List<Class<?>>classes = new ArrayList<>();
		Class<?> currentClass = this.getClass();
		while (!currentClass.getName().equals(BaseMessageTransform.class.getName())) {
			classes.add(currentClass);
			
			currentClass = currentClass.getSuperclass();
		}
		
		// Reverse so the rules higher up the hierarchy are execute first. 
		Collections.reverse(classes);
		
		// Get the config location annotations from the class hierarchy.  
		for (Class<?> clazz : classes) {
		
			ConfigPath[] locationAnnotations = clazz.getAnnotationsByType(ConfigPath.class);
		
			for (ConfigPath locationAnnotation :  locationAnnotations) {
				packageNames.add(((ConfigPath)locationAnnotation).value());
			}
		}
		
		List<BaseHL7MessageTransformationConfiguration> messageTransformationConfigurations = ConfigurationUtil.getConfiguration(packageNames, direction, message.getName());

		
		// Transform the message using ALL of the found config classes.	
		for (BaseHL7MessageTransformationConfiguration config : messageTransformationConfigurations) {
			HL7MessageTransformation transformation = new HL7MessageTransformation(message, config);

			message = transformation.transform();
		}
		
		return message;
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
