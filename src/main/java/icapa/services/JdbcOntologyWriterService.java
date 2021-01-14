package icapa.services;

import icapa.models.JdbcOntologyWriterParams;
import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;

import java.io.IOException;

public class JdbcOntologyWriterService implements AnalysisEngine {
    private JdbcOntologyWriterParams _params;

    public static AnalysisEngine fromParams(JdbcOntologyWriterParams params) {
        JdbcOntologyWriterService result = new JdbcOntologyWriterService();
        result._params = params;
        return result;
    }

    @Override
    public void initialize(UimaContext context) {
    }

    @Override
    public void process(JCas jCas) {

    }

    @Override
    public void close() throws IOException {

    }
}
