package icapa.services;

import icapa.Util;
import icapa.models.JdbcParams;
import org.apache.log4j.Logger;

import java.sql.*;

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
                _params.getUrl(),
                _params.getDriverClassName());
        } else {
            _connection = Util.getConnection(_params.getUrl(), _params.getDriverClassName());
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
        String query = "";
        if (_params.getCreateTableSuffix() == null) {
            query = Util.getCreateTableQuery(table);
        } else {
            query = Util.getCreateTableQuery(table, _params.getCreateTableSuffix());
        }
        LOGGER.info("Executing query: " + query);
        executeUpdate(query);
    }
}
