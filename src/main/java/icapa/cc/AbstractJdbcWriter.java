package icapa.cc;

import icapa.Const;
import icapa.Util;
import icapa.models.JdbcWriterParams;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

public abstract class AbstractJdbcWriter extends JCasAnnotator_ImplBase {
    private static final Logger LOGGER = Logger.getLogger(AbstractJdbcWriter.class.getName());

    @ConfigurationParameter(
        name = Const.PARAM_DRIVER_CLASS,
        description = "Full class name of the driver. Make sure to put the driver jar in lib/"
    )
    private String _driverClassName;

    @ConfigurationParameter(
        name = Const.PARAM_URL
    )
    private String _url;

    @ConfigurationParameter(
        name = Const.PARAM_USERNAME,
        mandatory = false
    )
    private String _username;

    @ConfigurationParameter(
        name = Const.PARAM_PASSWORD,
        mandatory = false
    )
    private String _password;

    @ConfigurationParameter(
        name = Const.PARAM_BATCH_SIZE,
        mandatory = false,
        defaultValue = "0"
    )
    private int _batchSize;

    private JdbcWriterParams _params = new JdbcWriterParams();

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        _params.setPassword(_password);
        _params.setUsername(_username);
        _params.setDriverClassName(_driverClassName);
        _params.setUrl(Util.decodeUrl(_url));
        _params.setBatchSize(_batchSize);
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
    }

    public JdbcWriterParams getParams() {
        return _params;
    }
}
