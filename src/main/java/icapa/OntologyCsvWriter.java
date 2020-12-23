package icapa;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.ctakes.typesystem.type.syntax.ConllDependencyNode;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            _writer.writeNext(headers, false);
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
            String polarity = String.valueOf(identifiedAnnotation.getPolarity());
            String textsem = identifiedAnnotation.getType().toString();

            FSArray ontologyConceptArr = identifiedAnnotation.getOntologyConceptArr();
            if (ontologyConceptArr != null) {
                // Only add things w/ ontology concept
                FeatureStructure[] ontologyFeatureStructures = ontologyConceptArr.toArray();
                Arrays.stream(ontologyFeatureStructures).forEach(ontologyFeatureStructure -> {
                    // Get ontology concepts
                    String code = getFeatureString(ontologyFeatureStructure, "code");
                    String cui = getFeatureString(ontologyFeatureStructure, "cui");
                    String preferredText = getFeatureString(ontologyFeatureStructure, "preferredText");
                    String refsem = ontologyFeatureStructure.getType().toString();
                    String codingScheme = getFeatureString(ontologyFeatureStructure, "codingScheme");
                    String score = getFeatureString(ontologyFeatureStructure, "score");
                    String tui = getFeatureString(ontologyFeatureStructure, "tui");
                    String uncertainty = getFeatureString(ontologyFeatureStructure, "uncertainty");

                    // Get Connl
                    List<ConllDependencyNode> conllDependencyNodes = JCasUtil.select(jCas, ConllDependencyNode.class)
                        .stream()
                        .filter(node -> Integer.parseInt(begin) <= node.getBegin() &&  node.getEnd() < Integer.parseInt(end))
                        .collect(Collectors.toList());
                    String trueText = conllDependencyNodes.stream().map(n -> n.getForm()).collect(Collectors.joining(" "));
                    String alt = jCas.getDocumentText().substring(Integer.parseInt(begin), Integer.parseInt(end));


                    // Add everything to row and write it out
                    String[] row = new String[_headerToIndex.size()];

                    putInRow(row, Const.ADDRESS_HEADER, address);
                    putInRow(row, Const.CODE_HEADER, code);
                    putInRow(row, Const.CONDITIONAL_HEADER, conditional);
                    putInRow(row, Const.CONFIDENCE_HEADER, confidence);
                    putInRow(row, Const.CUI_HEADER, cui);
                    putInRow(row, Const.GENERIC_HEADER, generic);
                    putInRow(row, Const.POLARITY_HEADER, polarity);
                    putInRow(row, Const.END_HEADER, end);
                    putInRow(row, Const.BEGIN_HEADER, begin);
                    putInRow(row, Const.PREFERRED_TEXT_HEADER, preferredText);
                    putInRow(row, Const.REFSEM_HEADER, refsem);
                    putInRow(row, Const.SCHEME_HEADER, codingScheme);
                    putInRow(row, Const.SCORE_HEADER, score);
                    putInRow(row, Const.SUBJECT_HEADER, subject);
                    putInRow(row, Const.TEXTSEM_HEADER, textsem);
                    putInRow(row, Const.TUI_HEADER, tui);
                    putInRow(row, Const.UNCERTAINTY_HEADER, uncertainty);


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
