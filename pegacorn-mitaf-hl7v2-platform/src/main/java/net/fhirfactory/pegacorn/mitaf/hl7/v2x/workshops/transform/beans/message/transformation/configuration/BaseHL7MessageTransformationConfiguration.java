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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.RemoveHL7Segment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.RemoveHL7Segments;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.RequiredHL7Segment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.UpdateHL7Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.annotation.UpdateHL7Messages;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step.BaseHL7UpdateTransformationStep;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step.BaseMitafMessageTransformStep;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step.HL7RemoveSegmentTransformationStep;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step.HL7RequiredSegmentsTransformationStep;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.configuration.rule.Rule;

/**
 * Base configuration class for HL7 transformation configurations.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL7MessageTransformationConfiguration {

	private List<BaseMitafMessageTransformStep> transformationSteps = new ArrayList<>();
	
	protected abstract Logger getLogger();

	public BaseHL7MessageTransformationConfiguration() {
		boolean containsRequiredAnnotation = false;
		boolean containesRemoveAnnotation = false;
		
		Class<? extends Annotation>[] supportedAnnotationClasses = new Class[] { UpdateHL7Message.class, RemoveHL7Segment.class, UpdateHL7Messages.class, RemoveHL7Segments.class, RequiredHL7Segment.class};


		for (Class<? extends Annotation> supportedAnnotationClass : supportedAnnotationClasses) {
			Annotation[] annotations = this.getClass().getAnnotationsByType(supportedAnnotationClass);

			for (Annotation annotation : annotations) {
				Rule rule = null;
	
				// Instantiate the rule class
				try {
					if (annotation instanceof UpdateHL7Message) {
						getLogger().debug("Found update segment annotation");
						
						UpdateHL7Message updateAnnotation = (UpdateHL7Message)annotation;
					
						Constructor<?> constructor = updateAnnotation.ruleClass().getConstructor();
						rule = (Rule) constructor.newInstance();
						
						// Instantiate the update class
						try {
							Constructor<?> updateClassConstructor = updateAnnotation.updateClass().getConstructor(Rule.class);
							BaseHL7UpdateTransformationStep segmentUpdateStep = (BaseHL7UpdateTransformationStep) updateClassConstructor.newInstance(rule);
	
							this.transformationSteps.add(segmentUpdateStep);
						} catch (Exception e) {
							throw new RuntimeException("Error creating the rule class", e);
						}
					} else if (annotation instanceof RemoveHL7Segment) {
						getLogger().debug("Found remove segment annotation");
						
						containesRemoveAnnotation = true;
						
						RemoveHL7Segment removeAnnotation = (RemoveHL7Segment)annotation;
						
						Constructor<?> constructor = removeAnnotation.ruleClass().getConstructor();
						rule = (Rule) constructor.newInstance();	
						
						// Instantiate the update class
						try {
							String segmentCode = ((RemoveHL7Segment)removeAnnotation).value();
							int repetition = ((RemoveHL7Segment)removeAnnotation).repetition();
							
							HL7RemoveSegmentTransformationStep segmentRemoveStep = new HL7RemoveSegmentTransformationStep(segmentCode, rule, repetition);
	
							this.transformationSteps.add(segmentRemoveStep);
						} catch (Exception e) {
							throw new RuntimeException("Error creating the rule class", e);
						}
					} else if (annotation instanceof RequiredHL7Segment) {
						getLogger().debug("Found required segment annotation");
						
						containsRequiredAnnotation = true;
						
						RequiredHL7Segment allowedSegmentAnnotation = (RequiredHL7Segment)annotation;
						
						Constructor<?> constructor = allowedSegmentAnnotation.ruleClass().getConstructor();
						rule = (Rule) constructor.newInstance();	
						
						// Instantiate the allowed segments class.  Pass all the allowed segments to the class.  This is different from the other annotations where it is one segment per class.
						try {
							RequiredHL7Segment[] allowedSegments = this.getClass().getAnnotationsByType(RequiredHL7Segment.class);
							
							List<String>allowedSegmentCodes = new ArrayList<>();
							
							for (RequiredHL7Segment allowedSegment : allowedSegments) {
								allowedSegmentCodes.add(allowedSegment.value());
							}
												
							HL7RequiredSegmentsTransformationStep allowedSegmentStep = new HL7RequiredSegmentsTransformationStep(allowedSegmentCodes, rule);
	
							this.transformationSteps.add(allowedSegmentStep);
						} catch (Exception e) {
							throw new RuntimeException("Error creating the rule class", e);
						}
					}
				} catch (Exception e) {
					throw new RuntimeException("Error creating the configuration class", e);
				}
				
				// It doesn't make sense to have both the remove and reuqired annotations on the same class.
				if (containesRemoveAnnotation && containsRequiredAnnotation) {
					throw new RuntimeException("@RemoveHL7Segment and @RequiredHL7Segment must not be used on the same class");
				}
			}
		}
	}

	public List<BaseMitafMessageTransformStep> getTransformationSteps() {
		return transformationSteps;
	}
}
