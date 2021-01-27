package icapa.services;

import icapa.models.Ontology;

import java.sql.ResultSet;

public interface OntologyConnection {
    ResultSet executeQuery(String query);
    int executeUpdate(String query);
    boolean tableExists(String table);
    void createAnnotationTable(String table);
    String getOntologyInsertQueryForTable(Ontology ontology, String table);
    boolean supportsBatchUpdates();
    int[] executeBatch();
    void addBatch(String query);
    void close();
}
