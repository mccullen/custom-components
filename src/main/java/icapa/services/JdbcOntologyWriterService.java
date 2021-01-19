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
    private Map<String, Integer> _headerToIndex;
    private String[] _headers;

    public static AnalysisEngine fromParams(JdbcOntologyWriterParams params) {
        JdbcOntologyWriterService result = new JdbcOntologyWriterService();
        result._params = params;
        result._headers = Util.getOntologyConceptHeaders();
        result._headerToIndex = Util.getKeyToIndex(result._headers);
        return result;
    }

    @Override
    public void initialize(UimaContext context) {
        LOGGER.info("Initializing Jdbc ontology writer service");
        if (!_params.getSqlConnection().tableExists(_params.getTable())) {
            // Table doesn't exist, so create it
            createTable();
        }
    }

    private void createTable() {
        String[] headersWithDatatypes = new String[_headers.length];
        for (int i = 0; i < _headers.length; ++i) {
            headersWithDatatypes[i] = "\"" + _headers[i] + "\"" + " VARCHAR(100)";
        }
        String query = "CREATE TABLE " + _params.getTable() + " (" + String.join(",", headersWithDatatypes) + ");";
        LOGGER.info("Executing query: " + query);
        _params.getSqlConnection().executeUpdate(query);
    }

    @Override
    public void process(JCas jCas) {
        List<Ontology> ontologies = Util.getOntologies(jCas);
        for (Ontology ontology : ontologies) {
            writeRow(ontology);
        }
    }

    private void writeRow(Ontology ontology) {
        String[] row = Util.getOntologyAsStringArray(ontology, _headerToIndex);
        String[] wrappedHeaders = new String[_headers.length];
        for (int i = 0; i < _headers.length; ++i) {
            wrappedHeaders[i] = "\"" + _headers[i] + "\"";
        }
        String insertStatement = "INSERT INTO " + _params.getTable() + " (" + String.join(",", wrappedHeaders) + ") VALUES (" + Util.wrapInSqlString(row) + ");";
        LOGGER.info("Executing query: " + insertStatement);
        int nRowsAffected = _params.getSqlConnection().executeUpdate(insertStatement);
    }

    @Override
    public void close() throws IOException {
        _params.getSqlConnection().close();
    }
}
