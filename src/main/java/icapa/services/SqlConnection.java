package icapa.services;

import icapa.models.Ontology;

import java.sql.ResultSet;

public interface SqlConnection {
    ResultSet executeQuery(String query);
    void close();
}
