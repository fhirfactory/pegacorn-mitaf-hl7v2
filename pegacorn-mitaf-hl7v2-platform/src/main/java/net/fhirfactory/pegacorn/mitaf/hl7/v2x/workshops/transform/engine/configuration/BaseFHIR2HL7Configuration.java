package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.configuration.annotation.CreateHL7Segment;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine.hl7.BaseHL7SegmentCreator;

/**
 * Base configuration class for FHIR to HL7 conversion configurations.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseFHIR2HL7Configuration extends BaseConfiguration {
	private List<BaseHL7SegmentCreator> segmentCreators = new ArrayList<>();

	/**
	 * 
	 */
	public BaseFHIR2HL7Configuration() {

		CreateHL7Segment[] transformationAnnotations = this.getClass().getAnnotationsByType(CreateHL7Segment.class);

		for (CreateHL7Segment annotation : transformationAnnotations) {

			try {
				Constructor<?> constructor = annotation.creationClass().getConstructor();
				BaseHL7SegmentCreator segmentCreator = (BaseHL7SegmentCreator) constructor.newInstance();

				this.segmentCreators.add(segmentCreator);
			} catch (Exception e) {
				throw new RuntimeException("Error creating the rule class", e);
			}
		}
	}

	public List<BaseHL7SegmentCreator> getSegmentCreators() {
		return segmentCreators;
	}
}
