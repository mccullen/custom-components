package icapa;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.opencsv.CSVWriterBuilder;
import icapa.models.HeaderProperties;
import icapa.models.Ontology;
import icapa.services.AnalysisEngine;
import icapa.services.CollectionReader;
import org.apache.ctakes.core.cc.XMISerializer;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.refsem.OntologyConcept;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.ctakes.typesystem.type.syntax.ConllDependencyNode;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.log4j.Logger;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Util {
    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());
    public static String getXmi(CAS cas) {
        String xmiString = "";
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XmiCasSerializer casSerializer = new XmiCasSerializer(cas.getTypeSystem());
            XMISerializer xmiSerializer = new XMISerializer(outputStream);
            casSerializer.serialize(cas, xmiSerializer.getContentHandler());
            xmiString = new String(outputStream.toByteArray(), Charset.defaultCharset());
        } catch (IOException | SAXException e) {
            LOGGER.error("Error getting xmi as string", e);
        }
        return xmiString;
    }

    public static <K> Map<K, Integer> getKeyToIndex(K[] keys) {
        Map<K, Integer> result = new HashMap<>();
        for (int i = 0; i < keys.length; ++i) {
            result.put(keys[i], i);
        }
        return result;
    }

    public static String[] getOntologyConceptHeaders() {
        return new String[]{
            Const.DOCUMENT_ID_HEADER,

            // Addresses
            Const.IDENTIFIED_ANNOTATION_ADDRESS_HEADER,
            Const.ONTOLOGY_ADDRESS_HEADER,

            Const.BEGIN_HEADER,
            Const.END_HEADER,

            // Sems
            Const.TEXTSEM_HEADER,
            Const.REFSEM_HEADER,

            // Flags
            Const.CONDITIONAL_HEADER,
            Const.GENERIC_HEADER,
            Const.DISAMBIGUATED_HEADER,

            // umls cui/tui
            Const.CUI_HEADER,
            Const.TUI_HEADER,

            // text
            Const.TRUE_TEXT_HEADER,
            Const.PREFERRED_TEXT_HEADER,

            Const.CODE_HEADER,
            Const.POLARITY_HEADER,
            Const.PARTS_OF_SPEECH_HEADER,
            Const.SCHEME_HEADER,
            Const.SCORE_HEADER,
            Const.SUBJECT_HEADER,
            Const.UNCERTAINTY_HEADER,
            Const.ENTITY_TYPE_HEADER,
            Const.SEGMENT_HEADER,
            Const.DISCOVERY_TECHNIQUE_HEADER,
            Const.HISTORY_OF_HEADER,
            Const.OID_HEADER,
            Const.OUI_HEADER,
        };
    }

    public static Map<String, Integer> getOntologyHeaderToIndex() {
        String[] headers = Util.getOntologyConceptHeaders();
        return getKeyToIndex(headers);
    }

    public static String getFeatureString(FeatureStructure fs, String featureName) {
        Feature feature = fs.getType().getFeatureByBaseName(featureName);
        String featureString = fs.getStringValue(feature);
        return featureString;
    }

    public static double getFeatureDouble(FeatureStructure fs, String featureName) {
        Feature feature = fs.getType().getFeatureByBaseName(featureName);
        double featureString = fs.getDoubleValue(feature);
        return featureString;
    }

    public static int getFeatureInt(FeatureStructure fs, String featureName) {
        Feature feature = fs.getType().getFeatureByBaseName(featureName);
        int featureString = fs.getIntValue(feature);
        return featureString;
    }

    public static boolean getFeatureBoolean(FeatureStructure fs, String featureName) {
        Feature feature = fs.getType().getFeatureByBaseName(featureName);
        boolean featureString = fs.getBooleanValue(feature);
        return featureString;
    }

    public static long getRuntimeInMiliseconds(Runnable runnable) {
        long startTime = System.nanoTime();
        runnable.run();
        long endTime = System.nanoTime();
        long durationInMilliseconds = (endTime - startTime)/1000000;
        return durationInMilliseconds;
    }

    public static AmazonS3 getS3Client() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", "us-east-1"))
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("test", "test")));
        builder.setPathStyleAccessEnabled(true); // Need this to get it to find localhost for some reason...
        AmazonS3 s3Client = builder.build();
        return s3Client;
    }

    /**
     * Gets all the ontologies in the jCas
     * */
    public static List<Ontology> getOntologies(JCas jCas) {
        return Util.getOntologies(jCas, true);
    }

    public static List<Ontology> getOntologies(JCas jCas, boolean keepAll) {
        List<Ontology> ontologies = new ArrayList<>();
        // Identified annotations: Pretty much anything with a begin and end position. May or may not include an ontologyArrConcept
        String documentId = getDocumentId(jCas); // Get the documentId here so you don't have to keep parsing the jcas to get it.
        Collection<IdentifiedAnnotation> identifiedAnnotations = JCasUtil.select(jCas, IdentifiedAnnotation.class);
        for (IdentifiedAnnotation identifiedAnnotation : identifiedAnnotations) {
            if (Util.isKeepAllOrHasOntologyConcepts(keepAll, identifiedAnnotation)) {
                List<Ontology> subset = getOntologiesFromIdentifiedAnnotation(jCas, identifiedAnnotation, documentId);
                ontologies.addAll(subset);
            }
        }
        return ontologies;
    }

    private static boolean isKeepAllOrHasOntologyConcepts(boolean keepAll, IdentifiedAnnotation identifiedAnnotation) {
        return keepAll || (identifiedAnnotation.getOntologyConceptArr() != null && identifiedAnnotation.getOntologyConceptArr().size() > 0);
    }

    /**
     * There should only be one DocumentID annotation in the jCas so this just gets the first one and returns it.
     * If there is more than one, only the first will be returned. If none is found, and empty string is returned.
     * */
    public static String getDocumentId(JCas jCas) {
        String result = "";
        Collection<DocumentID> documentIds = JCasUtil.select(jCas, DocumentID.class);
        if (documentIds != null && documentIds.size() > 0) {
            DocumentID documentId = documentIds.iterator().next();
            result = documentId.getDocumentID();
        }
        return result;
    }

    private static List<Ontology> getOntologiesFromIdentifiedAnnotation(JCas jCas, IdentifiedAnnotation identifiedAnnotation, String documentId) {
        List<Ontology> ontologies = new ArrayList<>();
        // TODO: If you want to set condition for which identity annotations to add
        Ontology ontology = new Ontology();
        ontology.setDocumentId(documentId);// Set document id beforehand for efficiency
        setOntologyProperties(jCas, identifiedAnnotation, ontology);
        ontologies.add(ontology);
        return ontologies;
    }

    private static void setOntologyProperties(JCas jCas, IdentifiedAnnotation identifiedAnnotation, Ontology ontology) {
        //OntologyConceptUtil.getAnnotationsByCode

        // Only add things w/ ontology concept
        ontology.setIdentifiedAnnotationAddress(identifiedAnnotation.getAddress()); // The id of the identified annotation (xmi:id in the xmi file)
        ontology.setSubject(identifiedAnnotation.getSubject());
        ontology.setBegin(identifiedAnnotation.getBegin());
        ontology.setEnd(identifiedAnnotation.getEnd());
        ontology.setConditional(identifiedAnnotation.getConditional());
        ontology.setHistoryOf(identifiedAnnotation.getHistoryOf());
        ontology.setGeneric(identifiedAnnotation.getGeneric());
        ontology.setPolarity(identifiedAnnotation.getPolarity());
        ontology.setTextsem(identifiedAnnotation.getType().toString());
        ontology.setUncertainty(identifiedAnnotation.getUncertainty());
        ontology.setEntityType(Util.getEntityTypeFromId(identifiedAnnotation.getTypeID()));
        ontology.setDiscoveryTechnique(Util.getDiscoveryTechniqueFromId(identifiedAnnotation.getDiscoveryTechnique()));
        String trueText = jCas.getDocumentText().substring(ontology.getBegin(), ontology.getEnd());
        ontology.setTrueText(trueText);
        // Get Connl to set parts of speech
        //Collection<ConllDependencyNode> conllDependencyNodes = JCasUtil.select(jCas, ConllDependencyNode.class);
        List<ConllDependencyNode> conllDependencyNodes = JCasUtil.select(jCas, ConllDependencyNode.class)
            .stream()
            .filter(node -> ontology.getBegin() <= node.getBegin() &&  node.getEnd() <= ontology.getEnd())
            .collect(Collectors.toList());

        String trueTextFromConll = conllDependencyNodes.stream().map(n -> n.getForm()).collect(Collectors.joining(" "));
        String partsOfSpeech = conllDependencyNodes.stream().map(n -> n.getPostag()).collect(Collectors.joining(","));
        ontology.setPartsOfSpeech(partsOfSpeech);

        // Set segment
        String segmentId = identifiedAnnotation.getSegmentID();
        if (segmentId != null) {
            Collection<Segment> segments = JCasUtil.select(jCas, Segment.class);
            for (Segment segment : segments) {
                if (segmentId.equals(segment.getId())) {
                    ontology.setSegment(segment.getPreferredText());
                    break; // Meh. Don't like the break here. May be better to do an old-fashioned Iterator, but that would be ugly too
                }
            }
        }

        FSArray ontologyConceptArr = identifiedAnnotation.getOntologyConceptArr();
        if (ontologyConceptArr != null) {

            FeatureStructure[] ontologyFeatureStructures = ontologyConceptArr.toArray();

            Arrays.stream(ontologyFeatureStructures).forEach(ontologyFeatureStructure -> {
                if (ontologyFeatureStructure instanceof OntologyConcept) {
                    OntologyConcept ontologyConcept = (OntologyConcept)ontologyFeatureStructure;
                    ontology.setOntologyConceptAddress(ontologyConcept.getAddress());

                    // Get ontology concepts
                    // Add everything into an ontology
                    ontology.setCode(ontologyConcept.getCode());
                    String refsem = ontologyFeatureStructure.getType().toString();
                    ontology.setRefsem(refsem);
                    ontology.setCodingScheme(ontologyConcept.getCodingScheme());
                    ontology.setScore(ontologyConcept.getScore());
                    ontology.setOid(ontologyConcept.getOid());
                    ontology.setOui(ontologyConcept.getOui());
                    ontology.setDisambiguated(ontologyConcept.getDisambiguated());
                    if (ontologyConcept instanceof UmlsConcept) {
                        // Add umls concept properties if present
                        UmlsConcept umlsConcept = (UmlsConcept)ontologyConcept;
                        ontology.setCui(umlsConcept.getCui());
                        ontology.setPreferredText(umlsConcept.getPreferredText());
                        ontology.setTui(umlsConcept.getTui());
                    }

                }
            });
        }
    }

    public static String getEntityTypeFromId(int id) {
        String type = "";
        switch (id) {
            case CONST.NE_TYPE_ID_UNKNOWN:
                type = "unknown";
                break;
            case CONST.NE_TYPE_ID_DRUG:
                type = "drug";
                break;
            case CONST.NE_TYPE_ID_DISORDER:
                type = "disorder";
                break;
            case CONST.NE_TYPE_ID_FINDING:
                type = "finding";
                break;
            case CONST.NE_TYPE_ID_PROCEDURE:
                type = "procedure";
                break;
            case CONST.NE_TYPE_ID_ANATOMICAL_SITE:
                type = "anatomical site";
                break;
            case CONST.NE_TYPE_ID_CLINICAL_ATTRIBUTE:
                type = "clinical attribute";
                break;
            case CONST.NE_TYPE_ID_DEVICE:
                type = "device";
                break;
            case CONST.NE_TYPE_ID_LAB:
                type = "lab";
                break;
            case CONST.NE_TYPE_ID_PHENOMENA:
                type = "phenomena";
                break;
            default:
                type = String.valueOf(id);
                break;
        }
        return type;
    }

    public static String getDiscoveryTechniqueFromId(int id) {
        String technique = "";
        switch (id) {
            case CONST.NE_DISCOVERY_TECH_DICT_LOOKUP:
                technique = "dict lookup";
                break;
            case CONST.NE_DISCOVERY_TECH_GOLD_ANNOTATION:
                technique = "gold annotation";
                break;
            default:
                technique = String.valueOf(id);
                break;
        }
        return technique;
    }

    /**
     * Get a jdbc connection from just a url (no username/pw)
     * */
    public static Connection getConnection(String url) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url);
        } catch (Exception e) {
            LOGGER.error("Could not connect to url: " + url, e);
        }
        return connection;
    }

    /**
     * Get jdbc connection using url,username, and pw
     * */
    public static Connection getConnection(String username, String password, String url) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            LOGGER.error("Could not connect to url: " + url , e);
        }
        return connection;
    }

    /**
     * Load the jdbc driver. This needs to be done before getting the connection.
     * */
    public static void loadDriver(String driverClassName) {
        LOGGER.info("Loading jdbc driver");
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Error loading driver: ", e);
        }
    }

    /**
     * Decode the url using UTF-8
     * */
    public static String decodeUrl(String url) {
        String newUrl = "";
        try {
            newUrl = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Error decoding url + " + url, e);
        }
        return newUrl;
    }

    /**
     * Get the ontology object as a string array. headerToIndex is a mapping of the header name (ex: 'cui', etc.)
     * to the index its value should be in the resulting array.
     * */
    public static String[] getOntologyAsStringArray(Ontology ontology, Map<String, Integer> headerToIndex) {
        String[] row = new String[headerToIndex.size()];
        putInRow(row, Const.IDENTIFIED_ANNOTATION_ADDRESS_HEADER, String.valueOf(ontology.getIdentifiedAnnotationAddress()), headerToIndex);
        putInRow(row, Const.CODE_HEADER, ontology.getCode(), headerToIndex);
        putInRow(row, Const.CONDITIONAL_HEADER, String.valueOf(ontology.getConditional()), headerToIndex);
        putInRow(row, Const.CUI_HEADER, ontology.getCui(), headerToIndex);
        putInRow(row, Const.GENERIC_HEADER, String.valueOf(ontology.getGeneric()), headerToIndex);
        putInRow(row, Const.POLARITY_HEADER, String.valueOf(ontology.getPolarity()), headerToIndex);
        putInRow(row, Const.END_HEADER, String.valueOf(ontology.getEnd()), headerToIndex);
        putInRow(row, Const.BEGIN_HEADER, String.valueOf(ontology.getBegin()), headerToIndex);
        putInRow(row, Const.PREFERRED_TEXT_HEADER, ontology.getPreferredText(), headerToIndex);
        putInRow(row, Const.REFSEM_HEADER, ontology.getRefsem(), headerToIndex);
        putInRow(row, Const.SCHEME_HEADER, ontology.getCodingScheme(), headerToIndex);
        putInRow(row, Const.SCORE_HEADER, String.valueOf(ontology.getScore()), headerToIndex);
        putInRow(row, Const.SUBJECT_HEADER, ontology.getSubject(), headerToIndex);
        putInRow(row, Const.TEXTSEM_HEADER, ontology.getTextsem(), headerToIndex);
        putInRow(row, Const.TUI_HEADER, ontology.getTui(), headerToIndex);
        putInRow(row, Const.UNCERTAINTY_HEADER, String.valueOf(ontology.getUncertainty()), headerToIndex);
        putInRow(row, Const.TRUE_TEXT_HEADER, ontology.getTrueText(), headerToIndex);
        putInRow(row, Const.DOCUMENT_ID_HEADER, ontology.getDocumentId(), headerToIndex);
        putInRow(row, Const.PARTS_OF_SPEECH_HEADER, ontology.getPartsOfSpeech(), headerToIndex);
        putInRow(row, Const.ENTITY_TYPE_HEADER, ontology.getEntityType(), headerToIndex);
        putInRow(row, Const.SEGMENT_HEADER, ontology.getSegment(), headerToIndex);
        putInRow(row, Const.DISCOVERY_TECHNIQUE_HEADER, ontology.getDiscoveryTechnique(), headerToIndex);
        putInRow(row, Const.HISTORY_OF_HEADER, String.valueOf(ontology.getHistoryOf()), headerToIndex);
        putInRow(row, Const.OID_HEADER, ontology.getOid(), headerToIndex);
        putInRow(row, Const.OUI_HEADER, ontology.getOui(), headerToIndex);
        putInRow(row, Const.DISAMBIGUATED_HEADER, String.valueOf(ontology.getDisambiguated()), headerToIndex);
        putInRow(row, Const.ONTOLOGY_ADDRESS_HEADER, String.valueOf(ontology.getOntologyConceptAddress()), headerToIndex);
        return row;
    }

    private static void putInRow(String[] row, String header, String value, Map<String, Integer> headerToIndex) {
        String newValue = value == null || value.equals("null") ? "" : value;
        row[headerToIndex.get(header)] = newValue;
    }

    public static String wrapInSqlString(String[] row) {
        String result = "";
        for (int i = 0; i < row.length; ++i) {
            if (i == row.length - 1) {
                result += "'" + row[i] + "'";
            } else {
                result += "'" + row[i] + "',";
            }
        }
        return result;
    }

    public static List<HeaderProperties> getDefaultHeaderProperties() {
        return Util.getHeaderPropertiesWithDocumentIdOverride(null);
    }

    public static List<HeaderProperties> getHeaderPropertiesWithDocumentIdOverride(HeaderProperties documentIdOverride) {
        List<HeaderProperties> headerProperties = new ArrayList<>();
        String[] headers = Util.getOntologyConceptHeaders();
        for (int i = 0; i < headers.length; ++i) {
            HeaderProperties props = new HeaderProperties();
            String header = headers[i];
            if (documentIdOverride != null && header.equals(Const.DOCUMENT_ID_HEADER)) {
                // DocumentId override provided and you are on that column, so override that property
                props.setDataType(documentIdOverride.getDataType());
                props.setName(documentIdOverride.getName());
            } else {
                // Not documentId column or override not provided, so use default
                String dataType = "";
                switch (header) {
                    // Flags and ints
                    case Const.IDENTIFIED_ANNOTATION_ADDRESS_HEADER:
                    case Const.CONDITIONAL_HEADER: // Flag
                    case Const.GENERIC_HEADER: // Flag
                    case Const.POLARITY_HEADER:
                    case Const.END_HEADER:
                    case Const.BEGIN_HEADER:
                    case Const.UNCERTAINTY_HEADER:
                    case Const.HISTORY_OF_HEADER:
                    case Const.DISAMBIGUATED_HEADER: // Flag
                    case Const.ONTOLOGY_ADDRESS_HEADER:
                        dataType = "INT";
                        break;
                    // Numbers
                    case Const.SCORE_HEADER:
                        // Max 5 digits total; 2 digits stored to right of decimal point (so max of 3 will be to the left).
                        dataType = "DECIMAL(" + Const.PRECISION + ", " + Const.SCALE + ")";
                        break;
                    // Strings variables of various lengths
                    case Const.CUI_HEADER:
                    case Const.TUI_HEADER:
                    case Const.CODE_HEADER:
                    case Const.ENTITY_TYPE_HEADER:
                    case Const.DISCOVERY_TECHNIQUE_HEADER:
                        dataType = "VARCHAR(50)";
                        break;
                    case Const.REFSEM_HEADER:
                    case Const.TEXTSEM_HEADER:
                    case Const.SUBJECT_HEADER:
                    case Const.SCHEME_HEADER:
                        dataType = "VARCHAR(100)";
                        break;
                    // Preferred text can actually be pretty long sometimes, so give it more size than the default case
                    case Const.PREFERRED_TEXT_HEADER:
                    case Const.PARTS_OF_SPEECH_HEADER:
                    case Const.TRUE_TEXT_HEADER: // Just in case...
                        dataType = "VARCHAR(2000)";
                        break;
                    // Everything else (oid/oui)
                    default:
                        dataType = "VARCHAR(300)";
                        break;
                }
                props.setName(header);
                props.setDataType(dataType);
            }
            headerProperties.add(props);
        }
        return headerProperties;
    }

    /**
     * Returns a CREATE TABLE statment for ontology concepts using a given table name.
     * Note this will not add an identity column b/c this differs for each
     * database
     * */
    public static String getCreateTableQuery(String tableName) {
        List<HeaderProperties> headerProperties = Util.getDefaultHeaderProperties();
        String query = Util.getCreateTableQuery(tableName, headerProperties, "");
        return query;
    }

    public static String getCreateTableQuery(String tableName, String augment) {
        List<HeaderProperties> headerProperties = Util.getDefaultHeaderProperties();
        String query = Util.getCreateTableQuery(tableName, headerProperties, augment);
        return query;
    }

    public static String getCreateTableQuery(String tableName, String augment, HeaderProperties customDocumentId) {
        List<HeaderProperties> headerProperties = Util.getHeaderPropertiesWithDocumentIdOverride(customDocumentId);
        String query = Util.getCreateTableQuery(tableName, headerProperties, augment);
        return query;
    }

    /**
     * Returns a CREATE TABLE statement for ontology concepts using the given tableName and the header properties.
     * This is useful if you want to provide your own identity column by appending to the headerProperties list.
     * */
    private static String getCreateTableQuery(String tableName, List<HeaderProperties> headerProperties, String augment) {
        StringBuilder query = new StringBuilder("CREATE TABLE " + tableName + " (");
        for (int i = 0; i < headerProperties.size(); ++i) {
            HeaderProperties p = headerProperties.get(i);

            // Wrap in double quotes just in case there are any keywords in the header names
            query.append("\"").append(p.getName()).append("\" ").append(p.getDataType());
            if (i != headerProperties.size()-1) {
                // Only add comma for all entries except the last one
                query.append(", ");
            } else if (augment != null && !augment.equals("")) {
                // We are on the last entry and the user chose to augment the statement, so add the
                // comma in this case followed by whatever they wanted to augment to the CREATE statement
                query.append(", ").append(augment);
            }
        }
        query.append(");");
        return query.toString();
    }

    public static String getInsertTemplate(String tableName, List<HeaderProperties> headerProperties) {
        StringBuilder query = new StringBuilder("INSERT INTO " + tableName + " (");
        // Add column names
        for (int i = 0; i < headerProperties.size(); ++i) {
            HeaderProperties p = headerProperties.get(i);
            query.append("\"").append(p.getName()).append("\"");
            if (i != headerProperties.size()-1) {
                // Only add comma for all entries except the last one
                query.append(", ");
            }
        }
        query.append(") VALUES (");
        // Add placeholders
        for (int i = 0; i < headerProperties.size(); ++i) {
            query.append("?");
            if (i != headerProperties.size()-1) {
                // Only add comma for all entries except the last one
                query.append(", ");
            }
        }
        query.append(");");
        return query.toString();
    }

    public static String getInsertQuery(String tableName, Ontology ontology, HeaderProperties documentIdOverride) {
        StringBuilder query = new StringBuilder("INSERT INTO " + tableName + " (");
        List<HeaderProperties> headerProperties = Util.getHeaderPropertiesWithDocumentIdOverride(documentIdOverride);
        // Add column names
        //Map<String, Object> headerNameToValue = new HashMap<>();
        for (int i = 0; i < headerProperties.size(); ++i) {
            HeaderProperties p = headerProperties.get(i);
            query.append("\"").append(p.getName()).append("\"");
            if (i != headerProperties.size()-1) {
                // Only add comma for all entries except the last one
                query.append(", ");
            }
        }
        query.append(") VALUES (");
        for (int i = 0; i < headerProperties.size(); ++i) {
            HeaderProperties p = headerProperties.get(i);
            switch (p.getName()) {
                case Const.IDENTIFIED_ANNOTATION_ADDRESS_HEADER:
                    query.append(Util.getSqlString(ontology.getIdentifiedAnnotationAddress()));
                    break;
                case Const.CODE_HEADER:
                    query.append(Util.getSqlString(ontology.getCode()));
                    break;
                case Const.CONDITIONAL_HEADER:
                    query.append(Util.getSqlString(ontology.getConditional()));
                    break;
                case Const.CUI_HEADER:
                    query.append(Util.getSqlString(ontology.getCui()));
                    break;
                case Const.GENERIC_HEADER:
                    query.append(Util.getSqlString(ontology.getGeneric()));
                    break;
                case Const.POLARITY_HEADER:
                    query.append(Util.getSqlString(ontology.getPolarity()));
                    break;
                case Const.END_HEADER:
                    query.append(Util.getSqlString(ontology.getEnd()));
                    break;
                case Const.BEGIN_HEADER:
                    query.append(Util.getSqlString(ontology.getBegin()));
                    break;
                case Const.PREFERRED_TEXT_HEADER:
                    query.append(Util.getSqlString(ontology.getPreferredText()));
                    break;
                case Const.REFSEM_HEADER:
                    query.append(Util.getSqlString(ontology.getRefsem()));
                    break;
                case Const.SCHEME_HEADER:
                    query.append(Util.getSqlString(ontology.getCodingScheme()));
                    break;
                case Const.SCORE_HEADER:
                    query.append(Util.getSqlString(ontology.getScore()));
                    break;
                case Const.SUBJECT_HEADER:
                    query.append(Util.getSqlString(ontology.getSubject()));
                    break;
                case Const.TEXTSEM_HEADER:
                    query.append(Util.getSqlString(ontology.getTextsem()));
                    break;
                case Const.TUI_HEADER:
                    query.append(Util.getSqlString(ontology.getTui()));
                    break;
                case Const.UNCERTAINTY_HEADER:
                    query.append(Util.getSqlString(ontology.getUncertainty()));
                    break;
                case Const.TRUE_TEXT_HEADER:
                    query.append(Util.getSqlString(ontology.getTrueText()));
                    break;
                case Const.DOCUMENT_ID_HEADER:
                    query.append(Util.getSqlString(ontology.getDocumentId()));
                    break;
                case Const.PARTS_OF_SPEECH_HEADER:
                    query.append(Util.getSqlString(ontology.getPartsOfSpeech()));
                    break;
                case Const.ENTITY_TYPE_HEADER:
                    query.append(Util.getSqlString(ontology.getEntityType()));
                    break;
                case Const.SEGMENT_HEADER:
                    query.append(Util.getSqlString(ontology.getSegment()));
                    break;
                case Const.DISCOVERY_TECHNIQUE_HEADER:
                    query.append(Util.getSqlString(ontology.getDiscoveryTechnique()));
                    break;
                case Const.HISTORY_OF_HEADER:
                    query.append(Util.getSqlString(ontology.getHistoryOf()));
                    break;
                case Const.OID_HEADER:
                    query.append(Util.getSqlString(ontology.getOid()));
                    break;
                case Const.OUI_HEADER:
                    query.append(Util.getSqlString(ontology.getOui()));
                    break;
                case Const.DISAMBIGUATED_HEADER:
                    query.append(Util.getSqlString(ontology.getDisambiguated()));
                    break;
                case Const.ONTOLOGY_ADDRESS_HEADER:
                    query.append(Util.getSqlString(ontology.getOntologyConceptAddress()));
                    break;
                default: {
                    if (documentIdOverride != null &&
                            p.getName() != null &&
                            p.getName().equals(documentIdOverride.getName())) {
                        // special case for document id column
                        String datatype = documentIdOverride.getDataType().toLowerCase();
                        if (datatype.contains("int")) {
                            query.append(Util.getSqlString(Integer.valueOf(ontology.getDocumentId())));
                        } else if (datatype.contains("decimal") || datatype.contains("numeric")) {
                            query.append(Util.getSqlString(Double.valueOf(ontology.getDocumentId())));
                        } else {
                            query.append(Util.getSqlString(ontology.getDocumentId()));
                        }
                    }
                    break;
                }
            }
            if (i != headerProperties.size()-1) {
                // Only add comma for all entries except the last one
                query.append(", ");
            }
        }
        query.append(");");

        // Add values
        return query.toString();
    }

    /**
     * Get the string for a sql insert statement based on the type. For example, strings will be wrapped in single
     * quotes, booleans will be converted to integer flags, etc.
     * */
    public static <T> String getSqlString(T t) {
        String result = "";
        if (t instanceof String) {
            result = "'" + t + "'";
        } else if (t instanceof Boolean) {
            if ((Boolean)t) {
                result += 1;
            } else {
                result += 0;
            }
        } else if (t instanceof Double || t instanceof Float) {
            String f = "%" + Const.PRECISION + "." + Const.SCALE + "f";
            result += String.format(f, t);
        } else {
            result += t;
        }
        return result;
    }

    public static boolean nullOrEmpty(String s) {
        return s == null || s.equals("");
    }

    /**
     * Collection reader's destroy() doesn't actually get called for some reason. So, if the cr doesn't have any
     * more docs to process, then we will destroy it ourselves.
     * */
    public static boolean hasNext(CollectionReader cr) {
        boolean next = cr.hasNext();
        Logger log = Logger.getLogger(cr.getClass());
        log.info("Has next? " + next);
        if (!next) {
            cr.destroy();
        }
        return next;
    }

    public static void logExceptionChain(Logger logger, SQLException ex) {
        logger.error(ex);
        SQLException nextException = ex.getNextException();
        while (nextException != null) {
            logger.error(nextException);
            nextException = nextException.getNextException();
        }
    }
}
