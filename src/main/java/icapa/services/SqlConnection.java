package icapa.services;

import icapa.models.Ontology;

import java.sql.ResultSet;

public interface SqlConnection {
    ResultSet executeQuery(String query);
    int executeUpdate(String query);
    boolean tableExists(String table);
    void createAnnotationTable(String table);
    void insertOntologyIntoTable(Ontology ontology, String table);
    void close();
}
