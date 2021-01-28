package icapa.cr;

import icapa.Const;
import icapa.Util;
import icapa.models.JdbcReaderParams;
import icapa.models.JdbcOntologyConnectionParams;
import icapa.services.CollectionReader;
import icapa.services.JdbcReaderService;
import icapa.services.JdbcOntologyConnection;
import icapa.services.OntologyConnection;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class JdbcReader extends JCasCollectionReader_ImplBase {
    private static Logger LOGGER = Logger.getLogger(JdbcReader.class.getName());

    /**
     * SQL statement to retrieve the document.
     */
    public static final String PARAM_SQL = "SqlStatement";
    @ConfigurationParameter(
        name = PARAM_SQL,
        description = "SQL statement to retrieve the document."
    )
    private String _sqlStatement;

    /**
     * Name of column from resultset that contains the document text. Supported
     * column types are CHAR, VARCHAR, and CLOB.
     */
    public static final String PARAM_DOCTEXT_COL = "DocTextColName";
    @ConfigurationParameter(
        name = PARAM_DOCTEXT_COL,
        description = "Name of column from resultset that contains the document text."
    )
    private String _docTextColName;

    @ConfigurationParameter(
        name = Const.PARAM_DRIVER_CLASS,
        description = Const.PARAM_DRIVER_CLASS_DESCRIPTION
    )
    private String _driverClassName;

    private static final String PARAM_DOCUMENT_ID_COLUMN = "DocumentIdColumnName";
    @ConfigurationParameter(
        name = PARAM_DOCUMENT_ID_COLUMN,
        description = "The name of the column used to identify the document. This should be unique for each document."
    )
    private String _documentIdCol;

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

    private CollectionReader _reader;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        LOGGER.info("Initializing Jdbc Reader");

        JdbcReaderParams params = new JdbcReaderParams();
        params.setDocumentTextColName(_docTextColName);
        params.setDocumentIdColName(_documentIdCol);
        params.setSqlStatement(Util.decodeUrl(_sqlStatement));
        params.setOntologyConnection(getOntologyConnection());

        _reader = JdbcReaderService.fromParams(params);
        _reader.initialize();
    }

    private OntologyConnection getOntologyConnection() {
        // Set sql connection
        JdbcOntologyConnectionParams jdbcSqlConnectionParams = new JdbcOntologyConnectionParams();
        jdbcSqlConnectionParams.setDriverClassName(_driverClassName);
        jdbcSqlConnectionParams.setUrl(Util.decodeUrl(_url));
        jdbcSqlConnectionParams.setUsername(_username);
        jdbcSqlConnectionParams.setPassword(_password);
        JdbcOntologyConnection sqlConnection = JdbcOntologyConnection.fromParams(jdbcSqlConnectionParams);
        return sqlConnection;
    }

    @Override
    public void getNext(JCas jCas) throws IOException, CollectionException {
        _reader.readNext(jCas);
    }

    @Override
    public boolean hasNext() throws IOException, CollectionException {
        return _reader.hasNext();
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[0];
    }
}
