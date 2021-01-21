package icapa.cc;

import icapa.Const;
import icapa.Util;
import icapa.models.JdbcOntologyWriterParams;
import icapa.models.JdbcParams;
import icapa.services.AnalysisEngine;
import icapa.services.JdbcOntologyWriterService;
import icapa.services.SqlConnection;
import icapa.services.JdbcSqlConnection;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;

public class JdbcOntologyWriter extends JCasAnnotator_ImplBase {
    private static final Logger LOGGER = Logger.getLogger(JdbcOntologyWriter.class.getName());

    public static final String PARAM_TABLE = "Table";
    @ConfigurationParameter(
        name = PARAM_TABLE,
        description = "Table",
        defaultValue = "",
        mandatory = true
    )
    private String _table;

    @ConfigurationParameter(
        name = Const.PARAM_DRIVER_CLASS,
        description = "Full class name of the driver. Make sure to put the driver jar in lib/"
    )
    private String _driverClassName;

    @ConfigurationParameter(
        name = Const.PARAM_DOCUMENT_ID_COLUMN,
        mandatory = false,
        defaultValue = Const.DOCUMENT_ID
    )
    private String _documentIdCol;

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

    public static final String PARAM_CREATE_TABLE_SUFFIX = "CreateTableSuffix";
    @ConfigurationParameter(
        name = PARAM_CREATE_TABLE_SUFFIX,
        mandatory = false,
        defaultValue = "Id"
    )
    private String _createTableSuffix;

    private AnalysisEngine _writer;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        JdbcOntologyWriterParams params = new JdbcOntologyWriterParams();
        params.setDocumentIdColumn(_documentIdCol);
        params.setTable(_table);

        // Set sql connection
        JdbcParams jdbcParams = new JdbcParams();
        jdbcParams.setPassword(_password);
        jdbcParams.setUsername(_username);
        jdbcParams.setUrl(Util.decodeUrl(_url));
        jdbcParams.setDriverClassName(_driverClassName);
        jdbcParams.setCreateTableSuffix(_createTableSuffix);
        SqlConnection sqlConnection = JdbcSqlConnection.fromParams(jdbcParams);
        params.setSqlConnection(sqlConnection);

        _writer = JdbcOntologyWriterService.fromParams(params);
        _writer.initialize(context);
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        _writer.process(jCas);
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            _writer.close();
        } catch (IOException e) {
            LOGGER.error("Error closing jdbc service", e);
        }
    }
}
