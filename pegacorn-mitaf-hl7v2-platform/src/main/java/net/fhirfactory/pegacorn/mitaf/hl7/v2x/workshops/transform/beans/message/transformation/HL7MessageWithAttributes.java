package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * HL7 Message + message attributes to be used in freemarker templates.
 * 
 * @author Brendan Douglas
 *
 */
public class HL7MessageWithAttributes {
	
	private String message;
	protected Map<String, Object> attributes = new HashMap<>();
	
	
	public HL7MessageWithAttributes(String message) {
		this.message = message;
	}

	
	/**
	 * Constructs this object from a json object.
	 * 
	 * @param messageWithAttributes
	 */
	public HL7MessageWithAttributes(JSONObject messageWithAttributes) {
		
		this.message = messageWithAttributes.getString("message");
		
		JSONArray jsonArray = messageWithAttributes.getJSONArray("attributes");
		
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject attribute = jsonArray.getJSONObject(i);
			
			
			Iterator<String>iterator = attribute.keys();
			
			while(iterator.hasNext()) {
				String keyValue = iterator.next();
							
				attributes.put(keyValue, attribute.get(keyValue));
			}
		}
	}

	
	public void addAttribute(String key, Object value) {
		attributes.put(key, value);
	}

	
	@Override
	public String toString() {
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("message", message);
		
		JSONArray jsonAttributeArray = new JSONArray();
		
		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
			JSONObject attribute = new JSONObject();
			attribute.put(entry.getKey(), entry.getValue());
			jsonAttributeArray.put(attribute);
		}
		
		jsonObject.put("attributes", jsonAttributeArray);

		return jsonObject.toString();
	}
	
	
	public Message getMessage() throws Exception {
		try (HapiContext hapiContext = new DefaultHapiContext();) {
			PipeParser parser = hapiContext.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			hapiContext.setModelClassFactory(cmf);

			Message message = parser.parse(this.message);
			return message;
		}
	}
	
	
	public  Map<String, Object> getAttributes() {
		return attributes;
	}
}
