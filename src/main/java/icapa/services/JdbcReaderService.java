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


    public static CollectionReader fromParams(JdbcReaderParams params) {
        JdbcReaderService reader = new JdbcReaderService();
        reader._params = params;
        return reader;
    }

    @Override
    public void initialize() {
        loadDriver();
        if (_params.getUsername() != null && _params.getPassword() != null) {
            connectUsingUsernameAndPassword();
        } else {
            connectUsingUrl();
        }
        setStatementAndResultSet();
    }

    private void loadDriver() {
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
            LOGGER.error("Could not connect to driver named " + _params.getDriverClassName(), e);
        }
    }

    private void connectUsingUrl() {
        try {
            _connection = DriverManager.getConnection(_params.getURL(), _params.getUsername(), _params.getPassword());
        } catch (Exception e) {
            LOGGER.error("Could not connect to driver named " + _params.getDriverClassName(), e);
        }
    }

    private void setStatementAndResultSet() {
        try {
            _statement = _connection.createStatement();
            _resultSet = _statement.executeQuery(_params.getSqlStatement());
            // Set cursor to one before the first record so you can iterate properly
            _resultSet.beforeFirst();
        } catch (SQLException throwables) {
            LOGGER.error(throwables);
        }
    }

    @Override
    public void readNext(JCas jCas) {
        try {
            // Set document text
            jCas.setDocumentText(_resultSet.getString(_params.getDocumentTextColName()));

            // Add document Id
            DocumentID documentId = new DocumentID(jCas);
            documentId.setDocumentID(_params.getDocumentIdColName());
            documentId.addToIndexes();
        } catch (SQLException throwables) {
            LOGGER.error("Error reading next document: ", throwables);
        }
    }

    @Override
    public boolean hasNext() {
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
        if (_connection != null) {
            try {
                _connection.close();
            } catch (SQLException throwables) {
                LOGGER.error("Error closing DB connection: ", throwables);
            }
        }
    }
}
