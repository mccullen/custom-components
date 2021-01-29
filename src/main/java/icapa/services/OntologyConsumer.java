package icapa.services;

import icapa.models.Ontology;

public interface OntologyConsumer {
    void createAnnotationTableIfAbsent();
    void insertOntologyIntoAnnotationTable(Ontology ontology);
    void close();
}
