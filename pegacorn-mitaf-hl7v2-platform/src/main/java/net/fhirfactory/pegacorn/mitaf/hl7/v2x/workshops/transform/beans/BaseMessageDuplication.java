package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelDirectionEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelNormalisationStatusEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelValidationStatusEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.PolicyEnforcementPointApprovalStatusEnum;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.HL7MessageWithAttributes;

/**
 * Base class for all classes which need to duplicate a message.  The duplication rules are provided by the sub classes.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseMessageDuplication {

	public UoW duplicateMessage(UoW uow) throws Exception {
		List<DataParcelManifest> subscribedTopics = new ArrayList<>();

		DataParcelManifest manifest = SerializationUtils.clone(uow.getIngresContent().getPayloadManifest());
		manifest.setContainerDescriptor(null);
		manifest.getContentDescriptor().setDataParcelAttribute("DuplicateMessage");

		manifest.setDataParcelFlowDirection(DataParcelDirectionEnum.INFORMATION_FLOW_OUTBOUND_DATA_PARCEL);
		manifest.setNormalisationStatus(DataParcelNormalisationStatusEnum.DATA_PARCEL_CONTENT_NORMALISATION_ANY);
		manifest.setValidationStatus(DataParcelValidationStatusEnum.DATA_PARCEL_CONTENT_VALIDATION_ANY);
		manifest.setEnforcementPointApprovalStatus(PolicyEnforcementPointApprovalStatusEnum.POLICY_ENFORCEMENT_POINT_APPROVAL_POSITIVE);
		manifest.setIntendedTargetSystem("*");
		manifest.setSourceSystem("*");
		manifest.setInterSubsystemDistributable(false);
		subscribedTopics.add(manifest);

		UoWPayload emptyPayload = new UoWPayload();
		emptyPayload.setPayloadManifest(manifest);

		UoW newUoW = new UoW(emptyPayload);

		Message originalMessage = null;

		// get the first and only payload element.
		String hl7Message = uow.getIngresContent().getPayload();

		List<Message> messages = null;

		try (HapiContext context = new DefaultHapiContext();) {
			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);

			originalMessage = parser.parse(hl7Message);
			messages = createMessages(originalMessage);
		}

		uow.getEgressContent().getPayloadElements().clear();
		

		// Add a entry as a unit of work payload.
		for (int i = 0; i < messages.size(); i++) {				
			HL7MessageWithAttributes messageAttributes = addDuplicationAttributes(messages, i);
			
			UoWPayload contentPayload = new UoWPayload();

			contentPayload.setPayloadManifest(manifest);
			contentPayload.setPayload(messageAttributes.toString());

			newUoW.getEgressContent().getPayloadElements().add(contentPayload);
		}

		newUoW.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS);

		return newUoW;
	}
	
	
	/**
	 * Create the new messages.
	 * 
	 * @param orginalMessage
	 * @return
	 * @throws Exception
	 */
	public abstract List<Message>createMessages(Message orginalMessage) throws Exception;
	
	public abstract Logger getLogger();
	
	/**
	 * Add duplication message attributes.  The default implementation adds 
	 * 
	 * @param messages
	 * @param currentMessageIndex
	 * @return
	 */
	public HL7MessageWithAttributes addDuplicationAttributes(List<Message> messages, int currentMessageIndex) {
		return new HL7MessageWithAttributes(messages.get(currentMessageIndex).toString());
	}
}
