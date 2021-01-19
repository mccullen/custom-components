package icapa.services;

import java.sql.ResultSet;

public interface SqlConnection {
    ResultSet executeQuery(String query);
    int executeUpdate(String query);
    boolean tableExists(String table);
    void close();
}
