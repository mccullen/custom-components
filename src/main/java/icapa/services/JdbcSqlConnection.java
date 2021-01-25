package icapa.services;

import icapa.Util;
import icapa.models.HeaderProperties;
import icapa.models.JdbcParams;
import icapa.models.Ontology;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.List;

public class JdbcSqlConnection implements SqlConnection {
    private static final Logger LOGGER = Logger.getLogger(JdbcSqlConnection.class.getName());

    private Connection _connection;
    private JdbcParams _params;

    public static JdbcSqlConnection fromParams(JdbcParams params) {
        JdbcSqlConnection engine = new JdbcSqlConnection();
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
    }


    @Override
    public ResultSet executeQuery(String query) {
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
    public void insertOntologyIntoTable(Ontology ontology, String table) {
        String query = Util.getInsertQuery(table, ontology, _params.getDocumentIdColAndDatatype());
        LOGGER.info("Executing query: " + query);
        int nRowsAffected = executeUpdate(query);
    }
}
