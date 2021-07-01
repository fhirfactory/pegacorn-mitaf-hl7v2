package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.engine;

/**
 * Base class for all data retrievers. Data retrievers are used when fetching data for a hl7 or fhir message.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseDataRetriever<T extends MitafMessage> {

	public abstract Object get(T message);

}
