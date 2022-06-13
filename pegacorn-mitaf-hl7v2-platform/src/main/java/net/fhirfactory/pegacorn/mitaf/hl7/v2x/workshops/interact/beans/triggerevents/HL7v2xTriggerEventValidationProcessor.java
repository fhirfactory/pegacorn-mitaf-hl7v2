/*
 * Copyright (c) 2022 Mark A. Hunter
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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents;

import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWPayload;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoWProcessingOutcomeEnum;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.base.IPCTopologyEndpoint;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.mllp.MLLPServerEndpoint;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.mllp.adapters.MLLPServerAdapter;
import net.fhirfactory.pegacorn.core.model.topology.nodes.WorkUnitProcessorSoftwareComponent;
import net.fhirfactory.pegacorn.internals.hl7v2.helpers.UltraDefensivePipeParser;
import net.fhirfactory.pegacorn.internals.hl7v2.triggerevents.valuesets.HL7v2SegmentTypeEnum;
import net.fhirfactory.pegacorn.internals.hl7v2.triggerevents.valuesets.HL7v2VersionEnum;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.datatypes.MLLPMessageActivityParcel;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.conformance.HL7v2xTriggerEventConformanceToolInterface;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.conformance.HL7v2xTriggerEventConformanceToolbox;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

@ApplicationScoped
public class HL7v2xTriggerEventValidationProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xTriggerEventValidationProcessor.class);

    private boolean checkInitialisationDone;
    private boolean complianceActivityToBeDone;

    @Inject
    private UltraDefensivePipeParser defensivePipeParser;

    @Inject
    private HL7v2xTriggerEventConformanceToolbox triggerEventConformanceToolbox;

    //
    // Constructor(s)
    //

    public HL7v2xTriggerEventValidationProcessor(){
        setCheckInitialisationDone(false);
        setComplianceActivityToBeDone(false);
    }

    //
    // Getters (and Setters)
    //

    protected Logger getLogger(){
        return(LOG);
    }

    protected UltraDefensivePipeParser getDefensivePipeParser(){
        return(this.defensivePipeParser);
    }

    protected HL7v2xTriggerEventConformanceToolbox getTriggerEventConformanceToolbox(){
        return(this.triggerEventConformanceToolbox);
    }

    public boolean isCheckInitialisationDone() {
        return checkInitialisationDone;
    }

    public void setCheckInitialisationDone(boolean checkInitialisationDone) {
        this.checkInitialisationDone = checkInitialisationDone;
    }

    public boolean isComplianceActivityToBeDone() {
        return complianceActivityToBeDone;
    }

    public void setComplianceActivityToBeDone(boolean complianceActivityToBeDone) {
        this.complianceActivityToBeDone = complianceActivityToBeDone;
    }

    //
    // Business Methods
    //

    public MLLPMessageActivityParcel ensureMinimalCompliance(MLLPMessageActivityParcel parcel, Exchange camelExchange){
        getLogger().debug(".ensureMinimalCompliance(): Entry, parcel->{}", parcel);

        if(!shouldPerformComplianceFixes(camelExchange)){
            getLogger().debug(".ensureMinimalCompliance(): Exit, flag not set for conformance fixes!");
            return(parcel);
        }

        //
        // Some defensive programming

        if(parcel == null){
            getLogger().debug(".ensureMinimalCompliance(): Exit, parcel is null, returning -null-");
            return(null);
        }

        if(parcel.getUow() == null) {
            getLogger().debug(".ensureMinimalCompliance(): Exit, uow within parcel is null, returning parcel as is");
            return (parcel);
        } else {
            if(!parcel.getUow().getProcessingOutcome().equals(UoWProcessingOutcomeEnum.UOW_OUTCOME_SUCCESS)){
                getLogger().debug(".ensureMinimalCompliance(): Exit, uow has a failure condition, returning parcel as is");
                return (parcel);
            }
        }

        //
        // Process the Payload

        UoWPayload payload = parcel.getUow().getIngresContent();

        // fix any bad segments
        Map<Integer, String> segmentList = getDefensivePipeParser().getOrderedSegmentList(payload.getPayload());
        HL7v2xTriggerEventConformanceToolInterface conformanceTool = null;
        for(int counter = 0; counter < segmentList.size(); counter += 1){
            getLogger().info(".ensureMinimalCompliance(): processing segment <{}>", counter);
            String segment = segmentList.get(counter);
            HL7v2SegmentTypeEnum segmentType = getDefensivePipeParser().getSegmentType(segment);
            getLogger().info(".ensureMinimalCompliance(): processing segmentType->{}",segmentType);
            if(segmentType != null){
                conformanceTool = getTriggerEventConformanceToolbox().getConformanceTool(segmentType);
                if(conformanceTool != null){
                    getLogger().info(".ensureMinimalCompliance(): using conformanceTool!");
                    String correctedSegment = conformanceTool.correctSegment(HL7v2VersionEnum.VERSION_HL7_V231, segment);
                    segmentList.put(counter, correctedSegment);
                }
            }
        }

        // Rebuild the payload
        StringBuilder outputMessage = new StringBuilder();
        for(int counter = 0; counter < segmentList.size(); counter += 1) {
            outputMessage.append(segmentList.get(counter) + "\r");
        }
        String newPayloadString = outputMessage.toString();
        payload.setPayload(newPayloadString);

        getLogger().debug(".ensureMinimalCompliance(): Exit, parcel->{}", parcel);
        return(parcel);
    }

    //
    // Derive Behaviour from Flags
    //

    protected boolean shouldPerformComplianceFixes(Exchange camelExchange){
        getLogger().debug(".shouldPerformComplianceFixes(): Entry");
        if(isCheckInitialisationDone()){
            getLogger().debug(".shouldPerformComplianceFixes(): Exit, initialisation already done... ");
            return(isComplianceActivityToBeDone());
        }
        getLogger().debug(".shouldPerformComplianceFixes(): Doing 1st Pass Initialisation of Parameters");
        boolean shouldPerformConformance = false;
        try{
            WorkUnitProcessorSoftwareComponent workUnitProcessorSoftwareComponent = camelExchange.getProperty(PetasosPropertyConstants.WUP_TOPOLOGY_NODE_EXCHANGE_PROPERTY_NAME, WorkUnitProcessorSoftwareComponent.class);
            MLLPServerEndpoint serverTopologyEndpoint = (MLLPServerEndpoint)workUnitProcessorSoftwareComponent.getIngresEndpoint();
            MLLPServerAdapter mllpAdapter = serverTopologyEndpoint.getMLLPServerAdapter();
            String testValue = mllpAdapter.getAdditionalParameters().get(PetasosPropertyConstants.HL7_TRIGGER_EVENT_MINIMAL_CONFORMANCE_ENFORCEMENT_PARAMETER_NAME);
            getLogger().debug(".shouldPerformComplianceFixes(): testValue->{}", testValue);
            if(StringUtils.isNotEmpty(testValue)){
                if(testValue.equalsIgnoreCase("True")){
                    shouldPerformConformance = true;
                }
            }
        } catch(Exception ex){
            getLogger().debug(".shouldPerformComplianceFixes(): Problem, message->{}, stackTrace->{}", ExceptionUtils.getMessage(ex), ExceptionUtils.getStackTrace(ex));
            shouldPerformConformance = false;
        }
        setComplianceActivityToBeDone(shouldPerformConformance);
        setCheckInitialisationDone(true);
        return(isComplianceActivityToBeDone());
    }
}
