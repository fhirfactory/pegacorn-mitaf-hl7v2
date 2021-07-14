/*
 * Copyright (c) 2021 Mark A. Hunter
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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.model;

public enum HL7v2VersionEnum {
    VERSION_UNKNOWN("unknown"),
    VERSION_HL7_V23("2.3"),
    VERSION_HL7_V231("2.3.1"),
    VERSION_HL7_V24("2.4"),
    VERSION_HL7_V25("2.5"),
    VERSION_HL7_V251("2.5.1");

    private String VersionText;

    private HL7v2VersionEnum(String version){
        this.VersionText = version;
    }

    public String getVersionText() {
        return VersionText;
    }

    public static HL7v2VersionEnum fromVersionText(String version){
        for(HL7v2VersionEnum currentTest: HL7v2VersionEnum.values()){
            if(currentTest.getVersionText().contentEquals(version)){
                return(currentTest);
            }
        }
        return(VERSION_UNKNOWN);
    }
}
