package icapa.services;

import icapa.Util;
import icapa.models.JdbcReaderParams;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import java.sql.*;

public class JdbcReaderService implements CollectionReader {
    private static final Logger LOGGER = Logger.getLogger(JdbcReaderService.class.getName());
    //public static final long PING_INTERVAL_IN_MILISECONDS = 3600000; // 1hr
    public static final long PING_INTERVAL_IN_MILISECONDS = 60000;

    private JdbcReaderParams _params;
    private Statement _statement;
    private ResultSet _resultSet;
    private long _startTime = System.nanoTime();

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
            pingServerIfElapsedTime();
        } catch (SQLException throwables) {
            LOGGER.error("Error reading next document: ", throwables);
        }
    }

    private void pingServerIfElapsedTime() {
        long time = System.nanoTime();
        long elapsedTimeInMiliseconds = (time - _startTime)/1000000;
        if (elapsedTimeInMiliseconds > PING_INTERVAL_IN_MILISECONDS) {
            LOGGER.info("Pinging server");
            ResultSet rs = _params.getOntologyConnection().executeQuery("SELECT 1;");
            try {
                rs.getStatement().close();
                rs.close();
            } catch (SQLException throwables) {
                Util.logExceptionChain(LOGGER, throwables);
            }
            _startTime = time;
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
            Util.logExceptionChain(LOGGER, throwables);
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
