package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.ModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelManifest;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelDirectionEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelNormalisationStatusEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.DataParcelValidationStatusEnum;
import net.fhirfactory.pegacorn.core.model.dataparcel.valuesets.PolicyEnforcementPointApprovalStatusEnum;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.HL7MessageWithAttributes;
import net.fhirfactory.pegacorn.petasos.core.tasks.accessors.PetasosFulfillmentTaskSharedInstance;

/**
 * Base class for all classes which need to duplicate a message.  The duplication rules are provided by the sub classes.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseMessageDuplication {

    private static final Logger LOG = LoggerFactory.getLogger(BaseMessageDuplication.class);
	
	public UoW duplicateMessage(UoW uow, Exchange exchange) throws Exception {
			    
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

		List<HL7MessageWithAttributes> messages = null;

		try (HapiContext context = new DefaultHapiContext();) {
			PipeParser parser = context.getPipeParser();
			parser.getParserConfiguration().setValidating(false);

			ModelClassFactory cmf = new DefaultModelClassFactory();
			context.setModelClassFactory(cmf);

			originalMessage = parser.parse(hl7Message);
			messages = createMessages(originalMessage);
			
			// Add a total count of the messages attribute.
			for (int i = 0; i < messages.size(); i++) {
				messages.get(i).addAttribute("total_number_of_messages", messages.size());
			}
		}

		uow.getEgressContent().getPayloadElements().clear();
		
		
		// If there are no messages to be sent then discard the fulfilment task.
        if (messages.size() == 0) {
        	PetasosFulfillmentTaskSharedInstance fulfillmentTask = exchange.getProperty(PetasosPropertyConstants.WUP_PETASOS_FULFILLMENT_TASK_EXCHANGE_PROPERTY, PetasosFulfillmentTaskSharedInstance.class);
            LOG.warn("postTransformProcessing() -> No messages to be sent, discard fullfilment task.");
            fulfillmentTask.getTaskFulfillment().setToBeDiscarded(true);
            uow.setProcessingOutcome(UoWProcessingOutcomeEnum.UOW_OUTCOME_FILTERED);
            
            return uow;
        }

		// Add a entry as a unit of work payload.
		for (HL7MessageWithAttributes message : messages) {
			UoWPayload contentPayload = new UoWPayload();

			contentPayload.setPayloadManifest(manifest);
			contentPayload.setPayload(message.toString());

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
	public abstract List<HL7MessageWithAttributes>createMessages(Message orginalMessage) throws Exception;
	
	public abstract Logger getLogger();
}
