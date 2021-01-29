package icapa.services;

import icapa.Util;
import icapa.models.JdbcOntologyConnectionParams;
import icapa.models.JdbcOntologyWriterParams;
import icapa.models.JdbcWriterParams;
import icapa.models.Ontology;
import org.apache.log4j.Logger;

import java.sql.*;

public class JdbcOntologyConnection implements OntologyConnection {
    private static final Logger LOGGER = Logger.getLogger(JdbcOntologyConnection.class.getName());

    private Connection _connection;
    private JdbcOntologyConnectionParams _params;
    private boolean _supportsBatchUpdates = false;
    private int _nBatches = 0;
    private Statement _batchStatement;

    public static JdbcOntologyConnection fromParams(JdbcOntologyConnectionParams params) {
        JdbcOntologyConnection engine = new JdbcOntologyConnection();
        engine._params = params;
        Util.loadDriver(engine._params.getDriverClassName());
        engine.setConnection();
        return engine;
    }

    private void setConnection() {
        if (_params.getUsername() != null && _params.getPassword() != null) {
            _connection = Util.getConnection(
                _params.getUsername(),
                _params.getPassword(),
                _params.getUrl());
        } else {
            _connection = Util.getConnection(_params.getUrl());
        }
        setSupportsBatchUpdates();
        if (_supportsBatchUpdates) {
            // Batch updates supported, so set autocommit to false so you have to explicitly commit
            // batches of queries. Also, create the batch statement that you will add batch queries to.
            try {
                _connection.setAutoCommit(false);
                _batchStatement = _connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                //_connection.prepareStatement("",ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            } catch (SQLException throwables) {
                LOGGER.error("Error setting autocommit to false", throwables);
            }
        }
    }

    private void setSupportsBatchUpdates() {
        try {
            _supportsBatchUpdates = _connection.getMetaData().supportsBatchUpdates();
        } catch (SQLException throwables) {
            LOGGER.error(throwables);
        }
        LOGGER.info("Driver supports batch updates: " + _supportsBatchUpdates);
    }

    @Override
    public ResultSet executeQuery(String query) {
        LOGGER.info("Executing query: " + query);
        ResultSet resultSet = null;
        try {
            Statement statement = _connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            resultSet = statement.executeQuery(query);
            commit();
            // DO NOT close the statement here. If you do, the result set will not be able to iterate
            // The caller is responsible for closing the resultSet and statement.
        } catch (SQLException throwables) {
            LOGGER.error("Error executing query", throwables);
        }
        return resultSet;
    }

    @Override
    public int executeUpdate(String query) {
        LOGGER.info("Executing update: " + query);
        int result = 0;
        try {
            Statement statement = _connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            result = statement.executeUpdate(query);
            commit();
            statement.close(); // Close statement since you are not returning any resultSet
        } catch (SQLException throwables) {
            LOGGER.error("Error executing update", throwables);
        }
        return result;
    }

    @Override
    public boolean tableExists(String table) {
        // TODO: Is there a better way to do this?
        boolean result = false;
        try {
            Statement statement = _connection.createStatement();
            statement.execute("SELECT 1 FROM " + table);
            commit();
            statement.close();
            result = true;
        } catch (SQLException throwables) {
            LOGGER.info("Table " + table + " does not exist. Attempting to create");
        }
        return result;
    }

    @Override
    public void createAnnotationTable(String table) {
        String query = Util.getCreateTableQuery(table, _params.getCreateTableSuffix(), _params.getDocumentIdColAndDatatype());
        executeUpdate(query);
    }

    @Override
    public String getOntologyInsertQueryForTable(Ontology ontology, String table) {
        String query = Util.getInsertQuery(table, ontology, _params.getDocumentIdColAndDatatype());
        return query;
    }

    @Override
    public boolean supportsBatchUpdates() {
        return _supportsBatchUpdates;
    }

    @Override
    public int[] executeBatch() {
        LOGGER.info("Executing batch");
        int[] updateCounts = new int[0];
        if (_supportsBatchUpdates) {
            try {
                updateCounts = _batchStatement.executeBatch();
                commit();
                _nBatches = 0;
            } catch (SQLException throwables) {
                LOGGER.error("Failed to execute batch statement", throwables);
            }
        } else {
            LOGGER.error("Driver does not support batch updates");
        }
        return updateCounts;
    }

    @Override
    public void addBatch(String query) {
        LOGGER.info("Adding query to batch: " + query);
        try {
            _batchStatement.addBatch(query);
            ++_nBatches;
        } catch (SQLException throwables) {
            LOGGER.error("Error adding query to batch: " + query, throwables);
        }
    }

    private void commit() {
        try {
            if (!_connection.getAutoCommit()) {
                _connection.commit();
            }
        } catch (SQLException throwables) {
            LOGGER.error("Error committing: ", throwables);
        }
    }

    @Override
    public void close() {
        try {
            if (_batchStatement != null) {
                executeBatch();
                _batchStatement.close();
            }
            _connection.close();
        } catch (SQLException throwables) {
            LOGGER.error("Error closing connection to teradata", throwables);
        }
    }
}
