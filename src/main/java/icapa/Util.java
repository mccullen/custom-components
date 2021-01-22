package icapa;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import icapa.models.HeaderProperties;
import icapa.models.Ontology;
import org.apache.ctakes.core.cc.XMISerializer;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.ctakes.typesystem.type.syntax.ConllDependencyNode;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.log4j.Logger;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
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
            e.printStackTrace();
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
            Const.ADDRESS_HEADER,
            Const.CODE_HEADER,
            Const.CONDITIONAL_HEADER,
            Const.CONFIDENCE_HEADER,
            Const.CUI_HEADER,
            Const.GENERIC_HEADER,
            Const.POLARITY_HEADER,
            Const.PARTS_OF_SPEECH_HEADER,
            Const.PREFERRED_TEXT_HEADER,
            Const.REFSEM_HEADER,
            Const.SCHEME_HEADER,
            Const.SCORE_HEADER,
            Const.SUBJECT_HEADER,
            Const.TEXTSEM_HEADER,
            Const.TRUE_TEXT_HEADER,
            Const.TUI_HEADER,
            Const.UNCERTAINTY_HEADER,
            Const.DOCUMENT_ID,
            Const.END_HEADER,
            Const.BEGIN_HEADER
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
        builder.setPathStyleAccessEnabled(true);
        AmazonS3 s3Client = builder.build();
        return s3Client;
    }

    public static List<Ontology> getOntologies(JCas jCas) {
        List<Ontology> ontologies = new ArrayList<>();
        // Identified annotations: Pretty much anything with a begin and end position. May or may not include an ontologyArrConcept
        String documentId = getDocumentId(jCas);
        Collection<IdentifiedAnnotation> identifiedAnnotations = JCasUtil.select(jCas, IdentifiedAnnotation.class);
        for (IdentifiedAnnotation identifiedAnnotation : identifiedAnnotations) {
            List<Ontology> subset = getOntologiesFromIdentifiedAnnotation(jCas, identifiedAnnotation, documentId);
            ontologies.addAll(subset);
        }
        return ontologies;
        /*
        Collection<OntologyConcept> ontologies = JCasUtil.select(jCas, OntologyConcept.class);
        for (OntologyConcept ontology : ontologies) {
            ontology.
        }

         */
    }

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
        // Identified annotations can have arrays of ontology concepts, so extract them
        FSArray ontologyConceptArr = identifiedAnnotation.getOntologyConceptArr();
        if (ontologyConceptArr != null) {
            // Only add things w/ ontology concept
            int address = identifiedAnnotation.getAddress(); // The id of the annotation (xmi:id in the xmi file)
            String subject = identifiedAnnotation.getSubject();
            int begin = identifiedAnnotation.getBegin();
            int end = identifiedAnnotation.getEnd();
            boolean conditional = identifiedAnnotation.getConditional();
            //identifiedAnnotation.getHistoryOf() TODO
            float confidence = identifiedAnnotation.getConfidence();
            boolean generic = identifiedAnnotation.getGeneric();
            int polarity = identifiedAnnotation.getPolarity();
            String textsem = identifiedAnnotation.getType().toString();
            int uncertainty = identifiedAnnotation.getUncertainty();
            FeatureStructure[] ontologyFeatureStructures = ontologyConceptArr.toArray();

            Arrays.stream(ontologyFeatureStructures).forEach(ontologyFeatureStructure -> {
                // Get Connl
                List<ConllDependencyNode> conllDependencyNodes = JCasUtil.select(jCas, ConllDependencyNode.class)
                    .stream()
                    .filter(node -> begin <= node.getBegin() &&  node.getEnd() <= end)
                    .collect(Collectors.toList());

                //Collection<ConllDependencyNode> conllDependencyNodes = JCasUtil.select(jCas, ConllDependencyNode.class);
                String trueTextFromConll = conllDependencyNodes.stream().map(n -> n.getForm()).collect(Collectors.joining(" "));
                String partsOfSpeech = conllDependencyNodes.stream().map(n -> n.getPostag()).collect(Collectors.joining(","));
                String trueText = jCas.getDocumentText().substring(begin, end);
                // Get ontology concepts
                String code = Util.getFeatureString(ontologyFeatureStructure, "code");
                String cui = Util.getFeatureString(ontologyFeatureStructure, "cui");
                String preferredText = Util.getFeatureString(ontologyFeatureStructure, "preferredText");
                String refsem = ontologyFeatureStructure.getType().toString();
                String codingScheme = Util.getFeatureString(ontologyFeatureStructure, "codingScheme");
                double score = Util.getFeatureDouble(ontologyFeatureStructure, "score");
                // TODO: tui, cui, and preferred text only exist as UMLSConcepts, so be sure to do a check to ensure you are of that type
                String tui = Util.getFeatureString(ontologyFeatureStructure, "tui");

                // Add everything into an ontology
                Ontology ontology = new Ontology();
                ontology.setAddress(address);
                ontology.setCode(code);
                ontology.setConditional(conditional);
                ontology.setConfidence(confidence);
                ontology.setCui(cui);
                ontology.setGeneric(generic);
                ontology.setPolarity(polarity);
                ontology.setEnd(end);
                ontology.setBegin(begin);
                ontology.setPreferredText(preferredText);
                ontology.setRefsem(refsem);
                ontology.setCodingScheme(codingScheme);
                ontology.setScore(score);
                ontology.setSubject(subject);
                ontology.setTextsem(textsem);
                ontology.setTui(tui);
                ontology.setUncertainty(uncertainty);
                ontology.setTrueText(trueText);
                ontology.setDocumentId(documentId);
                ontology.setPartsOfSpeech(partsOfSpeech);

                ontologies.add(ontology);
            });
        }
        return ontologies;
    }

    public static Connection getConnection(String url, String driverClassName) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url);
        } catch (Exception e) {
            LOGGER.error("Could not connect to driver named " + driverClassName + " at " + url, e);
        }
        return connection;
    }

    public static Connection getConnection(String username, String password, String url, String driverClassName) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            LOGGER.error("Could not connect to driver named " + driverClassName + " at " + url , e);
        }
        return connection;
    }

    public static void loadDriver(String driverClassName) {
        LOGGER.info("Loading jdbc driver");
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Error loading driver: ", e);
        }
    }

    public static String decodeUrl(String url) {
        String newUrl = "";
        try {
            newUrl = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Error decoding url + " + url, e);
        }
        return newUrl;
    }

    public static String[] getOntologyAsStringArray(Ontology ontology, Map<String, Integer> headerToIndex) {
        String[] row = new String[headerToIndex.size()];
        putInRow(row, Const.ADDRESS_HEADER, String.valueOf(ontology.getAddress()), headerToIndex);
        putInRow(row, Const.CODE_HEADER, ontology.getCode(), headerToIndex);
        putInRow(row, Const.CONDITIONAL_HEADER, String.valueOf(ontology.isConditional()), headerToIndex);
        putInRow(row, Const.CONFIDENCE_HEADER, String.valueOf(ontology.getConfidence()), headerToIndex);
        putInRow(row, Const.CUI_HEADER, ontology.getCui(), headerToIndex);
        putInRow(row, Const.GENERIC_HEADER, String.valueOf(ontology.isGeneric()), headerToIndex);
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
        putInRow(row, Const.DOCUMENT_ID, ontology.getDocumentId(), headerToIndex);
        putInRow(row, Const.PARTS_OF_SPEECH_HEADER, ontology.getPartsOfSpeech(), headerToIndex);
        return row;
    }

    private static void putInRow(String[] row, String header, String value, Map<String, Integer> headerToIndex) {
        row[headerToIndex.get(header)] = value;
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
            if (documentIdOverride != null && header.equals(Const.DOCUMENT_ID)) {
                props.setDataType(documentIdOverride.getDataType());
                props.setName(documentIdOverride.getName());
            } else {
                String dataType = "";
                switch (header) {
                    case Const.ADDRESS_HEADER:
                    case Const.CONDITIONAL_HEADER: // Flag
                    case Const.GENERIC_HEADER: // Flag
                    case Const.POLARITY_HEADER:
                    case Const.END_HEADER:
                    case Const.BEGIN_HEADER:
                    case Const.UNCERTAINTY_HEADER:
                        dataType = "INT";
                        break;
                    case Const.CONFIDENCE_HEADER:
                    case Const.SCORE_HEADER:
                        // Max 5 digits total; 2 digits stored to right of decimal point (so max of 3 will be to the left).
                        dataType = "DECIMAL(" + Const.PRECISION + ", " + Const.SCALE + ")";
                        break;
                    default:
                        dataType = "VARCHAR(100)";
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
                case Const.ADDRESS_HEADER:
                    query.append(Util.getSqlString(ontology.getAddress()));
                    break;
                case Const.CODE_HEADER:
                    query.append(Util.getSqlString(ontology.getCode()));
                    break;
                case Const.CONDITIONAL_HEADER:
                    query.append(Util.getSqlString(ontology.isConditional()));
                    break;
                case Const.CONFIDENCE_HEADER:
                    query.append(Util.getSqlString(ontology.getConfidence()));
                    break;
                case Const.CUI_HEADER:
                    query.append(Util.getSqlString(ontology.getCui()));
                    break;
                case Const.GENERIC_HEADER:
                    query.append(Util.getSqlString(ontology.isGeneric()));
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
                case Const.DOCUMENT_ID:
                    query.append(Util.getSqlString(ontology.getDocumentId()));
                    break;
                case Const.PARTS_OF_SPEECH_HEADER:
                    query.append(Util.getSqlString(ontology.getPartsOfSpeech()));
                    break;
                default:
                    break;
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
}
