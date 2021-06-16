package net.fhirfactory.pegacorn.mitaf.hl7v2.platform.transform.engine.configuration;

/**
 * The allowed types of configuration.
 * 
 * @author Brendan Douglas
 *
 */
public enum ConfigurationType {
	UPDATE_HL7("configuration", "UpdateConfiguration", DefaultHL7UpdateConfiguration.class), // Updates to a HL7 message
	UPDATE_FHIR("configuration", "UpdateConfiguration", DefaultFHIRUpdateConfiguration.class), // Updates to a FHIR object
	HL7_TO_FHIR("configuration", "HL72FHIRConversionConfiguration"), 
	FHIR_TO_HL7("configuration", "FHIR2HL7ConversionConfiguration");

	private String endFolderName;
	private String filenameSuffix;
	private Class<?>defaultConfiguration;

	ConfigurationType(String endFolderName, String filenameSuffix, Class<?>defaultConfiguration) {
		this.endFolderName = endFolderName;
		this.filenameSuffix = filenameSuffix;
		this.defaultConfiguration = defaultConfiguration;
	}
	
	ConfigurationType(String endFolderName, String filenameSuffix) {
		this(endFolderName, filenameSuffix, null);
	}

	public String getEndPackageName() {
		return endFolderName;
	}

	public String getFilenameSuffix() {
		return filenameSuffix;
	}
	
	public Class<?>getDefaultConfiguration() {
		return defaultConfiguration;
	}
}
