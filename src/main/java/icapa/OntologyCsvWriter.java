package icapa;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import org.apache.ctakes.typesystem.type.refsem.OntologyConcept;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import sun.security.jca.JCAUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class OntologyCsvWriter extends JCasAnnotator_ImplBase {
    static public final String PARAM_OUTPUT_FILE = "OutputFile";
    @ConfigurationParameter(
        name = PARAM_OUTPUT_FILE,
        description = "Output file",
        defaultValue = "*",
        mandatory = true
    )
    private String _outputFile;

    private ICSVWriter _writer;
    private Map<String, Integer> _headerToIndex;

    public OntologyCsvWriter() {
    }

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        try {
            File file = new File(_outputFile);
            FileWriter fileWriter = new FileWriter(file);
            _writer = new CSVWriter(fileWriter);
            String[] headers = Util.getOntologyConceptHeaders();
            _headerToIndex = Util.getKeyToIndex(headers);
            _writer.writeNext(headers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        String documentId = getDocumentId(jCas);
        Collection<IdentifiedAnnotation> identifiedAnnotations = JCasUtil.select(jCas, IdentifiedAnnotation.class);
        for (IdentifiedAnnotation identifiedAnnotation : identifiedAnnotations) {

            String address = String.valueOf(identifiedAnnotation.getAddress()); // The id of the annotation (xmi:id in the xmi file)
            String subject = identifiedAnnotation.getSubject();
            String begin = String.valueOf(identifiedAnnotation.getBegin());
            String end = String.valueOf(identifiedAnnotation.getEnd());
            String conditional = String.valueOf(identifiedAnnotation.getConditional());
            String confidence = String.valueOf(identifiedAnnotation.getConfidence());
            String generic = String.valueOf(identifiedAnnotation.getGeneric());

            FSArray ontologyConceptArr = identifiedAnnotation.getOntologyConceptArr();
            if (ontologyConceptArr != null) {
                // Only add things w/ ontology concept
                FeatureStructure[] ontologyFeatureStructures = ontologyConceptArr.toArray();
                Arrays.stream(ontologyFeatureStructures).forEach(ontologyFeatureStructure -> {
                    // Get ontology concepts
                    String code = getFeatureString(ontologyFeatureStructure, "code");

                    // Add everything to row and write it out
                    String[] row = new String[_headerToIndex.size()];
                    putInRow(row, Const.POS_START_HEADER, begin);
                    putInRow(row, Const.POS_END_HEADER, end);
                    putInRow(row, Const.ADDRESS_HEADER, address);
                    putInRow(row, Const.CODE_HEADER, code);
                    putInRow(row, Const.CONDITIONAL_HEADER, conditional);
                    putInRow(row, Const.SUBJECT_HEADER, subject);
                    putInRow(row, Const.CONFIDENCE_HEADER, confidence);
                    putInRow(row, Const.GENERIC_HEADER, generic);

                    _writer.writeNext(row, false);
                    String[] line = MyCSVReader.currentLine;
                    System.out.println(line);
                });
            }
        }
    }

    private void putInRow(String[] row, String header, String value) {
        row[_headerToIndex.get(header)] = value;
    }

    private String getDocumentId(JCas jCas) {
        String result = "";
        Collection<DocumentID> documentIds = JCasUtil.select(jCas, DocumentID.class);
        if (documentIds != null && documentIds.size() > 0) {
            DocumentID documentId = documentIds.iterator().next();
            result = documentId.getDocumentID();
        }
        return result;
    }

    private String getFeatureString(FeatureStructure fs, String featureName) {
        Feature feature = fs.getType().getFeatureByBaseName(featureName);
        String featureString = fs.getStringValue(feature);
        return featureString;
    }


    @Override
    public void destroy() {
        super.destroy();
    }
}
