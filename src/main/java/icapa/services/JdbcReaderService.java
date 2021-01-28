package icapa.services;

import icapa.models.JdbcReaderParams;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import java.sql.*;

public class JdbcReaderService implements CollectionReader {
    private static final Logger LOGGER = Logger.getLogger(JdbcReaderService.class.getName());

    private JdbcReaderParams _params;
    private Statement _statement;
    private ResultSet _resultSet;

    public static CollectionReader fromParams(JdbcReaderParams params) {
        JdbcReaderService reader = new JdbcReaderService();
        reader._params = params;
        return reader;
    }

    @Override
    public void initialize() {
        LOGGER.info("Initializing Jdbc Reader Service");
        setStatementAndResultSet();
    }

    private void setStatementAndResultSet() {
        LOGGER.info("Setting sql statement and getting result set");
        try {
            _resultSet = _params.getOntologyConnection().executeQuery(_params.getSqlStatement());
            _statement = _resultSet.getStatement();
            // Set cursor to one before the first record so you can iterate properly
            _resultSet.beforeFirst();
        } catch (SQLException throwables) {
            LOGGER.error("Error exeucting sql statement " + _params.getSqlStatement(), throwables);
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
            LOGGER.info("Reading document: " + docIdText);
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
            LOGGER.error("Error checking for next document", throwables);
        }
        return result;
    }

    @Override
    public void destroy() {
        LOGGER.info("Destroying reader and closing sql connection");
        if (_params.getOntologyConnection() != null) {
            try {
                _resultSet.close();
                _statement.close();
                _params.getOntologyConnection().close();
            } catch (SQLException throwables) {
                LOGGER.error("Error closing DB connection: ", throwables);
            }
        }
    }
}
