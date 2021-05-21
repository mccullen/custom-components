package icapa.cc;

import icapa.Const;
import icapa.models.OntologyWriterParams;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

public abstract class AbstractFileOntologyWriter extends JCasAnnotator_ImplBase {
    static public final String PARAM_DELIMITER = "Delimiter";

    // Configuration parameters
    @ConfigurationParameter(
        name = PARAM_DELIMITER,
        defaultValue = ",",
        mandatory = true
    )
    private char _delimiter;

    @ConfigurationParameter(
        name = Const.PARAM_KEEP_ALL,
        defaultValue = "true",
        mandatory = false
    )
    private boolean _keepAll;

    private OntologyWriterParams _params = new OntologyWriterParams();

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        _params.setDelimiter(_delimiter);
        _params.setKeepAll(_keepAll);
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
    }

    public OntologyWriterParams getParams() {
        return _params;
    }
}
