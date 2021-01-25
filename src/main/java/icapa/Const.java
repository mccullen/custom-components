package icapa;

import org.apache.uima.fit.descriptor.ConfigurationParameter;

public class Const {
    // NOTE: Any time these headers are added, you should go to Util and update the following
    // - getOntologyConceptHeaders
    // - getOntologyAsStringArray
    // - getHeaderPropertiesWithDocumentIdOverrideo
    // - getInsertQuery
    // - getOntologiesFromIdentifiedAnnotation
    public static final String ADDRESS_HEADER = "identifiedAnnotationAddress";
    public static final String CODE_HEADER = "code";
    public static final String CONDITIONAL_HEADER = "conditionalFlag";
    public static final String CUI_HEADER = "umlsCui";
    public static final String DOCUMENT_ID = "documentId";
    public static final String GENERIC_HEADER = "genericFlag";
    public static final String POLARITY_HEADER = "polarity";
    public static final String END_HEADER = "endIndex";
    public static final String BEGIN_HEADER = "beginIndex";
    public static final String PREFERRED_TEXT_HEADER = "preferredText";
    public static final String REFSEM_HEADER = "refsem";
    public static final String SCHEME_HEADER = "codingScheme";
    public static final String SCORE_HEADER = "score";
    public static final String SUBJECT_HEADER = "subject";
    public static final String TEXTSEM_HEADER = "textsem";
    public static final String TUI_HEADER = "umlsTui";
    public static final String UNCERTAINTY_HEADER = "uncertainty";
    public static final String TRUE_TEXT_HEADER = "trueText";
    public static final String PARTS_OF_SPEECH_HEADER = "partsOfSpeech";
    public static final String ENTITY_TYPE_HEADER = "entityTypeId";
    public static final String SEGMENT_HEADER = "segment";
    public static final String DISCOVERY_TECHNIQUE_HEADER = "discoveryTechniqueId";
    public static final String HISTORY_OF_HEADER = "historyOf";
    public static final String OID_HEADER = "oid";
    public static final String OUI_HEADER = "oui";
    public static final String DISAMBIGUATED_HEADER = "disambiguatedFlag";
    public static final String ONTOLOGY_ADDRESS_HEADER = "ontologyConceptAddress";

    public static final String PARAM_DRIVER_CLASS = "DriverClassName";
    public static final String PARAM_DOCUMENT_ID_COLUMN = "DocumentIdColumnName";
    public static final String PARAM_URL = "URL";
    public static final String PARAM_USERNAME = "Username";
    public static final String PARAM_PASSWORD = "Password";

    public static final int PRECISION = 5;
    public static final int SCALE = 2;
}
