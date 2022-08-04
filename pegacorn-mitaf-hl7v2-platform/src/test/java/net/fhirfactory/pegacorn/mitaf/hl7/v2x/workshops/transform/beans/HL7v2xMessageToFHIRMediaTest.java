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
import ca.uhn.hl7v2.parser.Parser;
import net.fhirfactory.pegacorn.core.constants.systemwide.DeploymentSystemSiteIdentificationInterface;
import net.fhirfactory.pegacorn.core.constants.systemwide.PegacornReferenceProperties;
import net.fhirfactory.pegacorn.internals.fhir.r4.codesystems.PegacornIdentifierCodeSystemFactory;
import net.fhirfactory.pegacorn.internals.fhir.r4.internal.systems.DeploymentInstanceDetailInterface;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.identifier.PegacornIdentifierFactory;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.media.factories.MediaFactory;
import net.fhirfactory.pegacorn.internals.hl7v2.helpers.HL7v2xMessageInformationExtractor;
import net.fhirfactory.pegacorn.internals.hl7v2.helpers.MediaPipeParser;
import net.fhirfactory.pegacorn.internals.hl7v2.helpers.UltraDefensivePipeParser;
import net.fhirfactory.pegacorn.internals.hl7v2.interfaces.HL7v2xInformationExtractionInterface;

public class HL7v2xMessageToFHIRMediaTest {
	
	private HL7v2xMessageToFHIRMedia converter;	
	private HL7v2xInformationExtractionInterface messageExtractor;		
	private PegacornReferenceProperties systemWideProperties;
	private PegacornIdentifierFactory identifierFactory;
	private PegacornIdentifierCodeSystemFactory pegacornIdentifierCodeSystemFactory;	
	private MediaFactory mediaFactory;
    private Parser pipeParser;
    private MediaPipeParser mediaParser;
    private UltraDefensivePipeParser udpp;
	
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
		mediaParser = new MediaPipeParser();
		udpp = new UltraDefensivePipeParser();
		mediaParser.setPipeParser(udpp);
		converter.setMediaParser(mediaParser);
	}
	
	@Test
	void testExtractMediaResource() {
		try {
			String resource = loadORUAttachmentResource();
			Media media = converter.extractNextMediaResource(resource);
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
			String resource = loadORUInlineResource();
			Media media = converter.extractNextMediaResource(resource);
			Assertions.assertNull(media);
		} catch (IOException e) {
			fail(e);
		}
	}
	
	
	@Test
	void testEncodedMediaResource() {
		try {
			String resource = loadORUAttachmentResource();
			Media media = converter.extractNextMediaResource(resource);
			Assertions.assertNotNull(media);
			Assertions.assertNotNull(media.getIdentifierFirstRep());
			Assertions.assertNotNull(media.getContent().getData());
			Assertions.assertNotEquals(0, media.getContent().getData().length);
			Assertions.assertEquals("application/pdf", media.getContent().getContentType());
		} catch (IOException e) {
			fail(e);
		}
	}
	
	@Test
	void testNonMediaResource() throws HL7Exception {
		try {
		String resource = loadADTResource();
		Media media = converter.extractNextMediaResource(resource);
		Assertions.assertNull(media);
		} catch (IOException e) {
			fail(e);
		}
	}
	
	private String loadORUAttachmentResource() throws IOException {
		Path filePath = Path.of("./src/test/resources/oru_r01_with_attachment.txt");
		return loadResource(filePath);
	}
	
	private String loadORUInlineResource() throws IOException {
		Path filePath = Path.of("./src/test/resources/oru_r01_with_inline.txt");
		return loadResource(filePath);
	}	
	
	private String loadADTResource() throws IOException {
		Path filePath = Path.of("./src/test/resources/adt_a01.txt");
		return loadResource(filePath);
	}
	
	private String loadResource(Path filePath) throws IOException {

		return Files.readString(filePath);
	}
}
