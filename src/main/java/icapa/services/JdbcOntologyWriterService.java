package icapa.services;

import icapa.Util;
import icapa.models.JdbcOntologyWriterParams;
import icapa.models.Ontology;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JdbcOntologyWriterService implements AnalysisEngine {
    private static final Logger LOGGER = Logger.getLogger(JdbcOntologyWriterService.class.getName());

    private JdbcOntologyWriterParams _params;

    public static AnalysisEngine fromParams(JdbcOntologyWriterParams params) {
        JdbcOntologyWriterService result = new JdbcOntologyWriterService();
        result._params = params;
        return result;
    }

    @Override
    public void initialize(UimaContext context) {
        LOGGER.info("Initializing Jdbc ontology writer service");
        if (!_params.getSqlConnection().tableExists(_params.getTable())) {
            // Table doesn't exist, so create it
            _params.getSqlConnection().createAnnotationTable(_params.getTable());
        }
    }

    @Override
    public void process(JCas jCas) {
        List<Ontology> ontologies = Util.getOntologies(jCas, _params.isKeepAll());
        for (Ontology ontology : ontologies) {
            _params.getSqlConnection().insertOntologyIntoTable(ontology, _params.getTable());
        }
    }


    @Override
    public void close() throws IOException {
        _params.getSqlConnection().close();
    }
}
