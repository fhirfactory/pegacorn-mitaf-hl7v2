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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.mllp;

import ca.uhn.hl7v2.model.Message;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class MLLPSenderAckTimeoutDaemon {
    private static final Logger LOG = LoggerFactory.getLogger(MLLPSenderAckTimeoutDaemon.class);

    private ConcurrentHashMap<String, Timer> daemonMap;

    //
    // Constructor(s)
    //

    public MLLPSenderAckTimeoutDaemon(){
        this.daemonMap = new ConcurrentHashMap<>();
    }

    //
    // Getters and Setters
    //

    protected Logger getLogger(){
        return(LOG);
    }

    protected ConcurrentHashMap<String, Timer> getDaemonMap(){
        return(daemonMap);
    }

    //
    // Business Methods
    //

    public String startMLLPMessageMonitor(String payload, Exchange camelExchange, String routeId){

        return(payload);
    }

    public Message stopMLLPMessageMonitor(Message payload, Exchange camelExchange, String routeId){

        return(payload);
    }

    //
    // Monitor/Daemon
    //

    public void ackFailedToReceiveTask(String routeId, Exchange camelExchange){

    }
}
