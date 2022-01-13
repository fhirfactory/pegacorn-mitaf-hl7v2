/*
 * Copyright (c) 2021 ACT Health
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans;

import ca.uhn.hl7v2.model.Segment;
import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.interfaces.oam.notifications.PetasosITOpsNotificationBrokerInterface;
import net.fhirfactory.pegacorn.core.model.petasos.oam.notifications.PetasosComponentITOpsNotification;
import net.fhirfactory.pegacorn.core.model.petasos.oam.topology.valuesets.PetasosMonitoredComponentTypeEnum;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.transformation.HL7MessageUtils;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgent;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.ProcessingPlantMetricsAgentAccessor;
import net.fhirfactory.pegacorn.petasos.oam.metrics.agents.WorkUnitProcessorMetricsAgent;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Dependent
public class HL7v2MessageExtractor {
	private static final Logger LOG = LoggerFactory.getLogger(HL7v2MessageExtractor.class);

	private DateTimeFormatter timeFormatter;

	@Inject
	private ProcessingPlantMetricsAgentAccessor processingPlantMetricsAgentAccessor;

	@Inject
	private PetasosITOpsNotificationBrokerInterface notificationAgent;

	//
	// Constructor(s)
	//

	public HL7v2MessageExtractor(){
		timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").withZone(ZoneId.of(PetasosPropertyConstants.DEFAULT_TIMEZONE));
	}

	//
	// Getters (and Setters)
	//

    protected Logger getLogger(){
        return(LOG);
    }

	protected DateTimeFormatter getTimeFormatter(){
		return(timeFormatter);
	}

    //
	// Business Methods
	//

	public String convertToMessage(UoW incomingUoW, Exchange camelExchange) {
		getLogger().debug(".convertToMessage(): Entry, incomingUoW->{}", incomingUoW);
		String messageAsString = incomingUoW.getIngresContent().getPayload();
		//
		// Because auditing is not running yet
		// Remove once Auditing is in place
		//
		//getLogger().info("IncomingMessage-----------------------------------------------------------------");
		getLogger().warn("OutgoingMessage->{}", messageAsString); // Log at WARN level so always seen in TEST
		//getLogger().info("IncomingMessage-----------------------------------------------------------------");
		//
		//
		//

		//
		// Do some Processing Plant metrics
		getMetricsAgent().incrementEgressMessageCount();
		//
		// Do some WUP Metrics
		WorkUnitProcessorMetricsAgent metricsAgent = camelExchange.getProperty(PetasosPropertyConstants.WUP_METRICS_AGENT_EXCHANGE_PROPERTY, WorkUnitProcessorMetricsAgent.class);
		metricsAgent.incrementEgressMessageCount();
		metricsAgent.touchLastActivityInstant();


		//
		// Add some notifications
		UoWPayload payload = incomingUoW.getIngresContent();

		String notificationContent = buildMessage(payload.getPayload());
		metricsAgent.sendITOpsNotification(notificationContent);

		getLogger().debug(".convertToMessage(): Entry, messageAsString->{}", messageAsString);
		return (messageAsString);
	}

	//
	// Getters (and Setters)
	//

	protected ProcessingPlantMetricsAgent getMetricsAgent(){
		return(processingPlantMetricsAgentAccessor.getMetricsAgent());
	}

	//
	// To be replaced by a more generic function

	private String buildMessage(String payload){
		if(payload == null){
			return("*malformed payload*");
		}
		List<String> segmentList = getSegmentList(payload);
		String pid = null;
		String msh = null;
		if(segmentList != null){
			pid = getPatientIdentitySegment(segmentList);
			msh = getMessageHeaderSegment(segmentList);
		}
		if(pid == null){
			pid = "No PID Segment";
		}
		if(msh == null){
			return("*malformed payload*");
		}
		String notificationContent = "--- \n" +
				"*MLLP Sender*" + "\n" +
				"Sending Message (" + getTimeFormatter().format(Instant.now()) + ")" + "\n" +
				msh + "\n" +
				pid + "\n" +
				"---";
		return(notificationContent);
	}

	private String getMessageHeaderSegment(List<String> segmentList){
		if(segmentList == null){
			return(null);
		}
		for(String currentSegment: segmentList){
			if(currentSegment.startsWith("MSH")){
				return(currentSegment);
			}
		}
		return(null);
	}

	private String getPatientIdentitySegment(List<String> segmentList){
		if(segmentList == null){
			return(null);
		}
		for(String currentSegment: segmentList){
			if(currentSegment.startsWith("PID")){
				return(currentSegment);
			}
		}
		return(null);
	}

	private List<String> getSegmentList(String message){
		if(message == null){
			return(new ArrayList<>());
		}
		if(!message.contentEquals("\r")){
			return(new ArrayList<>());
		}
		String[] segmentArray = message.split("\r");
		List<String> segmentList = new ArrayList<>();
		for(String currentSegment: segmentArray){
			getLogger().info("Segment->{}", currentSegment);
			segmentList.add(currentSegment);
		}
		return(segmentList);
	}
}
