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

    public static AnalysisEngine fromParams(JdbcOntologyWriterParams params) {
        JdbcOntologyWriterService result = new JdbcOntologyWriterService();
        result._params = params;
        return result;
    }

    @Override
    public void initialize(UimaContext context) {
        LOGGER.info("Initializing Jdbc ontology writer service");
        // TODO: Check if table exists. If so, do nothing. Else, create it
    }

    @Override
    public void process(JCas jCas) {
        List<Ontology> ontologies = Util.getOntologies(jCas);
        for (Ontology ontology : ontologies) {
            writeRow(ontology);
        }
    }

    private void writeRow(Ontology ontology) {
        String[] headers = Util.getOntologyConceptHeaders();
        // TODO: INSERT INTO mytbl () VALUES ();
        String insertStatement = "INSERT INTO " + _params.getTable() + " (" + String.join(",", headers) ;
        int nRowsAffected = _params.getSqlConnection().executeUpdate(insertStatement);
    }

    @Override
    public void close() throws IOException {
        _params.getSqlConnection().close();
    }
}
