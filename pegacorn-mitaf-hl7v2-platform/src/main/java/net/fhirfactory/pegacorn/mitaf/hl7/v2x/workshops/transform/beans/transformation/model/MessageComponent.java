package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.model;

/**
 * A message component.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageComponent {
	
	/**
	 * Gets the components value.
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract String value() throws Exception;
	
	
	/**
	 * Sets the components value.
	 * 
	 * @param value
	 * @throws Exception
	 */
	public abstract  void setValue(String value) throws Exception;
	
	
	/**
	 * Clears the components value.
	 * 
	 * @throws Exception
	 */
	public abstract  void clear() throws Exception;
	
	
	/**
	 * Copies the value from the sourceComponent to this component
	 * 
	 * @param sourceComponent
	 */
	public void copy(MessageComponent sourceComponent) throws Exception {
		this.setValue(sourceComponent.value());
	}
	
	
	/**
	 * Moves the value from the sourceComponent to this component.  This is a copy followed by a clear.
	 * 
	 * @param sourceComponent
	 */
	public void move(MessageComponent sourceComponent) throws Exception {
		copy(sourceComponent);
		sourceComponent.clear();
	}
}
