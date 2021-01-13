package icapa.services;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import icapa.Const;
import icapa.Util;
import icapa.models.Ontology;
import icapa.models.OntologyWriterParams;
import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class OntologyWriterService implements AnalysisEngine {
    private ICSVWriter _writer;
    private Map<String, Integer> _headerToIndex;
    public static OntologyWriterService from(OntologyWriterParams params) {
        OntologyWriterService result = new OntologyWriterService();
        try {
            result._writer = new CSVWriterBuilder(params.getWriter()).withSeparator(params.getDelimiter()).build();
            String[] headers = Util.getOntologyConceptHeaders();
            result._headerToIndex = Util.getKeyToIndex(headers);
            result._writer.writeNext(headers, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void initialize(UimaContext context) {
    }

    @Override
    public void process(JCas jCas) {
        List<Ontology> ontologies = Util.getOntologies(jCas);
        for (Ontology ontology : ontologies) {
            writeRow(ontology);
        }
    }

    private void writeRow(Ontology ontology) {
        String[] row = new String[_headerToIndex.size()];

        putInRow(row, Const.ADDRESS_HEADER, String.valueOf(ontology.getAddress()));
        putInRow(row, Const.CODE_HEADER, ontology.getCode());
        putInRow(row, Const.CONDITIONAL_HEADER, String.valueOf(ontology.isConditional()));
        putInRow(row, Const.CONFIDENCE_HEADER, String.valueOf(ontology.getConfidence()));
        putInRow(row, Const.CUI_HEADER, ontology.getCui());
        putInRow(row, Const.GENERIC_HEADER, String.valueOf(ontology.isGeneric()));
        putInRow(row, Const.POLARITY_HEADER, String.valueOf(ontology.getPolarity()));
        putInRow(row, Const.END_HEADER, String.valueOf(ontology.getEnd()));
        putInRow(row, Const.BEGIN_HEADER, String.valueOf(ontology.getBegin()));
        putInRow(row, Const.PREFERRED_TEXT_HEADER, ontology.getPreferredText());
        putInRow(row, Const.REFSEM_HEADER, ontology.getRefsem());
        putInRow(row, Const.SCHEME_HEADER, ontology.getCodingScheme());
        putInRow(row, Const.SCORE_HEADER, String.valueOf(ontology.getScore()));
        putInRow(row, Const.SUBJECT_HEADER, ontology.getSubject());
        putInRow(row, Const.TEXTSEM_HEADER, ontology.getTextsem());
        putInRow(row, Const.TUI_HEADER, ontology.getTui());
        putInRow(row, Const.UNCERTAINTY_HEADER, String.valueOf(ontology.getUncertainty()));
        putInRow(row, Const.TRUE_TEXT_HEADER, ontology.getTrueText());
        putInRow(row, Const.DOCUMENT_ID, ontology.getDocumentId());
        putInRow(row, Const.PARTS_OF_SPEECH_HEADER, ontology.getPartsOfSpeech());

        _writer.writeNext(row, false);
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
