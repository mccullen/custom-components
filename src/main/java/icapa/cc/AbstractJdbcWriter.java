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

// TODO
public abstract class AbstractJdbcWriter extends BaseWriter {
    private static final Logger LOGGER = Logger.getLogger(AbstractJdbcWriter.class.getName());

    @ConfigurationParameter(
        name = Const.PARAM_DRIVER_CLASS,
        description = Const.PARAM_DRIVER_CLASS_DESCRIPTION
    )
    private String _driverClassName;

    @ConfigurationParameter(
        name = Const.PARAM_URL,
        description = Const.PARAM_URL_DESCRIPTION
    )
    private String _url;

    @ConfigurationParameter(
        name = Const.PARAM_USERNAME,
        description = Const.PARAM_USERNAME_DESCRIPTION,
        mandatory = false
    )
    private String _username;

    @ConfigurationParameter(
        name = Const.PARAM_PASSWORD,
        description = Const.PARAM_PASSWORD_DESCRIPTION,
        mandatory = false
    )
    private String _password;

    private static final String PARAM_BATCH_SIZE = "BatchSize";
    @ConfigurationParameter(
        name = PARAM_BATCH_SIZE,
        description = "The size of the batch for inserting into the Table. If this is set to 0 or 1, then batches will not be used. Each annotation will be inserted into the Table via an INSERT statement. If you do not use batches, then EACH of these INSERT statements will be sent across the network to the database independently, which is inefficient. But if you set BatchSize to 100, then 100 INSERT statements will be batched together and sent across the network at once. However, the risk with setting this too high is that the network payload (with 100 INSERT statements, for example) is greater. Also, there is a greater risk of something going wrong (an error being thrown, losing connection, etc.) and ruining the whole batch.",
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
