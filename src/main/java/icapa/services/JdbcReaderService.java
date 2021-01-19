package icapa.services;

import icapa.models.JdbcReaderParams;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import java.sql.*;

public class JdbcReaderService implements CollectionReader {
    private static final Logger LOGGER = Logger.getLogger(JdbcReaderService.class.getName());

    private JdbcReaderParams _params;
    private Connection _connection;
    private Statement _statement;
    private ResultSet _resultSet;
    private SqlConnection _sqlConnection;

    public static CollectionReader fromParams(JdbcReaderParams params) {
        JdbcReaderService reader = new JdbcReaderService();
        reader._params = params;
        return reader;
    }

    @Override
    public void initialize() {
        LOGGER.info("Initializing logger reader service");
        loadDriver();
        LOGGER.info("Connecting to driver");
        if (_params.getUsername() != null && _params.getPassword() != null) {
            connectUsingUsernameAndPassword();
        } else {
            connectUsingUrl();
        }
        setStatementAndResultSet();
    }

    private void loadDriver() {
        LOGGER.info("Loading jdbc driver");
        try {
            Class.forName(_params.getDriverClassName());
        } catch (ClassNotFoundException e) {
            LOGGER.error("Error loading driver: ", e);
        }
    }

    private void connectUsingUsernameAndPassword() {
        try {
            _connection = DriverManager.getConnection(_params.getURL());
        } catch (Exception e) {
            LOGGER.error("Could not connect to driver named " + _params.getDriverClassName() + " at " + _params.getURL(), e);
        }
    }

    private void connectUsingUrl() {
        try {
            _connection = DriverManager.getConnection(_params.getURL(), _params.getUsername(), _params.getPassword());
        } catch (Exception e) {
            LOGGER.error("Could not connect to driver named " + _params.getDriverClassName() + " at " + _params.getURL() , e);
        }
    }

    private void setStatementAndResultSet() {
        LOGGER.info("Setting sql statement and getting result set");
        try {
            _statement = _connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            _resultSet = _statement.executeQuery(_params.getSqlStatement());
            // Set cursor to one before the first record so you can iterate properly
            _resultSet.beforeFirst();
        } catch (SQLException throwables) {
            LOGGER.error(throwables);
        }
    }

    @Override
    public void readNext(JCas jCas) {
        LOGGER.info("Reading next document");
        try {
            // Set document text
            String text = _resultSet.getString(_params.getDocumentTextColName());
            text = text == null ? "" : text;
            jCas.setDocumentText(text);

            // Add document Id
            DocumentID documentId = new DocumentID(jCas);
            String docIdText = _resultSet.getString(_params.getDocumentIdColName());
            docIdText = docIdText == null ? "" : docIdText;
            documentId.setDocumentID(docIdText);
            documentId.addToIndexes();
        } catch (SQLException throwables) {
            LOGGER.error("Error reading next document: ", throwables);
        }
    }

    @Override
    public boolean hasNext() {
        LOGGER.info("Checking for another document");
        boolean result = false;
        try {
            result = _resultSet.next();
        } catch (SQLException throwables) {
            LOGGER.error(throwables);
        }
        return result;
    }

    @Override
    public void destroy() {
        LOGGER.info("Destroying reader and closing sql connection");
        if (_connection != null) {
            try {
                _resultSet.close();
                _statement.close();
                _connection.close();
            } catch (SQLException throwables) {
                LOGGER.error("Error closing DB connection: ", throwables);
            }
        }
    }
}
