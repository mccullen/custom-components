package icapa.services;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import icapa.Const;
import icapa.Util;
import icapa.models.Ontology;
import icapa.models.OntologyWriterParams;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class OntologyWriterService implements AnalysisEngine {
    private static final Logger LOGGER = Logger.getLogger(OntologyWriterService.class.getName());

    private ICSVWriter _writer;
    private Map<String, Integer> _headerToIndex;
    private String[] _headers;
    private OntologyWriterParams _params;
    public static OntologyWriterService from(OntologyWriterParams params) {
        /*
        OntologyWriterService result = new OntologyWriterService();
        result._params = params;
        try {
            result._writer = new CSVWriterBuilder(params.getWriter()).withSeparator(params.getDelimiter()).build();
            result._headers = Util.getOntologyConceptHeaders();
            result._headerToIndex = Util.getKeyToIndex(result._headers);
            result._writer.writeNext(result._headers, false);
        } catch (Exception e) {
            LOGGER.error("Error initializing ontology writer service", e);
        }
        return result;

         */
        return null;
    }

    @Override
    public void initialize(UimaContext context) {
    }

    @Override
    public void process(JCas jCas) {
        List<Ontology> ontologies = Util.getOntologies(jCas, _params.isKeepAll());
        for (Ontology ontology : ontologies) {
            writeRow(ontology);
        }
    }

    private void writeRow(Ontology ontology) {
        String[] row = Util.getOntologyAsStringArray(ontology, _headerToIndex);
        _writer.writeNext(row, false);
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
