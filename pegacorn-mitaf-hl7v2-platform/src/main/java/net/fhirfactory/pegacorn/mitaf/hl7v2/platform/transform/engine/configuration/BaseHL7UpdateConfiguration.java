package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration.annotation.RemoveHL7Segment;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration.annotation.UpdateHL7Segment;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.hl7.BaseHL7SegmentRemover;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.hl7.BaseHL7SegmentUpdater;
import net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.rule.Rule;

/**
 * Base configuration class for HL7 transformation configurations.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL7UpdateConfiguration extends BaseConfiguration {

	private List<BaseHL7SegmentUpdater> segmentUpdaters = new ArrayList<>();
	private List<BaseHL7SegmentRemover> segmentRemovers = new ArrayList<>();

	public BaseHL7UpdateConfiguration() {
		
		Class<? extends Annotation>[] supportedAnnotationClasses = new Class[] { UpdateHL7Segment.class, RemoveHL7Segment.class };


		for (Class<? extends Annotation> supportedAnnotationClass : supportedAnnotationClasses) {
			Annotation annotation = this.getClass().getAnnotation(supportedAnnotationClass);

			Rule rule = null;

			// Instantiate the rule class
			try {
				if (annotation instanceof UpdateHL7Segment) {
					UpdateHL7Segment updateAnnotation = (UpdateHL7Segment)annotation;
				
					Constructor<?> constructor = updateAnnotation.ruleClass().getConstructor();
					rule = (Rule) constructor.newInstance();
					
					// Instantiate the update class
					try {
						Constructor<?> updateClassConstructor = updateAnnotation.updateClass().getConstructor(Rule.class);
						BaseHL7SegmentUpdater segmentUpdater = (BaseHL7SegmentUpdater) updateClassConstructor.newInstance(rule);

						this.segmentUpdaters.add(segmentUpdater);
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
						BaseHL7SegmentRemover segmentRemover = (BaseHL7SegmentRemover) removeClassConstructor.newInstance(rule);

						this.segmentRemovers.add(segmentRemover);
					} catch (Exception e) {
						throw new RuntimeException("Error creating the rule class", e);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("Error creating the rule class", e);
			}
		}
	}

	public List<BaseHL7SegmentUpdater> getSegmentUpdaters() {
		return segmentUpdaters;
	}
	
	public List<BaseHL7SegmentRemover> getSegmentRemovers() {
		return segmentRemovers;
	}
}
