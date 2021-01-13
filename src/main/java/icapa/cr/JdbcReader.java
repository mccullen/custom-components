package icapa.cr;

import icapa.models.JdbcReaderParams;
import icapa.services.CollectionReader;
import icapa.services.JdbcReaderService;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Logger;

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

    public static final String PARAM_DRIVER_CLASS = "DriverClassName";
    @ConfigurationParameter(
        name = PARAM_DRIVER_CLASS,
        description = "Full class name of the driver. Make sure to put the driver jar in lib/"
    )
    private String _driverClassName;

    public static final String PARAM_DOCUMENT_ID_COLUMN = "DocumentIdColumnName";
    @ConfigurationParameter(
        name = PARAM_DOCUMENT_ID_COLUMN
    )
    private String _documentIdCol;

    public static final String PARAM_URL = "URL";
    @ConfigurationParameter(
        name = PARAM_URL
    )
    private String _url;

    public static final String PARAM_USERNAME = "Username";
    @ConfigurationParameter(
        name = PARAM_USERNAME,
        mandatory = false
    )
    private String _username;

    public static final String PARAM_PASSWORD = "Password";
    @ConfigurationParameter(
        name = PARAM_PASSWORD,
        mandatory = false
    )
    private String _password;

    private CollectionReader _reader;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        JdbcReaderParams params = new JdbcReaderParams();
        params.setDocumentTextColName(_docTextColName);
        params.setDriverClassName(_driverClassName);
        params.setPassword(_password);
        params.setSqlStatement(_sqlStatement);
        params.setURL(_url);
        params.setUsername(_username);
        params.setDocumentIdColName(_documentIdCol);
        _reader = JdbcReaderService.fromParams(params);
        _reader.initialize();
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
