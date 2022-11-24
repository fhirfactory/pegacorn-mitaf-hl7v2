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
import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import net.fhirfactory.pegacorn.core.constants.subsystems.MLLPComponentConfigurationConstantsEnum;
import net.fhirfactory.pegacorn.core.interfaces.tasks.PetasosTaskLifetimeExtensionInterface;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class MLLPSenderAckTimeoutDaemon {
    private static final Logger LOG = LoggerFactory.getLogger(MLLPSenderAckTimeoutDaemon.class);

    private static final Integer MAX_RETRY_COUNT = 10;

    private ConcurrentHashMap<String, Timer> daemonMap;
    private ConcurrentHashMap<String, Long> mllpSenderAckTimeoutDuration;
    private Long ackWaitDuration;

    @Produce
    private ProducerTemplate camelProducerService;

    @Inject
    private PetasosTaskLifetimeExtensionInterface taskLifetimeExtension;

    //
    // Constructor(s)
    //

    public MLLPSenderAckTimeoutDaemon(){
        this.daemonMap = new ConcurrentHashMap<>();
        this.mllpSenderAckTimeoutDuration = new ConcurrentHashMap<>();
        this.ackWaitDuration = Long.valueOf(MLLPComponentConfigurationConstantsEnum.CAMEL_MLLP_ACKNOWLEDGEMENT_TIMEOUT.getDefaultValue());
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

    protected ProducerTemplate getCamelProducerService(){
        return(camelProducerService);
    }


    //
    // Business Methods
    //

    public String startMLLPMessageMonitor(String payload, Exchange camelExchange, String routeId, String timerName, String duration){
        getLogger().debug(".startMLLPMessageMonitor(): Entry, routeId->{}, timerName->{}, duration->{}", routeId, timerName, duration);

        getLogger().trace(".startMLLPMessageMonitor(): [Derive Acknowledge Maximum Wait Duration] Start");
        try{
            if(StringUtils.isNotEmpty(duration)){
                Long ackWaitDurationFromConfigFile = Long.valueOf(duration);
                if(ackWaitDurationFromConfigFile >= 0){
                    ackWaitDuration = ackWaitDurationFromConfigFile;
                }
            }
        } catch( Exception ex){
            ackWaitDuration = Long.valueOf(MLLPComponentConfigurationConstantsEnum.CAMEL_MLLP_ACKNOWLEDGEMENT_TIMEOUT.getDefaultValue());
        }
        getLogger().trace(".startMLLPMessageMonitor(): [Derive Acknowledge Maximum Wait Duration] Finish, ackWaitDuration->{}", ackWaitDuration);

        getLogger().trace(".startMLLPMessageMonitor(): [Create AckFailedToReceive Task] Start");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ackFailedToReceiveTask(routeId, timerName, camelExchange);
            }
        };
        getLogger().trace(".startMLLPMessageMonitor(): [Create AckFailedToReceive Task] Finish");

        getLogger().trace(".startMLLPMessageMonitor(): [Create Timer] Start, timerName->{}", timerName);
        Timer timer = new Timer(timerName);
        getLogger().trace(".startMLLPMessageMonitor(): [Create Timer] Finish");

        getLogger().trace(".startMLLPMessageMonitor(): [Schedule Task] Start");
        timer.schedule(task, ackWaitDuration);
        getLogger().trace(".startMLLPMessageMonitor(): [Schedule Task] Finish");

        getLogger().trace(".startMLLPMessageMonitor(): [Register Timer/Task In HashMap] Start");
        getDaemonMap().put(timerName, timer);
        getLogger().trace(".startMLLPMessageMonitor(): [Register Timer/Task In HashMap] Finish");

        getLogger().debug(".startMLLPMessageMonitor(): Exit, returning to route");
        return(payload);
    }

    public Message stopMLLPMessageMonitor(Message payload, Exchange camelExchange, String routeId, String timerName, String duration){
        getLogger().debug(".stopMLLPMessageMonitor(): Entry, routeId->{}, timerName->{}, duration->{}", routeId, timerName, duration);

        getLogger().trace(".stopMLLPMessageMonitor(): [Retrieve Timer/Task From HashMap] Start");
        Timer timer = getDaemonMap().get(timerName);
        getLogger().trace(".stopMLLPMessageMonitor(): [Retrieve Timer/Task From HashMap] Finish, timer->{}", timer);

        getLogger().trace(".stopMLLPMessageMonitor(): [Clear Timer/Task & remove from HashMap] Start");
        if(timer != null){
            timer.cancel();
            getDaemonMap().remove(timerName);
        }
        getLogger().trace(".stopMLLPMessageMonitor(): [Clear Timer/Task & remove from HashMap] Finish");

        getLogger().debug(".stopMLLPMessageMonitor(): Exit, returning to route");
        return(payload);
    }

    //
    // Monitor/Daemon
    //

    public void ackFailedToReceiveTask(String routeId, String timerName, Exchange camelExchange)  {
        Timer timer = getDaemonMap().get(timerName);
        if(timer != null){
            timer.cancel();
            getDaemonMap().remove(timerName);
        }
        getLogger().trace(".ackFailedToReceiveTask(): [Fail the Route] Start");
        getCamelProducerService().sendBody("controlbus:route?"+routeId+"&action=fail");
        getLogger().trace(".ackFailedToReceiveTask(): [Fail the Route] Finish");
    }
}
