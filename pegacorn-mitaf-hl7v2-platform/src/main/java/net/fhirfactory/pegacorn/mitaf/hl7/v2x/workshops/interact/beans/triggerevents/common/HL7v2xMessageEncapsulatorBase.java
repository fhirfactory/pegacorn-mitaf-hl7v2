package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.common;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.interfaces.topology.ProcessingPlantInterface;
import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;
import net.fhirfactory.pegacorn.core.model.petasos.participant.ProcessingPlantPetasosParticipantNameHolder;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.HL7V2XTopicFactory;
import net.fhirfactory.pegacorn.internals.hl7v2.helpers.UltraDefensivePipeParser;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.EndpointMetricsAgent;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgent;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgentAccessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public abstract class HL7v2xMessageEncapsulatorBase {

    private static final Integer SYNAPSE_PAYLOAD_SIZE = 32000;

    private HapiContext context;
    private DateTimeFormatter timeFormatter;
    private boolean initialised;
    private boolean includeFullHL7MessageInLog;
    private Integer maxHL7MessageSize;

    @Inject
    private HL7V2XTopicFactory topicFactory;

    @Inject
    private ProcessingPlantInterface processingPlant;

    @Inject
    private ProcessingPlantPetasosParticipantNameHolder participantNameHolder;

    @Inject
    private ProcessingPlantMetricsAgentAccessor processingPlantMetricsAgentAccessor;

    @Inject
    private UltraDefensivePipeParser defensivePipeParser;

    //
    // Constructor(s)
    //

    public HL7v2xMessageEncapsulatorBase() {
        context = new DefaultHapiContext();
        timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.of(PetasosPropertyConstants.DEFAULT_TIMEZONE));
        maxHL7MessageSize = SYNAPSE_PAYLOAD_SIZE;
        includeFullHL7MessageInLog = false;
        initialised = false;
    }

    //
    // Post Construct
    //

    @PostConstruct
    public void initialise() {
        getLogger().debug(".initialise(): Entry");
        if (initialised) {
            getLogger().debug(".initialise(): Nothing to do, already initialised!");
        } else {
            getLogger().info(".initialise(): Start");
            getLogger().info(".initialise(): [Check if Full HL7 Message to be included in Log] Start");
            String includeMessageString = getProcessingPlant().getMeAsASoftwareComponent().getOtherConfigurationParameter(PetasosPropertyConstants.INCLUDE_FULL_HL7_MESSAGE_IN_LOG);
            if (StringUtils.isNotEmpty(includeMessageString)) {
                if (includeMessageString.equalsIgnoreCase("true")) {
                    setIncludeFullHL7MessageInLog(true);
                }
            }
            getLogger().info(".initialise(): [Check if Full HL7 Message to be included in Log] include->{}", isIncludeFullHL7MessageInLog());
            getLogger().info(".initialise(): [Check if Full HL7 Message to be included in Log] Finish");
            getLogger().info(".initialise(): [Check Size Of HL7 Message to be included in Log] Start");
            String messageMaximumSize = getProcessingPlant().getMeAsASoftwareComponent().getOtherConfigurationParameter(PetasosPropertyConstants.MAXIMUM_HL7_MESSAGE_SIZE_IN_LOG);
            if (StringUtils.isNotEmpty(messageMaximumSize)) {
                Integer messageMaxSize = Integer.getInteger(messageMaximumSize);
                if (messageMaxSize != null) {
                    setMaxHL7MessageSize(messageMaxSize);
                }
            }
            getLogger().info(".initialise(): [Check Size Of HL7 Message to be included in Log] MaximumSize->{}", getMaxHL7MessageSize());
            getLogger().info(".initialise(): [Check Size Of HL7 Message to be included in Log] Finish");
            getLogger().info(".initialise(): Finish");
            this.initialised = true;
        }
        getLogger().debug(".initialise(): Exit");
    }

    //
    // Getters (and Setters)
    //

    protected ProcessingPlantPetasosParticipantNameHolder getParticipantNameHolder() {
        return (participantNameHolder);
    }

    protected HL7V2XTopicFactory getTopicFactory() {
        return (topicFactory);
    }

    protected DateTimeFormatter getTimeFormatter() {
        return (timeFormatter);
    }

    abstract protected Logger getLogger();

    protected ProcessingPlantInterface getProcessingPlant() {
        return (processingPlant);
    }

    protected UltraDefensivePipeParser getDefensivePipeParser() {
        return (defensivePipeParser);
    }

    protected boolean isIncludeFullHL7MessageInLog() {
        return includeFullHL7MessageInLog;
    }

    protected void setIncludeFullHL7MessageInLog(boolean includeFullHL7MessageInLog) {
        this.includeFullHL7MessageInLog = includeFullHL7MessageInLog;
    }

    protected Integer getMaxHL7MessageSize() {
        return maxHL7MessageSize;
    }

    protected void setMaxHL7MessageSize(Integer maxHL7MessageSize) {
        this.maxHL7MessageSize = maxHL7MessageSize;
    }

    protected ProcessingPlantMetricsAgent getProcessingPlantMetricsAgent() {
        return (processingPlantMetricsAgentAccessor.getMetricsAgent());
    }

    public boolean triggerIsSupported(String trigger) {
        return true;
    }

    public DataParcelTypeDescriptor createDataParcelTypeDescriptor(String messageEventType, String messageTriggerEvent, String version) {
        DataParcelTypeDescriptor descriptor = getTopicFactory().newDataParcelDescriptor(messageEventType, messageTriggerEvent, version);
        return (descriptor);
    }

    //
    // Business Functions
    //

    protected void sendMessageReceivedConsoleNotification(String portDescription, String message, String mshSegment, String pidSegment, EndpointMetricsAgent endpointMetricsAgent) {
        getLogger().debug(".sendMessageReceivedConsoleNotification(): Entry, portDescription->{}, message->{}", portDescription, message);
        StringBuilder notificationContentBuilder = new StringBuilder();
        notificationContentBuilder.append("Ingres-Message((" + portDescription + ")(" + getTimeFormatter().format(Instant.now()) + ")){");
        if (includeFullHL7MessageInLog) {
            String displayedMessage = null;
            try {
                if (message.length() > getMaxHL7MessageSize()) {
                    displayedMessage = message.substring(0, getMaxHL7MessageSize());
                } else {
                    displayedMessage = message;
                }
            } catch (Exception ex) {
                displayedMessage = "Cannot Parse Message Content";
                getLogger().warn(".sendMessageReceivedConsoleNotification(): Cannot parse message, error->{}, stackTrace->{}", ExceptionUtils.getMessage(ex), ExceptionUtils.getStackTrace(ex));
            }
            notificationContentBuilder.append(displayedMessage);
        } else {
            notificationContentBuilder.append(mshSegment);
            notificationContentBuilder.append(" ::: ");
            notificationContentBuilder.append(pidSegment);
        }
        notificationContentBuilder.append("}");

        StringBuilder formattedNotificationContent = new StringBuilder();
        formattedNotificationContent.append("<table>");
        formattedNotificationContent.append("<tr>");
        formattedNotificationContent.append("<th> From </th><th>" + portDescription + " (" + getTimeFormatter().format(Instant.now()) + ") </th");
        formattedNotificationContent.append("</tr>");
        formattedNotificationContent.append("<tr>");
        formattedNotificationContent.append("<td> Metadata </td><td>" + mshSegment + "\n" + pidSegment + "</td>");
        formattedNotificationContent.append("</tr>");
        formattedNotificationContent.append("</table>");

        endpointMetricsAgent.sendITOpsNotification(notificationContentBuilder.toString(), formattedNotificationContent.toString());

        getLogger().debug(".sendMessageReceivedConsoleNotification(): Exit");
    }

    protected void sendMessageReceivedConsoleNotification(String portDescription, Message message, String mshSegment, String pidSegment, EndpointMetricsAgent endpointMetricsAgent) {
        getLogger().debug(".sendMessageReceivedConsoleNotification(): Entry, portDescription->{}, message->{}", portDescription, message);

        String displayedMessage = null;
        try {
            String actualMessage = message.encode();

            if (actualMessage.length() > getMaxHL7MessageSize()) {
                displayedMessage = actualMessage.substring(0, getMaxHL7MessageSize());
            } else {
                displayedMessage = actualMessage;
            }
        } catch (Exception ex) {
            displayedMessage = "Cannot Parse Message Content";
            getLogger().warn(".sendMessageReceivedConsoleNotification(): Cannot parse message, error->{}, stackTrace->{}", ExceptionUtils.getMessage(ex), ExceptionUtils.getStackTrace(ex));
        }
        sendMessageReceivedConsoleNotification(portDescription, displayedMessage, mshSegment, pidSegment, endpointMetricsAgent);
        getLogger().debug(".sendMessageReceivedConsoleNotification(): Exit");
    }
}


