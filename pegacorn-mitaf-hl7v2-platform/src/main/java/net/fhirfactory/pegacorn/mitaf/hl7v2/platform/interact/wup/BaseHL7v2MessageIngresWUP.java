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
package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.interact.wup;

import net.fhirfactory.pegacorn.components.interfaces.topology.WorkshopInterface;
import net.fhirfactory.pegacorn.petasos.core.moa.wup.GenericMessageBasedWUPEndpoint;
import net.fhirfactory.pegacorn.workshops.InteractWorkshop;
import net.fhirfactory.pegacorn.wups.archetypes.petasosenabled.messageprocessingbased.InteractIngresMessagingGatewayWUP;

import javax.inject.Inject;

/**
 * Base class for all Mitaf Ingres WUPs.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseHL7v2MessageIngresWUP extends InteractIngresMessagingGatewayWUP {

	private String WUP_NAME="HL7v2MessageIngresWUP";
	private String WUP_VERSION="1.0.0";

	@Inject
	private InteractWorkshop interactWorkshop;

	@Override
	protected String specifyIngresTopologyEndpointName() {
		return null;
	}

	@Override
	protected String specifyIngresEndpointVersion() {
		return null;
	}

	@Override
	protected String specifyWUPInstanceName() {
		return (WUP_NAME);
	}

	@Override
	protected String specifyWUPInstanceVersion() {
		return (WUP_VERSION);
	}

	@Override
	protected WorkshopInterface specifyWorkshop() {
		return (interactWorkshop);
	}

	@Override
	protected GenericMessageBasedWUPEndpoint specifyIngresTopologyEndpoint() {
		return null;
	}

	@Override
	public void configure() throws Exception {

	}
}
