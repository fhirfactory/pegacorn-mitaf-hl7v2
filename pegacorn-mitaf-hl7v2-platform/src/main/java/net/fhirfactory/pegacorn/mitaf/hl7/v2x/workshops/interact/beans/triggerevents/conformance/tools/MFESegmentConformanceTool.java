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
import net.fhirfactory.pegacorn.internals.hl7v2.triggerevents.valuesets.HL7v2VersionEnum;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MFESegmentConformanceTool extends SegmentConformanceToolBase {
    private static final Logger LOG = LoggerFactory.getLogger(MFESegmentConformanceTool.class);

    //
    // Constructor(s)
    //

    public MFESegmentConformanceTool(){
        super();
    }

    //
    // Getters (and Setters)
    //

    @Override
    protected Logger getLogger(){
        return(LOG);
    }

    @Override
    protected HL7v2SegmentTypeEnum getSupportedSegmentType() {
        return (HL7v2SegmentTypeEnum.MFE);
    }

//
    // Business Methods
    //

    @Override
    public String correctSegment(HL7v2VersionEnum version, String segment) {
        getLogger().debug(".correctSegment(): Entry, version->{}, segment->{}", version, segment);
        if(StringUtils.isEmpty(segment)){
            getLogger().debug(".correctSegment(): Exit, segment is empty");
            return(null);
        }
        if(version == null){
            getLogger().debug(".correctSegment(): Exit, versdion is empty");
        }

        String correctedSegment = null;
        switch(version){
            case VERSION_UNKNOWN:
            case VERSION_HL7_V21:
            case VERSION_HL7_V22:
            case VERSION_HL7_V23:
                correctedSegment = segment;
                break;
            case VERSION_HL7_V231:
            case VERSION_HL7_V24:
            case VERSION_HL7_V25:
            case VERSION_HL7_V251:
                correctedSegment = correctSegmentV231toV251(segment);
                break;
            case VERSION_HL7_V26:
            default:
                correctedSegment = segment;
        }

        getLogger().debug(".correctSegment(): Exit, correctedSegment->{}", correctedSegment);
        return(correctedSegment);
    }

    @Override
    public String checkSegment(HL7v2VersionEnum version, String segment) {
        throw(new NotImplementedException());
    }

    //
    // Per Segment Version Corrections
    //

    private String correctSegmentV231toV251(String inputSegment){
        getLogger().debug(".correctSegmentV231toV251(): Entry, inputSegment->{}", inputSegment);
        String segmentWithNewLineRemoved = inputSegment.replace("\\n", "");
        String segmentWithCarriageReturnRemoved = segmentWithNewLineRemoved.replace("\\r", "");
        String[] fieldSet = segmentWithCarriageReturnRemoved.split("\\|");
        int fieldCount = fieldSet.length;
        getLogger().debug(".correctSegmentV231toV251(): Split the segment into fields, field count->{}", fieldCount);
        if(fieldCount < 5){
            getLogger().debug(".correctSegmentV231toV251(): Exit, malformed inputSegment, outputSegment = inputSegment");
            return(inputSegment);
        }

        String outputSegment = null;
        if(fieldCount == 5){
            outputSegment = segmentWithCarriageReturnRemoved + "|CE";
        }
        if(fieldCount== 6){
            outputSegment = inputSegment;
        }
        getLogger().debug(".correctSegmentV231toV251(): Exit, outputSegment->{}", outputSegment);
        return(outputSegment);
    }
}
