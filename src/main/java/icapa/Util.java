package icapa;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import icapa.models.Ontology;
import org.apache.ctakes.core.cc.XMISerializer;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.ctakes.typesystem.type.syntax.ConllDependencyNode;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
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
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class Util {
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
}
