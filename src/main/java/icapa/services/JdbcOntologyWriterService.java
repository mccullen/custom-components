package icapa.services;

import icapa.Util;
import icapa.models.JdbcOntologyWriterParams;
import icapa.models.Ontology;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.util.List;

public class JdbcOntologyWriterService implements AnalysisEngine {
    private static final Logger LOGGER = Logger.getLogger(JdbcOntologyWriterService.class.getName());

    private JdbcOntologyWriterParams _params;
    private int _batchIndex = 0;

    public static AnalysisEngine fromParams(JdbcOntologyWriterParams params) {
        JdbcOntologyWriterService result = new JdbcOntologyWriterService();
        result._params = params;
        return result;
    }

    @Override
    public void initialize(UimaContext context) {
        LOGGER.info("Initializing Jdbc ontology writer service");
        _params.getOntologyConsumer().createAnnotationTableIfAbsent();
    }

    @Override
    public void process(JCas jCas) {
        List<Ontology> ontologies = Util.getOntologies(jCas, _params.isKeepAll());
        for (Ontology ontology : ontologies) {
            _params.getOntologyConsumer().insertOntologyIntoAnnotationTable(ontology);
        }
    }


    @Override
    public void close() throws IOException {
        _params.getOntologyConsumer().close();
    }
}
