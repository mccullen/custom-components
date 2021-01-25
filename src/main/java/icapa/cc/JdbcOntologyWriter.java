package icapa.cc;

import icapa.Const;
import icapa.Util;
import icapa.models.HeaderProperties;
import icapa.models.JdbcOntologyWriterParams;
import icapa.models.JdbcSqlConnectionParams;
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

public class JdbcOntologyWriter extends AbstractJdbcWriter {
    private static final Logger LOGGER = Logger.getLogger(JdbcOntologyWriter.class.getName());

    public static final String PARAM_TABLE = "Table";
    @ConfigurationParameter(
        name = PARAM_TABLE,
        description = "Table",
        defaultValue = "",
        mandatory = true
    )
    private String _table;

    public static final String PARAM_CREATE_TABLE_SUFFIX = "CreateTableSuffix";
    @ConfigurationParameter(
        name = PARAM_CREATE_TABLE_SUFFIX,
        mandatory = false
    )
    private String _createTableSuffix;

    public static final String PARAM_DOCUMENT_ID_COL_AND_DATATYPE = "DocumentIdColAndDatatype";
    public static final String DEFAULT_VALUE_DOCUMENT_ID_COL_AND_DATATYPE = Const.DOCUMENT_ID + " VARCHAR(100)";
    @ConfigurationParameter(
        name = PARAM_DOCUMENT_ID_COL_AND_DATATYPE,
        mandatory = false,
        defaultValue = DEFAULT_VALUE_DOCUMENT_ID_COL_AND_DATATYPE
    )
    private String _documentIdColAndDatatype;

    private AnalysisEngine _writer;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        JdbcOntologyWriterParams params = new JdbcOntologyWriterParams();
        params.setTable(_table);

        // Set sql connection
        JdbcSqlConnectionParams jdbcSqlConnectionParams = new JdbcSqlConnectionParams();
        jdbcSqlConnectionParams.setPassword(getParams().getPassword());
        jdbcSqlConnectionParams.setUsername(getParams().getUsername());
        // Create the custom col/datatype pair by splitting the string on a space.
        String[] parts = _documentIdColAndDatatype.split(" ");
        HeaderProperties docHeader = new HeaderProperties();
        docHeader.setName(parts[0]);
        docHeader.setDataType(parts[1]);
        jdbcSqlConnectionParams.setDocumentIdColAndDatatype(docHeader);
        jdbcSqlConnectionParams.setUrl(Util.decodeUrl(getParams().getUrl()));
        jdbcSqlConnectionParams.setDriverClassName(getParams().getDriverClassName());
        jdbcSqlConnectionParams.setCreateTableSuffix(_createTableSuffix);
        SqlConnection sqlConnection = JdbcSqlConnection.fromParams(jdbcSqlConnectionParams);
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
