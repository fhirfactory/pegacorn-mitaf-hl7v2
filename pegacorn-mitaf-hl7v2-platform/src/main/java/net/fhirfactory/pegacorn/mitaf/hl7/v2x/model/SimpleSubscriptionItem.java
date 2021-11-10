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

import net.fhirfactory.pegacorn.core.model.dataparcel.DataParcelTypeDescriptor;

import java.util.ArrayList;
import java.util.List;

public class SimpleSubscriptionItem {
    private String sourceSystem;
    private List<DataParcelTypeDescriptor> descriptorList;

    public SimpleSubscriptionItem(){
        descriptorList = new ArrayList<>();
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public List<DataParcelTypeDescriptor> getDescriptorList() {
        return descriptorList;
    }

    public void setDescriptorList(List<DataParcelTypeDescriptor> descriptorList) {
        this.descriptorList = descriptorList;
    }

    @Override
    public String toString() {
        return "SimpleSubscriptionItem{" +
                "sourceSystem=" + sourceSystem +
                ", descriptorList=" + descriptorList +
                '}';
    }
}
