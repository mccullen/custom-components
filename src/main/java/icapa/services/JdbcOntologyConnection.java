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
    private boolean _useBatchUpdates = false;
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
        LOGGER.info("Driver supports batch updates: " + _supportsBatchUpdates);
        if (useBatchUpdates()) {
            LOGGER.info("Using batch updates with a batch size of " + _params.getBatchSize());
            try {
                _connection.setAutoCommit(false);
                _batchStatement = _connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
    }

    private boolean useBatchUpdates() {
        return _supportsBatchUpdates && _params.getBatchSize() > 1;
    }

    @Override
    public ResultSet executeQuery(String query) {
        LOGGER.info("Attempting to execute query: " + query);
        ResultSet resultSet = null;
        try {
            Statement statement = _connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            resultSet = statement.executeQuery(query);
        } catch (SQLException throwables) {
            LOGGER.error("Error executing query", throwables);
        }
        return resultSet;
    }

    @Override
    public int executeUpdate(String query) {
        LOGGER.info("Attempting to execute query: " + query);
        int result = 0;
        try {
            Statement statement = _connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            result = statement.executeUpdate(query);
            statement.close();
        } catch (SQLException throwables) {
            LOGGER.error("Error executing update", throwables);
        }
        return result;
    }

    @Override
    public boolean tableExists(String table) {
        // TODO: You should probably do this using dbc.tables, but this works for now
        boolean result = false;
        try {
            Statement statement = _connection.createStatement();
            statement.execute("SELECT 1 FROM " + table);
            result = true;
            statement.close();
        } catch (SQLException throwables) {
            LOGGER.info("Table " + table + " does not exist. Attempting to create");
        }
        return result;
    }

    @Override
    public void close() {
        try {
            if (_batchStatement != null) {
                _batchStatement.executeBatch();
                _batchStatement.close();
            }
            _connection.close();
        } catch (SQLException throwables) {
            LOGGER.error("Error closing connection to teradata", throwables);
        }
    }

    @Override
    public void createAnnotationTable(String table) {
        String query = Util.getCreateTableQuery(table, _params.getCreateTableSuffix(), _params.getDocumentIdColAndDatatype());
        LOGGER.info("Executing query: " + query);
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
        int[] updateCounts = new int[0];
        if (useBatchUpdates()) {
            try {
                updateCounts = _batchStatement.executeBatch();
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
        try {
            _batchStatement.addBatch(query);
        } catch (SQLException throwables) {
            LOGGER.error("Error adding query to batch: " + query, throwables);
        }
    }
}
