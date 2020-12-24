package icapa.services;

import com.opencsv.CSVWriter;
import icapa.Const;
import icapa.Util;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.ctakes.typesystem.type.syntax.ConllDependencyNode;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OntologyCasConsumer implements CasConsumer {
    private CSVWriter _writer;
    private Map<String, Integer> _headerToIndex;
    public static OntologyCasConsumer from(Writer writer) {
        OntologyCasConsumer result = new OntologyCasConsumer();
        try {
            result._writer = new CSVWriter(writer);
            String[] headers = Util.getOntologyConceptHeaders();
            result._headerToIndex = Util.getKeyToIndex(headers);
            result._writer.writeNext(headers, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public void process(JCas jCas) {
        // TODO: Create a model object to hold all the features in a row to make this a lot cleaner
        String documentId = getDocumentId(jCas);
        Collection<IdentifiedAnnotation> identifiedAnnotations = JCasUtil.select(jCas, IdentifiedAnnotation.class);
        for (IdentifiedAnnotation identifiedAnnotation : identifiedAnnotations) {
            writeRow(jCas, identifiedAnnotation, documentId);
        }
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

    private void writeRow(JCas jCas, IdentifiedAnnotation identifiedAnnotation, String documentId) {
        FSArray ontologyConceptArr = identifiedAnnotation.getOntologyConceptArr();
        if (ontologyConceptArr != null) {
            // Only add things w/ ontology concept
            String address = String.valueOf(identifiedAnnotation.getAddress()); // The id of the annotation (xmi:id in the xmi file)
            String subject = identifiedAnnotation.getSubject();
            String begin = String.valueOf(identifiedAnnotation.getBegin());
            String end = String.valueOf(identifiedAnnotation.getEnd());
            String conditional = String.valueOf(identifiedAnnotation.getConditional());
            String confidence = String.valueOf(identifiedAnnotation.getConfidence());
            String generic = String.valueOf(identifiedAnnotation.getGeneric());
            String polarity = String.valueOf(identifiedAnnotation.getPolarity());
            String textsem = identifiedAnnotation.getType().toString();
            String uncertainty = String.valueOf(identifiedAnnotation.getUncertainty());
            FeatureStructure[] ontologyFeatureStructures = ontologyConceptArr.toArray();

            Arrays.stream(ontologyFeatureStructures).forEach(ontologyFeatureStructure -> {
                // Get Connl
                List<ConllDependencyNode> conllDependencyNodes = JCasUtil.select(jCas, ConllDependencyNode.class)
                    .stream()
                    .filter(node -> Integer.parseInt(begin) <= node.getBegin() &&  node.getEnd() <= Integer.parseInt(end))
                    .collect(Collectors.toList());

                //Collection<ConllDependencyNode> conllDependencyNodes = JCasUtil.select(jCas, ConllDependencyNode.class);
                String trueTextFromConll = conllDependencyNodes.stream().map(n -> n.getForm()).collect(Collectors.joining(" "));
                String partsOfSpeech = conllDependencyNodes.stream().map(n -> n.getPostag()).collect(Collectors.joining(","));
                String trueText = jCas.getDocumentText().substring(Integer.parseInt(begin), Integer.parseInt(end));
                // Get ontology concepts
                String code = Util.getFeatureString(ontologyFeatureStructure, "code");
                String cui = Util.getFeatureString(ontologyFeatureStructure, "cui");
                String preferredText = Util.getFeatureString(ontologyFeatureStructure, "preferredText");
                String refsem = ontologyFeatureStructure.getType().toString();
                String codingScheme = Util.getFeatureString(ontologyFeatureStructure, "codingScheme");
                String score = String.valueOf(Util.getFeatureDouble(ontologyFeatureStructure, "score"));
                String tui = Util.getFeatureString(ontologyFeatureStructure, "tui");

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
                putInRow(row, Const.TRUE_TEXT_HEADER, trueText);
                putInRow(row, Const.DOCUMENT_ID, documentId);
                putInRow(row, Const.PARTS_OF_SPEECH_HEADER, partsOfSpeech);


                _writer.writeNext(row, false);
            });
        }
    }

    private void putInRow(String[] row, String header, String value) {
        row[_headerToIndex.get(header)] = value;
    }
    @Override
    public void close() {
        try {
            _writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
