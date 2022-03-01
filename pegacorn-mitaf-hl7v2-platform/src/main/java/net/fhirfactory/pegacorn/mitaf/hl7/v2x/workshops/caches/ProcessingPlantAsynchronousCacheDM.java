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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.caches;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ProcessingPlantAsynchronousCacheDM {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingPlantAsynchronousCacheDM.class);

    private final ConcurrentHashMap<String, String> asynchronousACKCache;

    public ProcessingPlantAsynchronousCacheDM() {
        asynchronousACKCache = new ConcurrentHashMap<>();
    }

    @Transactional
    public void addAckMessage(String messageControlId, String ackMessage) {
        LOG.debug(".addAckMessage(): Entry, messageControlId --> {}", messageControlId);

        if(asynchronousACKCache.containsKey(messageControlId)){
            asynchronousACKCache.remove(messageControlId);
        }
        asynchronousACKCache.put(messageControlId, ackMessage);
    }


    public String getAckMessage(String messageControlId) {
        LOG.debug(".getAckMessage(): Entry, messageControlId --> {}", messageControlId);

        return asynchronousACKCache.get(messageControlId);
    }

     @Transactional
    public void removeAckMessage(String messageControlId) {
        LOG.debug(".removeAckMessage(): Entry, messageControlId --> {}", messageControlId);

        if (messageControlId == null) {
            return;
        }
        if (asynchronousACKCache.containsKey(messageControlId)) {
            asynchronousACKCache.remove(messageControlId);
        }
    }
}
