package icapa.services;

import java.sql.ResultSet;

public interface SqlConnection {
    ResultSet executeQuery(String query);
    boolean tableExists(String table);
    void close();
}
