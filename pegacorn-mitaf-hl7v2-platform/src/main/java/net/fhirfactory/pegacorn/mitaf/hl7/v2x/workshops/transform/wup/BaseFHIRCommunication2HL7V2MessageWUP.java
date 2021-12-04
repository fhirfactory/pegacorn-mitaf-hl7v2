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

import net.fhirfactory.pegacorn.core.interfaces.topology.WorkshopInterface;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.topics.HL7V2XTopicFactory;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.HL7v2xMessageOutOfFHIRCommunication;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.HL7v2xTransformMessage;
import net.fhirfactory.pegacorn.workshops.TransformWorkshop;
import net.fhirfactory.pegacorn.wups.archetypes.petasosenabled.messageprocessingbased.MOAStandardWUP;

import javax.inject.Inject;


/**
 * Base class for all Mitaf WUPs to transform FHIR Communication resource to a
 * HL7 v2 message.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseFHIRCommunication2HL7V2MessageWUP extends MOAStandardWUP {

	public BaseFHIRCommunication2HL7V2MessageWUP(){
		super();
	}

	@Inject
	protected HL7V2XTopicFactory hl7v2xTopicIDBuilder;

	@Inject
	private TransformWorkshop workshop;
	
	@Override
	protected WorkshopInterface specifyWorkshop() {
		return (workshop);
	}

	@Override
	public void configure() throws Exception {
        getLogger().info("{}:: ingresFeed() --> {}", getClass().getName(), ingresFeed());
        getLogger().info("{}:: egressFeed() --> {}", getClass().getName(), egressFeed());

		fromIncludingPetasosServices(ingresFeed())
			.routeId(getNameSet().getRouteCoreWUP())
	        .bean(HL7v2xMessageOutOfFHIRCommunication.class, "extractAndTransformMessage")
	        .bean(HL7v2xTransformMessage.class, "setHL7MessageAsExchangeBody(*, Exchange)")
			.setHeader("systemName", constant(System.getenv("KUBERNETES_SERVICE_NAME")))
			.to("direct-vm:" + "transform-filter-egress-start")
			.to(egressFeed());
	}
}