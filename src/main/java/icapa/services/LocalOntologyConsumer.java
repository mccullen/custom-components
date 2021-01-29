package icapa.services;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import icapa.Util;
import icapa.models.Ontology;
import net.sf.cglib.core.Local;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class LocalOntologyConsumer implements OntologyConsumer {
    public static final Logger LOGGER = Logger.getLogger(LocalOntologyConsumer.class.getName());

    private ICSVWriter _csvWriter;
    private Map<String, Integer> _headerToIndex;
    private String[] _headers;
    private Writer _writer;
    private char _delimiter;

    public static LocalOntologyConsumer from(Writer writer, char delimiter) {
        LocalOntologyConsumer result = new LocalOntologyConsumer();
        result._writer = writer;
        result._delimiter = delimiter;
        return result;
    }

    @Override
    public void createAnnotationTableIfAbsent() {
        try {
            _csvWriter = new CSVWriterBuilder(_writer).withSeparator(_delimiter).build();
            _headers = Util.getOntologyConceptHeaders();
            _headerToIndex = Util.getKeyToIndex(_headers);
            _csvWriter.writeNext(_headers, false);
        } catch (Exception e) {
            LOGGER.error("Error initializing ontology writer service", e);
        }
    }

    @Override
    public void insertOntologyIntoAnnotationTable(Ontology ontology) {
        String[] row = Util.getOntologyAsStringArray(ontology, _headerToIndex);
        _csvWriter.writeNext(row, false);
    }

    @Override
    public void close() {
        try {
            _csvWriter.close();
        } catch (IOException e) {
            LOGGER.error("Error closing csv writer", e);
        }
    }
}
