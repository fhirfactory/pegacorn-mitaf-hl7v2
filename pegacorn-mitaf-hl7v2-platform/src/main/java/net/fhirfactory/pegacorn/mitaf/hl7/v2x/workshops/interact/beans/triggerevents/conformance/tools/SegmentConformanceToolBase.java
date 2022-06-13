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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.conformance.tools;

import net.fhirfactory.pegacorn.internals.hl7v2.triggerevents.valuesets.HL7v2SegmentTypeEnum;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.conformance.HL7v2xTriggerEventConformanceToolInterface;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.conformance.HL7v2xTriggerEventConformanceToolbox;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public abstract class SegmentConformanceToolBase extends RouteBuilder implements HL7v2xTriggerEventConformanceToolInterface {

    private boolean initialised;

    @Inject
    private HL7v2xTriggerEventConformanceToolbox toolbox;

    //
    // Constructor
    //

    public SegmentConformanceToolBase(){
        setInitialised(false);
    }

    //
    // Getters and Setters
    //

    protected abstract Logger getLogger();
    protected abstract HL7v2SegmentTypeEnum getSupportedSegmentType();

    public boolean isInitialised() {
        return initialised;
    }

    public void setInitialised(boolean initialised) {
        this.initialised = initialised;
    }

    //
    // Post Construct
    //

    @PostConstruct
    public void initialise(){
        getLogger().debug(".initialise(): Entry");
        if(isInitialised()){
            getLogger().debug(".initialise(): Nothing to do, already initialised");
        } else {
            getLogger().info(".initialise(): Starting...");
            getLogger().info(".initialise(): [Register this Tool with Toolbox] Start");
            toolbox.addConformanceTool(getSupportedSegmentType(), this);
            getLogger().info(".initialise(): [Register this Tool with Toolbox] Finish");

            setInitialised(true);
            getLogger().info(".initialise(): Finished...");
        }
        getLogger().debug(".initialise(): Exit");
    }

    //
    // Startup Service
    //


    @Override
    public void configure() throws Exception {
        String instanceName = getClass().getSimpleName();

        from("timer://"+instanceName+"?delay=1000&repeatCount=1")
                .routeId("ConformanceTool::"+instanceName)
                .log(LoggingLevel.DEBUG, "Starting....");
    }
}
