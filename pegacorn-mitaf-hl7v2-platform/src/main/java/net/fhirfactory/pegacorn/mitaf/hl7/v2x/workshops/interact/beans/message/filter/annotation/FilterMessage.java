package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter.AllowMessageFilterCondition;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.message.filter.FilterCondition;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.FilterType;

/**
 * An annotation to use when filtering a message.  
 * 
 * 
 * @author Brendan Douglas
 *
 */
@Repeatable(FilterMessages.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FilterMessage {
	public String messageType();

	public Class<? extends FilterCondition> filterConditionClass() default AllowMessageFilterCondition.class;
	
	public FilterType filterType() default FilterType.POST_TRANSFORMATION;
}
