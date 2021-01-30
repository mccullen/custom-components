package icapa.services;

import icapa.Util;
import icapa.models.Ontology;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.util.List;

public class OntologyWriterService implements AnalysisEngine {
    private static final Logger LOGGER = Logger.getLogger(OntologyWriterService.class.getName());

    private OntologyConsumer _ontologyConsumer;
    private boolean _keepAll;

    public static AnalysisEngine fromParams(OntologyConsumer ontologyConsumer, boolean keepAll) {
        OntologyWriterService result = new OntologyWriterService();
        result._keepAll = keepAll;
        result._ontologyConsumer = ontologyConsumer;
        return result;
    }

    @Override
    public void initialize(UimaContext context) {
        LOGGER.info("Initializing Jdbc ontology writer service");
        _ontologyConsumer.createAnnotationTableIfAbsent();
    }

    @Override
    public void process(JCas jCas) {
        List<Ontology> ontologies = Util.getOntologies(jCas, _keepAll);
        for (Ontology ontology : ontologies) {
            _ontologyConsumer.insertOntologyIntoAnnotationTable(ontology);
        }
    }

    @Override
    public void close() throws IOException {
        _ontologyConsumer.close();
    }
}
