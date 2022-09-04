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
package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.wup;

import ca.uhn.hl7v2.AcknowledgmentCode;
import ca.uhn.hl7v2.ErrorCode;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.mllp.MLLPServerEndpoint;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.mllp.MLLPMessageIngresProcessor;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.capabilities.HL7v2xQueryCapabilityBroker;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.HL7v2xQueryCapturePostTransformResponse;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.HL7v2xQueryUnsupportedTriggerEvent;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.beans.triggerevents.HL7v2xTriggerEventIngresProcessor;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.FreeMarkerConfiguration;
import net.fhirfactory.pegacorn.petasos.core.moa.wup.MessageBasedWUPEndpointContainer;
import net.fhirfactory.pegacorn.petasos.wup.helper.IngresActivityRegistrationServices;
import org.apache.camel.ExchangePattern;
import org.apache.camel.component.hl7.HL7DataFormat;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.camel.component.hl7.HL7.ack;
import static org.apache.camel.support.builder.PredicateBuilder.and;

public abstract class BaseHL7v2QueryMessageServerWUP extends BaseHL7v2xMessageIngressWUP {

    private String WUP_VERSION="1.0.0";
    private String CAMEL_COMPONENT_TYPE="mllp";

    @Inject
    private FreeMarkerConfiguration freemarkerConfig;

    @Override
    protected String specifyWUPInstanceName() {
        return (this.getClass().getSimpleName());
    }

    @Override
    protected String specifyWUPInstanceVersion() {
        return (WUP_VERSION);
    }

    @Override
    public void configure() throws Exception {
        getLogger().info("{}:: ingresFeed() --> {}", getClass().getSimpleName(), ingresFeed());
        getLogger().info("{}:: egressFeed() --> {}", getClass().getSimpleName(), egressFeed());

        // This will make sure the file exists during app startup
        String ingresTransformFileName = System.getenv("TRANSFORMATION_CONFIG_FILE_LOCATION") + "/" + System.getenv("KUBERNETES_SERVICE_NAME") + "/" + System.getenv("KUBERNETES_SERVICE_NAME") + "-ingres-transformation-config.ftl";
        Path ingresTransformationFile = Paths.get(ingresTransformFileName);
        if (!Files.exists(ingresTransformationFile)) {
            throw new RuntimeException("Transformation file not found: " + ingresTransformFileName);
        }
        String egressTransformFileName = System.getenv("TRANSFORMATION_CONFIG_FILE_LOCATION") + "/" + System.getenv("KUBERNETES_SERVICE_NAME") + "/" + System.getenv("KUBERNETES_SERVICE_NAME") + "-egress-transformation-config.ftl";
        Path egressTransformFile = Paths.get(egressTransformFileName);
        if (!Files.exists(egressTransformFile)) {
            throw new RuntimeException("Transformation file not found: " + ingresTransformFileName);
        }
        
        HL7DataFormat hl7 = new HL7DataFormat();
        hl7.setValidate(false);

        fromInteractIngresService(ingresFeed())
                .routeId(getNameSet().getRouteCoreWUP())
                .bean(MLLPMessageIngresProcessor.class, "captureMLLPMessage(*, Exchange," + specifySourceSystem() +","+specifyIntendedTargetSystem()+","+specifyMessageDiscriminatorType()+","+specifyMessageDiscriminatorValue()+")")
                .bean(HL7v2xTriggerEventIngresProcessor.class, "encapsulateTriggerEvent(*, Exchange)")
                .bean(IngresActivityRegistrationServices.class, "registerQueryActivityStart(*,  Exchange)")
                .choice()
                    .when(and(header("CamelHL7MessageType").contains("QRY"),
                              header("CamelHL7TriggerEvent").contains("A19")))
                        .bean(freemarkerConfig,"configure(*, Exchange)")
                        .to("freemarker:file:" + ingresTransformFileName + "?contentCache=false&allowTemplateFromHeader=true&allowContextMapAll=true")
                        .bean(freemarkerConfig,"convertToMessage(*, Exchange)")
                        .bean(HL7v2xQueryCapabilityBroker.class, "processQueryTask")
                        .bean(freemarkerConfig,"configure(*, Exchange)")
                        .to("freemarker:file:" + egressTransformFileName + "?contentCache=false&allowTemplateFromHeader=true&allowContextMapAll=true")
                        .to(ExchangePattern.InOnly, forwardToEgressFeed())
                        .transform(ack())
                    .otherwise()
                        .to(ExchangePattern.InOnly,unsupportedTriggerEventTypeChannel())
                        .transform(ack(AcknowledgmentCode.AE, "Supports only QRY A19 messages to this port", ErrorCode.UNSUPPORTED_MESSAGE_TYPE))
                .end();

        from(unsupportedTriggerEventTypeChannel())
                .bean(HL7v2xQueryUnsupportedTriggerEvent.class, "articulateUnsupportedTrigger(*, Exchange)")
                .to(egressFeed());

        from(forwardToEgressFeed())
                .bean(HL7v2xQueryCapturePostTransformResponse.class, "capturePostTransformMessage(*,  Exchange)")
                .to(egressFeed());
    }

    protected String forwardToEgressFeed(){
        String endpointURL = "direct:"+getClass().getSimpleName()+"egressFeedContinuityChannel";
        return(endpointURL);
    }

    protected String unsupportedTriggerEventTypeChannel(){
        String endpointURL = "direct:"+getClass().getSimpleName()+"unsupportedTriggerEventChannel";
        return(endpointURL);
    }

    @Override
    protected MessageBasedWUPEndpointContainer specifyIngresEndpoint() {
        getLogger().debug(".specifyIngresEndpoint(): Entry, specifyIngresTopologyEndpointName()->{}", specifyIngresTopologyEndpointName());
        MessageBasedWUPEndpointContainer endpoint = new MessageBasedWUPEndpointContainer();
        MLLPServerEndpoint serverTopologyEndpoint = (MLLPServerEndpoint) getTopologyEndpoint(specifyIngresTopologyEndpointName());
        getLogger().trace(".specifyIngresEndpoint(): Retrieved serverTopologyEndpoint->{}", serverTopologyEndpoint);
        int portValue = serverTopologyEndpoint.getMLLPServerAdapter().getPortNumber();
        String interfaceDNSName = serverTopologyEndpoint.getMLLPServerAdapter().getHostName();
        endpoint.setEndpointSpecification(CAMEL_COMPONENT_TYPE+":"+interfaceDNSName+":"+Integer.toString(portValue)+"?requireEndOfData=false&autoAck=true");
        endpoint.setEndpointTopologyNode(serverTopologyEndpoint);
        endpoint.setFrameworkEnabled(false);
        getLogger().debug(".specifyIngresEndpoint(): Exit, endpoint->{}", endpoint);
        return (endpoint);
    }

    @Override
    protected String specifyMessageDiscriminatorType() {
        return null;
    }

    @Override
    protected String specifyMessageDiscriminatorValue() {
        return null;
    }
}
