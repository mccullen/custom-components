package icapa.services;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import icapa.Util;
import icapa.models.Ontology;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class FileOntologyConsumer implements OntologyConsumer {
    public static final Logger LOGGER = Logger.getLogger(FileOntologyConsumer.class.getName());

    private ICSVWriter _csvWriter;
    private Map<String, Integer> _headerToIndex;
    private Writer _writer;
    private char _delimiter;
    private boolean _append;

    public static FileOntologyConsumer from(Writer writer, char delimiter, boolean append) {
        FileOntologyConsumer result = new FileOntologyConsumer();
        result._writer = writer;
        result._delimiter = delimiter;
        result._append = append;
        return result;
    }

    /**
     * Note: It is up to the writer to create the table if absent. So, for example, if the writer
     * is not set to append mode, it will delete the table rather than append to it. It is the
     * responsability of the user of this class to set the writer appropriately so this class
     * appends to already existing files.
     * */
    @Override
    public void createAnnotationTableIfAbsent() {
        try {
            _csvWriter = new CSVWriterBuilder(_writer).withSeparator(_delimiter).build();
            String[] headers = Util.getOntologyConceptHeaders();
            _headerToIndex = Util.getKeyToIndex(headers);
            if (!_append) {
                _csvWriter.writeNext(headers, false);
            }
        } catch (Exception e) {
            LOGGER.error("Error initializing ontology writer service", e);
        }
    }

    @Override
    public void insertOntologyIntoAnnotationTable(Ontology ontology) {
        String[] row = Util.getOntologyAsStringArray(ontology, _headerToIndex);
        _csvWriter.writeNext(row, false);
        try {
            _csvWriter.flush();
        } catch (IOException e) {
            LOGGER.error("Error flushing csv writer.", e);
        }
    }

    @Override
    public void close() {
        try {
            _csvWriter.close(); // flushes results
        } catch (IOException e) {
            LOGGER.error("Error closing csv writer", e);
        }
    }
}
