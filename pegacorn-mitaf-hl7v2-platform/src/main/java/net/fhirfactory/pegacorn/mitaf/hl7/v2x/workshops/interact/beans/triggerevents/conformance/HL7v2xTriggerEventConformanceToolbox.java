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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.conformance;

import net.fhirfactory.pegacorn.internals.hl7v2.triggerevents.valuesets.HL7v2SegmentTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class HL7v2xTriggerEventConformanceToolbox {
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2xTriggerEventConformanceToolbox.class);

    Map<HL7v2SegmentTypeEnum, HL7v2xTriggerEventConformanceToolInterface> toolbox;

    //
    // Constructor(s)
    //

    public HL7v2xTriggerEventConformanceToolbox(){
        this.toolbox = new ConcurrentHashMap<>();
    }

    //
    // Getters and Setters
    //

    protected Logger getLogger(){
        return(LOG);
    }

    //
    // Business Methods
    //

    public void addConformanceTool(HL7v2SegmentTypeEnum segmentType, HL7v2xTriggerEventConformanceToolInterface tool){
        getLogger().debug(".addConformanceTool(): Entry, segmentType->{}", segmentType);

        if(tool == null || segmentType == null){
            getLogger().debug(".addConformanceTool(): Exit, tool or segmentType are null");
            return;
        }

        if(toolbox.containsKey(segmentType)){
            getLogger().debug(".addConformanceTool(): Exit, tool already specified for given segmentType");
            return;
        }

        toolbox.put(segmentType, tool);
        getLogger().debug(".addConformanceTool(): Exit, tool added");
    }

    public HL7v2xTriggerEventConformanceToolInterface getConformanceTool(HL7v2SegmentTypeEnum segmentType){
        getLogger().debug(".getConformanceTool(): Entry, segmentType->{}", segmentType);

        if(segmentType == null){
            getLogger().debug(".getConformanceTool(): Exit, segmentType is null, returning -null-");
            return(null);
        }

        if(toolbox.containsKey(segmentType)){
            HL7v2xTriggerEventConformanceToolInterface conformanceTool = toolbox.get(segmentType);
            getLogger().debug(".getConformanceTool(): Exit, conformanceTool->{}", conformanceTool);
            return(conformanceTool);
        }

        getLogger().debug(".getConformanceTool(): Exit, no conformanceTool for segmentType, returning -null-");
        return(null);
    }
}
