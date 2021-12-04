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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.interfaces;

import ca.uhn.hl7v2.model.Message;

import java.util.Date;

public interface HL7v2xInformationExtractionInterface {
    public String extractMessageID(Message message);
    public String extractMessageID(String messageAsText);
    public Date extractMessageDate(Message message);
    public Date extractMessageDate(String messageAsText);
    public String extractMessageSource(Message message);
    public String extractMessageSource(String messageAsText);
    public String extractMessageVersion(Message message);
    public String extractMessageVersion(String messageAsText);
    public String convertMessageToString(Message message);
    public Message convertToHL7v2Message(String messageAsText);
    public String extractMessageTrigger(Message message);
    public String extractMessageTrigger(String messageAsText);
    public String extractMessageType(Message message);
    public String extractMessageType(String messageAsText);
}
