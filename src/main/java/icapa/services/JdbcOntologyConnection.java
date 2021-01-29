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
            _connection.close();
        } catch (SQLException throwables) {
            LOGGER.error("Error closing connection to teradata", throwables);
        }
    }
}
