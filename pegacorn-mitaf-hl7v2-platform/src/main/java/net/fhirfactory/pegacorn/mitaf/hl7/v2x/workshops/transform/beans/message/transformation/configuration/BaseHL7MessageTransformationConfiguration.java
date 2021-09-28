package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.RemoveHL7Segment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.UpdateHL7Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step.BaseHL7RemoveSegmentTransformationStep;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step.BaseHL7UpdateTransformationStep;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;

/**
 * Base configuration class for HL7 transformation configurations.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL7MessageTransformationConfiguration {

	private List<BaseHL7UpdateTransformationStep> messageUpdateSteps = new ArrayList<>();
	private List<BaseHL7RemoveSegmentTransformationStep> segmentsToBeRemoved = new ArrayList<>();
	
	protected abstract Logger getLogger();

	public BaseHL7MessageTransformationConfiguration() {
		
		Class<? extends Annotation>[] supportedAnnotationClasses = new Class[] { UpdateHL7Message.class, RemoveHL7Segment.class};


		for (Class<? extends Annotation> supportedAnnotationClass : supportedAnnotationClasses) {
			Annotation annotation = this.getClass().getAnnotation(supportedAnnotationClass);

			Rule rule = null;

			// Instantiate the rule class
			try {
				if (annotation instanceof UpdateHL7Message) {
					UpdateHL7Message updateAnnotation = (UpdateHL7Message)annotation;
				
					Constructor<?> constructor = updateAnnotation.ruleClass().getConstructor();
					rule = (Rule) constructor.newInstance();
					
					// Instantiate the update class
					try {
						Constructor<?> updateClassConstructor = updateAnnotation.updateClass().getConstructor(Rule.class);
						BaseHL7UpdateTransformationStep segmentUpdate = (BaseHL7UpdateTransformationStep) updateClassConstructor.newInstance(rule);

						this.messageUpdateSteps.add(segmentUpdate);
					} catch (Exception e) {
						throw new RuntimeException("Error creating the rule class", e);
					}
				} else if (annotation instanceof RemoveHL7Segment) {
					RemoveHL7Segment removeAnnotation = (RemoveHL7Segment)annotation;
					
					Constructor<?> constructor = removeAnnotation.ruleClass().getConstructor();
					rule = (Rule) constructor.newInstance();	
					
					// Instantiate the update class
					try {
						Constructor<?> removeClassConstructor = removeAnnotation.removalClass().getConstructor(Rule.class);
						BaseHL7RemoveSegmentTransformationStep segmentRemove = (BaseHL7RemoveSegmentTransformationStep) removeClassConstructor.newInstance(rule);

						this.segmentsToBeRemoved.add(segmentRemove);
					} catch (Exception e) {
						throw new RuntimeException("Error creating the rule class", e);
					}
				} 
			} catch (Exception e) {
				throw new RuntimeException("Error creating the rule class", e);
			}
		}
	}

	public List<BaseHL7UpdateTransformationStep> getSegmentsToBeUpdated() {
		return messageUpdateSteps;
	}
	
	public List<BaseHL7RemoveSegmentTransformationStep> getSegmentsToBeRemoved() {
		return segmentsToBeRemoved;
	}
}
