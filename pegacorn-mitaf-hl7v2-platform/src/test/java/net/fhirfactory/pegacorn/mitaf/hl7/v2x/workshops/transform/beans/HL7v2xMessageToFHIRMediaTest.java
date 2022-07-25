package net.fhirfactory.pegacorn.mitaf.hl7.v2x.workshops.transform.beans;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hl7.fhir.r4.model.Media;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import net.fhirfactory.pegacorn.core.constants.systemwide.DeploymentSystemSiteIdentificationInterface;
import net.fhirfactory.pegacorn.core.constants.systemwide.PegacornReferenceProperties;
import net.fhirfactory.pegacorn.internals.fhir.r4.codesystems.PegacornIdentifierCodeSystemFactory;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.systems.DeploymentInstanceDetailInterface;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.identifier.PegacornIdentifierFactory;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.media.factories.MediaFactory;
import net.fhirfactory.pegacorn.internals.hl7v2.helpers.HL7v2xMessageInformationExtractor;
import net.fhirfactory.pegacorn.internals.hl7v2.interfaces.HL7v2xInformationExtractionInterface;

public class HL7v2xMessageToFHIRMediaTest {
	
	private HL7v2xMessageToFHIRMedia converter;	
	private HL7v2xInformationExtractionInterface messageExtractor;		
	private PegacornReferenceProperties systemWideProperties;
	private PegacornIdentifierFactory identifierFactory;
	private PegacornIdentifierCodeSystemFactory pegacornIdentifierCodeSystemFactory;	
	private MediaFactory mediaFactory;
    private Parser pipeParser;
	
	@SuppressWarnings("resource")
	@BeforeEach
	public void setup() {
		pipeParser = new DefaultHapiContext().getPipeParser();
        pipeParser.getParserConfiguration().setValidating(false);
        pipeParser.getParserConfiguration().setEncodeEmptyMandatoryFirstSegments(true);

		converter = new HL7v2xMessageToFHIRMedia();
		messageExtractor = new HL7v2xMessageInformationExtractor();
		converter.setMessageInformationExtractionInterface(messageExtractor );
		mediaFactory = new MediaFactory();
		identifierFactory = new PegacornIdentifierFactory();
		pegacornIdentifierCodeSystemFactory = new PegacornIdentifierCodeSystemFactory();
		systemWideProperties = new PegacornReferenceProperties();
		pegacornIdentifierCodeSystemFactory.setSystemWideProperties(systemWideProperties);
		identifierFactory.setPegacornIdentifierCodeSystemFactory(pegacornIdentifierCodeSystemFactory);
		mediaFactory.setIdentifierFactory(identifierFactory);
		converter.setMediaFactory(mediaFactory );
		identifierFactory.setSiteIdentification(new DeploymentSystemSiteIdentificationInterface() { @Override public String getDeploymentSite() { return ""; } } );
		identifierFactory.setPegacornReferenceProperties(systemWideProperties);
		DeploymentInstanceDetailInterface deploymentDetails = new DeploymentInstanceDetailInterface() {		
			@Override
			public Reference getDeploymentInstanceSystemOwnerPractitionerRole() { return null; }
			@Override
			public Reference getDeploymentInstanceSystemOwnerPractitioner() { return null; }
			@Override
			public Reference getDeploymentInstanceSystemOwnerOrganization() { return null; }
			@Override
			public String getDeploymentInstanceSystemOwnerContactName() { return null; }
			@Override
			public String getDeploymentInstanceSystemEndpointSystem() { return null; }
			@Override
			public Reference getDeploymentInstanceSystemAdministratorPractitionerRole() { return null; }
			@Override
			public Reference getDeploymentInstanceSystemAdministratorPractitioner() { return null; }
			@Override
			public String getDeploymentInstanceSystemAdministratorContactName() { return null; }
			@Override
			public String getDeploymentInstanceOrganizationName() { return null; }
			@Override
			public Reference getDeploymentInstanceOrganization() {return null;}
		};
		identifierFactory.setDeploymentInstanceDetailInterface(deploymentDetails );
	}
	
	@Test
	void testExtractMediaResource() {
		try {
			Message resource = loadORUAttachmentResource();
			Media media = converter.extractMediaResource(resource);
			Assertions.assertNotNull(media);
			Assertions.assertNotNull(media.getIdentifierFirstRep());
			Assertions.assertNotNull(media.getContent().getData());
		} catch (IOException e) {
			fail(e);
		}
	}
	
	
	@Test
	void testExtractMediaResourceNoBase64() {
		try {
			Message resource = loadORUInlineResource();
			Media media = converter.extractMediaResource(resource);
			Assertions.assertNull(media);
		} catch (IOException e) {
			fail(e);
		}
	}
	
	
	@Test
	void testEncodedMediaResource() throws HL7Exception {
		try {
			Message resource = loadORUAttachmentResource();
			Media media = converter.extractNextMediaResource(resource.encode());
			Assertions.assertNotNull(media);
			Assertions.assertNotNull(media.getIdentifierFirstRep());
			Assertions.assertNotNull(media.getContent().getData());
		} catch (IOException e) {
			fail(e);
		}
	}
	
	@Test
	void testNonMediaResource() {
		try {
		Message resource = loadADTResource();
		Media media = converter.extractMediaResource(resource);
		Assertions.assertNull(media);
		} catch (IOException e) {
			fail(e);
		}
	}
	
	private Message loadORUAttachmentResource() throws IOException {
		Path filePath = Path.of("./src/test/resources/oru_r01_with_attachment.txt");
		return loadResource(filePath);
	}
	
	private Message loadORUInlineResource() throws IOException {
		Path filePath = Path.of("./src/test/resources/oru_r01_with_inline.txt");
		return loadResource(filePath);
	}	
	
	private Message loadADTResource() throws IOException {
		Path filePath = Path.of("./src/test/resources/adt_a01.txt");
		return loadResource(filePath);
	}
	
	private Message loadResource(Path filePath) throws IOException {

		String content = Files.readString(filePath);
		Message hl7Msg;
		try {
			hl7Msg = pipeParser.parse(content);
			return hl7Msg;
		} catch (HL7Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
