package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.configuration.step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Structure;

/**
 * Creates a map of what message groups a segment is in.
 * 
 * @author Brendan Douglas
 *
 */
public class SegmentGroups {
	
	
	public static Map<String, List<Group>> get(Message message) throws HL7Exception {
		Map<String, List<Group>>segmentInGroups = new HashMap<String, List<Group>>();
			
		String[] names = message.getNames();
		
		for (String name : names) {
			get(name, segmentInGroups, message);
		}
		
		return segmentInGroups;
	}
	
	
	
	/**
	 * A recursive method to populate the map of groups that a segment is in.
	 * 
	 * @param groupName
	 * @param segmentInGroups
	 * @param group
	 * @throws HL7Exception
	 */
	private static void get(String groupName, Map<String, List<Group>>segmentInGroups, Group group) throws HL7Exception {
		Structure structure = group.get(groupName);
		
		if (structure instanceof Group) {
			String namesInGroup[] = ((Group) structure).getNames();
			
			for (String nameInGroup : namesInGroup) {
				get(nameInGroup, segmentInGroups, (Group)structure);
			}
			
		} else {
			
		  List<Group> groups = segmentInGroups.get(groupName);
		  if (groups == null) {
			  groups = new ArrayList<Group>();
		  }
		  
		  groups.add(group);
		  segmentInGroups.put(groupName, groups);
		  
		  return;
		}
	}
	
}
