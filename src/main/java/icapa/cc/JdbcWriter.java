package icapa.cc;

import icapa.Const;
import icapa.services.AnalysisEngine;
import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

public class JdbcWriter extends AbstractJdbcWriter {
    private static final Logger LOGGER = Logger.getLogger(JdbcWriter.class.getName());

    public static final String PARAM_NAMESPACE = "Namespace";
    @ConfigurationParameter(
        name = PARAM_NAMESPACE,
        mandatory = false,
        defaultValue = ""
    )
    private String _namespace;

    private AnalysisEngine _writer;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

    }
}
