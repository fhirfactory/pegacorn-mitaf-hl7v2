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
package net.fhirfactory.pegacorn.mitaf.hl7.v24.interact.wup;

import static org.apache.camel.component.hl7.HL7.ack;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.apache.camel.component.hl7.HL7DataFormat;

import net.fhirfactory.pegacorn.core.model.topology.endpoints.mllp.MLLPServerEndpoint;
import net.fhirfactory.pegacorn.mitaf.hl7.v24.interact.beans.HL7v24TaskA19QueryClientHandler;
import net.fhirfactory.pegacorn.mitaf.hl7.v24.interact.beans.HL7v24UnsupportedInput;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.interact.wup.BaseHL7v2xMessageIngressWUP;
import net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans.message.transformation.FreeMarkerConfiguration;
import net.fhirfactory.pegacorn.petasos.core.moa.wup.MessageBasedWUPEndpointContainer;

public abstract class HL7v24MessageA19EnabledIngressWUP extends BaseHL7v2xMessageIngressWUP {

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
                .unmarshal(hl7)
                .log(LoggingLevel.INFO, "HL7v24MessageA19EnabledIngressWUP: Received -> ${body}")
                .bean(freemarkerConfig,"configure(*, Exchange)")
                .to("freemarker:file:" + ingresTransformFileName + "?contentCache=false&allowTemplateFromHeader=true&allowContextMapAll=true")
                .bean(freemarkerConfig,"convertToMessage(*, Exchange)")
                .choice()
                    .when(exchangeProperty(HL7v24UnsupportedInput.EXCHANGE_PROP_UNSUPPORTED_ERROR).isNull())
                        .log(LoggingLevel.DEBUG, "HL7v24MessageA19EnabledIngressWUP: Received Supported Query -> ${body}")
                        .bean(HL7v24TaskA19QueryClientHandler.class, "processA19Request")
                        .log(LoggingLevel.DEBUG, "HL7v24MessageA19EnabledIngressWUP: Post A19 Query Client Handler -> ${body}")
                        .bean(freemarkerConfig,"configure(*, Exchange)")
                        .to("freemarker:file:" + egressTransformFileName + "?contentCache=false&allowTemplateFromHeader=true&allowContextMapAll=true")
                        .bean(freemarkerConfig,"convertToMessage(*, Exchange)")
                        .marshal(hl7)
                        .log(LoggingLevel.INFO, "HL7v24MessageA19EnabledIngressWUP: Returning ACK -> ${body}")
                    .otherwise()
                        .log(LoggingLevel.DEBUG, "HL7v24MessageA19EnabledIngressWUP: Not a supported query after ingres transformation -> ${body}")
                        .bean(HL7v24UnsupportedInput.class, "buildUnsupportedNACK(*, Exchange)")
                        .marshal(hl7)
                        .log(LoggingLevel.INFO, "HL7v24MessageA19EnabledIngressWUP: Returning NACK -> ${body}")
                .end()
                .setProperty("CamelMllpAcknowledgementString", body());  // this property is used for what is sent back to the client, not the body
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
    protected String specifySourceSystem() {
        return null;
    }

    @Override
    protected String specifyIntendedTargetSystem() {
        return null;
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
