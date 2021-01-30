package icapa.services;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import icapa.Util;
import icapa.models.Ontology;
import net.sf.cglib.core.Local;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class LocalOntologyConsumer implements OntologyConsumer {
    public static final Logger LOGGER = Logger.getLogger(LocalOntologyConsumer.class.getName());

    private Writer _writer;
    private boolean _fileExists;
    private String _outputFile;
    private OntologyConsumer _ontologyConsumer;

    public static LocalOntologyConsumer from(String outputFile, char delimiter) {
        LocalOntologyConsumer result = new LocalOntologyConsumer();
        result._outputFile = outputFile;
        result.setFileExists();
        result.setWriter();
        result._ontologyConsumer = FileOntologyConsumer.from(result._writer, delimiter, result._fileExists);
        return result;
    }

    private void setWriter() {
        File file = new File(_outputFile);
        try {
            // If file exists, this will append to it. Otherwise it will create
            FileWriter fileWriter = new FileWriter(file, _fileExists);
            _writer = fileWriter;
        } catch (IOException e) {
            LOGGER.error("Error loading file " + _outputFile, e);
        }
    }

    private void setFileExists() {
        if (Files.exists(Paths.get(_outputFile))) {
            _fileExists = true;
        } else {
            _fileExists = false;
        }
    }

    @Override
    public void createAnnotationTableIfAbsent() {
        _ontologyConsumer.createAnnotationTableIfAbsent();
    }

    @Override
    public void insertOntologyIntoAnnotationTable(Ontology ontology) {
        _ontologyConsumer.insertOntologyIntoAnnotationTable(ontology);
    }

    @Override
    public void close() {
        _ontologyConsumer.close();
    }
}
