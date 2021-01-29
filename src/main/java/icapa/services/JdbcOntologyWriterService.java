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
        if (!_params.getOntologyConnection().tableExists(_params.getTable())) {
            // Table doesn't exist, so create it
            _params.getOntologyConnection().createAnnotationTable(_params.getTable());
        }
    }

    @Override
    public void process(JCas jCas) {
        List<Ontology> ontologies = Util.getOntologies(jCas, _params.isKeepAll());
        OntologyConnection conn = _params.getOntologyConnection();
        for (Ontology ontology : ontologies) {
            if (conn.supportsBatchUpdates() && _params.getJdbcWriterParams().getBatchSize() > 1) {
                // Supports batch updates
                if (_batchIndex >= _params.getJdbcWriterParams().getBatchSize()) {
                    conn.executeBatch();
                    _batchIndex = 0;
                }
                ++_batchIndex;
                String query = conn.getOntologyInsertQueryForTable(ontology, _params.getTable());
                conn.addBatch(query);

                /*
                *   PreparedStatement ps = c.prepareStatement("INSERT INTO employees VALUES (?, ?)");

  ps.setString(1, "John");
  ps.setString(2,"Doe");
  ps.addBatch();

  ps.clearParameters();
  ps.setString(1, "Dave");
  ps.setString(2,"Smith");
  ps.addBatch();

  ps.clearParameters();
  int[] results = ps.executeBatch();
                * */
            } else {
                // Doesn't support batch updates
                String query = conn.getOntologyInsertQueryForTable(ontology, _params.getTable());
                conn.executeUpdate(query);
            }
        }
    }


    @Override
    public void close() throws IOException {
        _params.getOntologyConnection().close();
    }
}
