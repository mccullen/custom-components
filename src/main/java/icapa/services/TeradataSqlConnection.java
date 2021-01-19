package icapa.services;

import icapa.Util;
import icapa.models.TeradataParams;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Map;

public class TeradataSqlConnection implements SqlConnection {
    private static final Logger LOGGER = Logger.getLogger(TeradataSqlConnection.class.getName());

    private Connection _connection;
    private TeradataParams _params;

    public static TeradataSqlConnection fromParams(TeradataParams params) {
        TeradataSqlConnection engine = new TeradataSqlConnection();
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
        return false;
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
