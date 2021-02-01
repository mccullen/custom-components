package icapa.cc;

import icapa.Const;
import icapa.models.HeaderProperties;
import icapa.models.JdbcOntologyConsumerParams;
import icapa.models.JdbcOntologyWriterParams;
import icapa.services.*;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;

public class JdbcOntologyWriter extends AbstractJdbcWriter {
    private static final Logger LOGGER = Logger.getLogger(JdbcOntologyWriter.class.getName());

    public static final String PARAM_TABLE = "Table";
    @ConfigurationParameter(
        name = PARAM_TABLE,
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
    public static final String DEFAULT_VALUE_DOCUMENT_ID_COL_AND_DATATYPE = Const.DOCUMENT_ID_HEADER + " VARCHAR(100)";
    @ConfigurationParameter(
        name = PARAM_DOCUMENT_ID_COL_AND_DATATYPE,
        mandatory = false,
        defaultValue = DEFAULT_VALUE_DOCUMENT_ID_COL_AND_DATATYPE
    )
    private String _documentIdColAndDatatype;

    @ConfigurationParameter(
        name = Const.PARAM_KEEP_ALL,
        mandatory = false,
        defaultValue = "true"
    )
    private boolean _keepAll;

    private AnalysisEngine _writer;
    private JdbcOntologyWriterParams _params = new JdbcOntologyWriterParams();

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        setParams();
        OntologyConsumer ontologyConsumer = getOntologyConsumer();
        _writer = OntologyWriterService.fromParams(ontologyConsumer, _params.isKeepAll());
        _writer.initialize(context);
    }

    private void setParams() {
        _params.setJdbcWriterParams(getParams()); // inherited params
        _params.setTable(_table);
        _params.setKeepAll(_keepAll);
        _params.setCreateTableSuffix(_createTableSuffix);
        _params.setDocumentIdColAndDatatype(getDocHeader());
    }

    private OntologyConsumer getOntologyConsumer() {
        // Set sql connection
        JdbcOntologyConsumerParams ontologyConsumerParams = new JdbcOntologyConsumerParams();
        ontologyConsumerParams.setDriverClassName(getParams().getDriverClassName());
        ontologyConsumerParams.setUsername(getParams().getUsername());
        ontologyConsumerParams.setPassword(getParams().getPassword());
        ontologyConsumerParams.setUrl(_params.getJdbcWriterParams().getUrl());
        ontologyConsumerParams.setTable(_params.getTable());
        ontologyConsumerParams.setDocumentIdColAndDatatype(_params.getDocumentIdColAndDatatype());
        ontologyConsumerParams.setCreateTableSuffix(_params.getCreateTableSuffix());
        ontologyConsumerParams.setBatchSize(_params.getJdbcWriterParams().getBatchSize());
        OntologyConsumer sqlConnection = JdbcOntologyConsumer.fromParams(ontologyConsumerParams);
        return sqlConnection;
    }

    private HeaderProperties getDocHeader() {
        // Create the custom col/datatype pair by splitting the string on a space.
        String[] parts = _documentIdColAndDatatype.split(" ");
        HeaderProperties docHeader = new HeaderProperties();
        docHeader.setName(parts[0]);
        docHeader.setDataType(parts[1]);
        return docHeader;
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
