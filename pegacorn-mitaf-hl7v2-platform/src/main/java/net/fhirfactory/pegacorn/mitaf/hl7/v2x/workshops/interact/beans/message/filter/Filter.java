package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter.annotation.FilterMessage;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.FilterType;

/**
 * Base configuration class for HL7 filter config classes.  A config class contains annotations at the class level describing the message filtering requirements.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class Filter {
	private Map<String, FilterCondition>preTransformationFilters = new HashedMap<String, FilterCondition>();
	private Map<String, FilterCondition>postTransformationFilters = new HashedMap<String, FilterCondition>();

	protected abstract Logger getLogger();

	public Filter() {
		
		for (FilterMessage filterAnnotation : this.getClass().getAnnotationsByType(FilterMessage.class)) {
			FilterCondition filterCondition = null;
	
			try {			
				Constructor<?> constructor = filterAnnotation.filterConditionClass().getConstructor();
				filterCondition = (FilterCondition) constructor.newInstance();	

				String mesageType = ((FilterMessage)filterAnnotation).messageType();
				
				FilterType filterType = ((FilterMessage)filterAnnotation).filterType();
				
				if (filterType == FilterType.PRE_TRANSFORMATION) {
					this.preTransformationFilters.put(mesageType, filterCondition);
				} else {
					this.postTransformationFilters.put(mesageType, filterCondition);
				}
			} catch (Exception e) {
				throw new RuntimeException("Error creating the filter", e);
			}
		}
	}
	
	
	public boolean doFilter(Message message, FilterType filterType) throws HL7Exception {

		// Try to find a rule based on message type.  If not found then try a rule using a wildcard
		FilterCondition filterCondition = null;
		
		if (filterType == FilterType.PRE_TRANSFORMATION) {
			filterCondition = preTransformationFilters.get(message.getName());
		} else {
			filterCondition = postTransformationFilters.get(message.getName());
		}
		
		if (filterCondition == null) {
			// Try a filter condition with a wildcard
			String wildcardName = message.getName().substring(0,3) + "_*";
			
			if (filterType == FilterType.PRE_TRANSFORMATION) {
				filterCondition = preTransformationFilters.get(wildcardName);
			} else {
				filterCondition = postTransformationFilters.get(wildcardName);
			}
		}
		
		if (filterCondition == null) {
			return true;
		}
		
		return filterCondition.execute(message);
	}
}
