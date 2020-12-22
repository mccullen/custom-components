package icapa;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class MyCSVReader extends JCasCollectionReader_ImplBase {
    static private final Logger LOGGER = Logger.getLogger( "CSVReader" );

    static public final String PARAM_INPUT_FILE = "InputFile";
    @ConfigurationParameter(
            name = PARAM_INPUT_FILE,
            description = "Input file",
            defaultValue = "*",
            mandatory = false
    )
    private String _inputFile;

    static public final String PARAM_ROW_START = "RowStart";
    @ConfigurationParameter(
            name = PARAM_ROW_START,
            description = "Row start",
            mandatory = false
    )
    private int _rowStart;

    private int _currentRow;

    static public final String PARAM_ROW_END = "RowEnd";
    @ConfigurationParameter(
            name = PARAM_ROW_END,
            description = "Row end",
            mandatory = false
    )
    private int _rowEnd;

    static public final String PARAM_NOTE_COL_NAME = "NoteColumnName";
    @ConfigurationParameter(
            name = PARAM_NOTE_COL_NAME,
            description = "Note Column name",
            mandatory = false
    )
    private String _noteColName;

    private CSVReader _csvReader;
    private int _docsProcessed = 0;
    private int _noteColIndex = 0;

    public MyCSVReader() {
        LOGGER.info("Ctor");
        LOGGER.info(_inputFile);

    }

    /**
     * Called after construtor. At this point, your configuration params will be set.
     */
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        LOGGER.info("initializeing");
        LOGGER.info(_inputFile);
        _currentRow = _rowStart;
        try {
            _csvReader = new CSVReaderBuilder(new FileReader(_inputFile)).build();
            String[] headers = _csvReader.readNext();
            // Set _noteColIndex to the index of the specified header
            while (_noteColIndex < headers.length && !headers[_noteColIndex].equals(_noteColName)) {
                ++_noteColIndex;
            }
            _csvReader.skip(_rowStart);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public void getNext(JCas jCas) throws IOException, CollectionException {
        IdentifiedAnnotation a = new IdentifiedAnnotation(jCas);
        a.getId();
        // Set document text
        LOGGER.info("getnext........................................");
        try {
            String[] line = _csvReader.readNext();
            if (line != null) {
                String text = line[_noteColIndex];
                //LOGGER.info(text);
                jCas.setDocumentText(text);
            } else {
                jCas.setDocumentText("");
            }
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
        ++_currentRow;
    }

    public boolean hasNext() throws IOException, CollectionException {
        LOGGER.info("hasnext........................................");
        LOGGER.info(_currentRow);
        LOGGER.info(_rowEnd);
        boolean result = _currentRow <= _rowEnd;
        LOGGER.info(result);
        ++_docsProcessed;
        LOGGER.info("docs processed: " + _docsProcessed);
        return result;
    }

    public Progress[] getProgress() {
        return new Progress[0];
    }
}
