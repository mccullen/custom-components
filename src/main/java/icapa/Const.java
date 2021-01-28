package icapa;

import org.apache.uima.fit.descriptor.ConfigurationParameter;

public class Const {
    // NOTE: Any time these headers are added, you should go to Util and update the following
    // - getOntologyConceptHeaders
    // - getOntologyAsStringArray
    // - getHeaderPropertiesWithDocumentIdOverrideo
    // - getInsertQuery
    // - getOntologiesFromIdentifiedAnnotation
    public static final String IDENTIFIED_ANNOTATION_ADDRESS_HEADER = "identified_annotation_address";
    public static final String CODE_HEADER = "code";
    public static final String CONDITIONAL_HEADER = "conditional_flag";
    public static final String CUI_HEADER = "umls_cui";
    public static final String DOCUMENT_ID_HEADER = "document_id";
    public static final String GENERIC_HEADER = "generic_flag";
    public static final String POLARITY_HEADER = "polarity";
    public static final String END_HEADER = "end_index";
    public static final String BEGIN_HEADER = "begin_index";
    public static final String PREFERRED_TEXT_HEADER = "preferred_text";
    public static final String REFSEM_HEADER = "refsem";
    public static final String SCHEME_HEADER = "coding_scheme";
    public static final String SCORE_HEADER = "score";
    public static final String SUBJECT_HEADER = "subject";
    public static final String TEXTSEM_HEADER = "textsem";
    public static final String TUI_HEADER = "umls_tui";
    public static final String UNCERTAINTY_HEADER = "uncertainty";
    public static final String TRUE_TEXT_HEADER = "true_text";
    public static final String PARTS_OF_SPEECH_HEADER = "parts_of_speech";
    public static final String ENTITY_TYPE_HEADER = "entity_type";
    public static final String SEGMENT_HEADER = "segment";
    public static final String DISCOVERY_TECHNIQUE_HEADER = "discovery_technique";
    public static final String HISTORY_OF_HEADER = "history_of";
    public static final String OID_HEADER = "oid";
    public static final String OUI_HEADER = "oui";
    public static final String DISAMBIGUATED_HEADER = "disambiguated_flag";
    public static final String ONTOLOGY_ADDRESS_HEADER = "ontology_concept_address";

    public static final String PARAM_DRIVER_CLASS = "DriverClassName";
    public static final String PARAM_DRIVER_CLASS_DESCRIPTION = "The full class name of the jdbc driver. Make sure that the driver is on the java CLASSPATH (I usually put it in CTAKES_HOME/lib)";
    public static final String PARAM_URL = "URL";
    public static final String PARAM_URL_DESCRIPTION = "The UTF-8 encoded url to use to hook up the jdbc driver specified by DriverClassName. Using an encoded url is useful if, for example, your url needs to contain equal to (=) signs. Since the equal to character is not allowed in configuration parameter values (it is a key character reserved for specifying parameter=value pairs), you can use %3D instead.";
    public static final String PARAM_USERNAME = "Username";
    public static final String PARAM_USERNAME_DESCRIPTION = "The username to use to log into the database. If not provided, the reader will attempt to connect using only the URL.";
    public static final String PARAM_PASSWORD = "Password";
    public static final String PARAM_PASSWORD_DESCRIPTION = "The password to use to log into the database. If not provided, the reader will attempt to connect using only the URL.";
    public static final String PARAM_KEEP_ALL = "KeepAll";
    public static final String PARAM_KEEP_ALL_DESCRIPTION = "True if you want to keep ALL IdentifiedAnnotations, including those that do not have any associated OntologyConcepts. If an IdentifiedAnnotation does not have any OntologyConcepts, then NULL will be inserted for each of the OntologyConcept features. If set to false, only IdentifiedAnnotations with OntologyConcepts will be inserted into the annotation Table. ";

    public static final int PRECISION = 5;
    public static final int SCALE = 2;
}
