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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.datatypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.fhirfactory.pegacorn.core.model.petasos.uow.UoW;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MLLPMessageActivityParcel implements Serializable {
    private UoW uow;
    private String mllpConnectionLocalAddress;
    private String mllpConnectionRemoteAddress;
    private String mllpAcknowledgement;
    private String mllpAcknowledgementType;
    private String mllpSendingFacility;
    private String mllpSendingApplication;
    private String mllpReceivingFacility;
    private String mllpReceivingApplication;
    private String mllpTimestamp;
    private String mllpSecurity;
    private String mllpMessageType;
    private String mllpEventType;
    private String mllpTriggerEvent;
    private String mllpMessageControlId;
    private String mllpProcessingId;
    private String mllpVersionId;
    private String mllpCharSet;
    Map<Integer, String> notes;

    //
    // Constructor(s)
    //

    public MLLPMessageActivityParcel(){
        this.uow = null;
        this.mllpConnectionLocalAddress = null;
        this.mllpConnectionRemoteAddress = null;
        this.mllpAcknowledgement = null;
        this.mllpAcknowledgementType = null;
        this.mllpReceivingFacility = null;
        this.mllpReceivingApplication = null;
        this.mllpSendingApplication = null;
        this.mllpSendingFacility = null;
        this.mllpTimestamp = null;
        this.mllpSecurity = null;
        this.mllpMessageType = null;
        this.mllpEventType = null;
        this.mllpTriggerEvent = null;
        this.mllpMessageControlId = null;
        this.mllpProcessingId = null;
        this.mllpVersionId = null;
        this.mllpCharSet = null;
        this.notes = new HashMap<>();
    }

    //
    // Getters and Setters
    //


    public String getMllpSendingFacility() {
        return mllpSendingFacility;
    }

    public void setMllpSendingFacility(String mllpSendingFacility) {
        this.mllpSendingFacility = mllpSendingFacility;
    }

    public String getMllpSendingApplication() {
        return mllpSendingApplication;
    }

    public void setMllpSendingApplication(String mllpSendingApplication) {
        this.mllpSendingApplication = mllpSendingApplication;
    }

    public UoW getUow() {
        return uow;
    }

    public void setUow(UoW uow) {
        this.uow = uow;
    }

    public String getMllpConnectionLocalAddress() {
        return mllpConnectionLocalAddress;
    }

    public void setMllpConnectionLocalAddress(String mllpConnectionLocalAddress) {
        this.mllpConnectionLocalAddress = mllpConnectionLocalAddress;
    }

    public String getMllpConnectionRemoteAddress() {
        return mllpConnectionRemoteAddress;
    }

    public void setMllpConnectionRemoteAddress(String mllpConnectionRemoteAddress) {
        this.mllpConnectionRemoteAddress = mllpConnectionRemoteAddress;
    }

    public String getMllpAcknowledgement() {
        return mllpAcknowledgement;
    }

    public void setMllpAcknowledgement(String mllpAcknowledgement) {
        this.mllpAcknowledgement = mllpAcknowledgement;
    }

    public String getMllpAcknowledgementType() {
        return mllpAcknowledgementType;
    }

    public void setMllpAcknowledgementType(String mllpAcknowledgementType) {
        this.mllpAcknowledgementType = mllpAcknowledgementType;
    }

    public String getMllpReceivingFacility() {
        return mllpReceivingFacility;
    }

    public void setMllpReceivingFacility(String mllpReceivingFacility) {
        this.mllpReceivingFacility = mllpReceivingFacility;
    }

    public String getMllpReceivingApplication() {
        return mllpReceivingApplication;
    }

    public void setMllpReceivingApplication(String mllpReceivingApplication) {
        this.mllpReceivingApplication = mllpReceivingApplication;
    }

    public String getMllpTimestamp() {
        return mllpTimestamp;
    }

    public void setMllpTimestamp(String mllpTimestamp) {
        this.mllpTimestamp = mllpTimestamp;
    }

    public String getMllpSecurity() {
        return mllpSecurity;
    }

    public void setMllpSecurity(String mllpSecurity) {
        this.mllpSecurity = mllpSecurity;
    }

    public String getMllpMessageType() {
        return mllpMessageType;
    }

    public void setMllpMessageType(String mllpMessageType) {
        this.mllpMessageType = mllpMessageType;
    }

    public String getMllpEventType() {
        return mllpEventType;
    }

    public void setMllpEventType(String mllpEventType) {
        this.mllpEventType = mllpEventType;
    }

    public String getMllpTriggerEvent() {
        return mllpTriggerEvent;
    }

    public void setMllpTriggerEvent(String mllpTriggerEvent) {
        this.mllpTriggerEvent = mllpTriggerEvent;
    }

    public String getMllpMessageControlId() {
        return mllpMessageControlId;
    }

    public void setMllpMessageControlId(String mllpMessageControlId) {
        this.mllpMessageControlId = mllpMessageControlId;
    }

    public String getMllpProcessingId() {
        return mllpProcessingId;
    }

    public void setMllpProcessingId(String mllpProcessingId) {
        this.mllpProcessingId = mllpProcessingId;
    }

    public String getMllpVersionId() {
        return mllpVersionId;
    }

    public void setMllpVersionId(String mllpVersionId) {
        this.mllpVersionId = mllpVersionId;
    }

    public String getMllpCharSet() {
        return mllpCharSet;
    }

    public void setMllpCharSet(String mllpCharSet) {
        this.mllpCharSet = mllpCharSet;
    }

    public Map<Integer, String> getNotes() {
        return notes;
    }

    public void setNotes(Map<Integer, String> notes) {
        this.notes = notes;
    }

    @JsonIgnore
    public void addNote(String note){
        int size = notes.size();
        if(StringUtils.isNotEmpty(note)){
            notes.put(size, note);
        }
    }

    @JsonIgnore
    public String getNote(Integer noteNumber){
        if(noteNumber == null){
            return(null);
        }
        if(noteNumber < 0){
            return(null);
        }
        if(notes.containsKey(noteNumber)){
            return(notes.get(noteNumber));
        }
        return(null);
    }

    @JsonIgnore
    public Integer getNoteCount(){
        return(notes.size());
    }

    //
    // toString
    //

    @Override
    public String toString() {
        return "MLLPMessageActivity{" +
                "uow=" + uow +
                ", mllpConnectionLocalAddress='" + mllpConnectionLocalAddress + '\'' +
                ", mllpConnectionRemoteAddress='" + mllpConnectionRemoteAddress + '\'' +
                ", mllpAcknowledgement='" + mllpAcknowledgement + '\'' +
                ", mllpAcknowledgementType='" + mllpAcknowledgementType + '\'' +
                ", mllpSendingFacility='" + mllpSendingFacility + '\'' +
                ", mllpSendingApplication='" + mllpSendingApplication + '\'' +
                ", mllpReceivingFacility='" + mllpReceivingFacility + '\'' +
                ", mllpReceivingApplication='" + mllpReceivingApplication + '\'' +
                ", mllpTimestamp='" + mllpTimestamp + '\'' +
                ", mllpSecurity='" + mllpSecurity + '\'' +
                ", mllpMessageType='" + mllpMessageType + '\'' +
                ", mllpEventType='" + mllpEventType + '\'' +
                ", mllpTriggerEvent='" + mllpTriggerEvent + '\'' +
                ", mllpMessageControlId='" + mllpMessageControlId + '\'' +
                ", mllpProcessingId='" + mllpProcessingId + '\'' +
                ", mllpVersionId='" + mllpVersionId + '\'' +
                ", mllpCharSet='" + mllpCharSet + '\'' +
                ", notes=" + notes +
                ", noteCount=" + getNoteCount() +
                '}';
    }
}
