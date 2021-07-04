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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.wup;


import net.fhirfactory.pegacorn.components.interfaces.topology.WorkshopInterface;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.HL7V2XTopicFactory;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.FHIRCommunicationToUoW;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.FHIRResourceSecurityMarkerInjection;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.HL7v2MessageAsTextToHL7V2xMessage;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.HL7v2xMessageIntoFHIRCommunication;
import net.fhirfactory.pegacorn.workshops.TransformWorkshop;
import net.fhirfactory.pegacorn.wups.archetypes.petasosenabled.messageprocessingbased.MOAStandardWUP;

import javax.inject.Inject;

/**
 * Base class for all Mitaf WUPs to transform HL7 v2 messages to a FHIR
 * Communication resource.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL7V2Message2FHIRCommunicationWUP extends MOAStandardWUP {
	private String WUP_VERSION = "1.0.0";

	@Inject
	private TransformWorkshop workshop;

	@Override
	protected WorkshopInterface specifyWorkshop(){
		return(workshop);
	}

	@Inject
	private HL7v2MessageAsTextToHL7V2xMessage hl7v2TextToMessage;

	@Inject
	private HL7v2xMessageIntoFHIRCommunication hl7v2xMessageIntoFHIRCommunication;

	@Inject
	private FHIRResourceSecurityMarkerInjection securityMarkerInjection;

	@Inject
	private FHIRCommunicationToUoW communicationIntoUoW;

	@Inject
	private HL7V2XTopicFactory topicFactory;

	@Override
	public void configure() throws Exception {
		getLogger().info(this.getClass().getName() + ":: ingresFeed() --> {}", this.ingresFeed());
		getLogger().info(this.getClass().getName() + ":: egressFeed() --> {}", this.egressFeed());

		fromIncludingPetasosServices(ingresFeed())
				.routeId(getNameSet().getRouteCoreWUP())
				.bean(hl7v2TextToMessage, "convertToMessage")
				.bean(hl7v2xMessageIntoFHIRCommunication, "encapsulateMessage")
				.bean(securityMarkerInjection, "injectSecurityMarkers")
				.bean(communicationIntoUoW, "packageCommunicationResource")
				.to(egressFeed());
	}

	public HL7V2XTopicFactory getTopicFactory() {
		return topicFactory;
	}
}
