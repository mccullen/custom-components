package icapa.cr;

import icapa.services.CollectionReader;
import icapa.services.DelimiterReaderService;
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

public class DelimiterReader extends JCasCollectionReader_ImplBase {
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
        description = "Row start. Inclusive and starts at 0.",
        mandatory = false,
        defaultValue = "0"
    )
    private int _rowStart;

    private int _currentRow;

    static public final String PARAM_ROW_END = "RowEnd";
    @ConfigurationParameter(
        name = PARAM_ROW_END,
        description = "Row end. Inclusive",
        mandatory = false,
        defaultValue = "-1" // If -1, will read until the end
    )
    private int _rowEnd;

    static public final String PARAM_NOTE_COL_NAME = "NoteColumnName";
    @ConfigurationParameter(
        name = PARAM_NOTE_COL_NAME,
        description = "Note Column name",
        mandatory = false,
        defaultValue = "note"
    )
    private String _noteColName;

    private CollectionReader _reader;

    public DelimiterReader() {
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
        try {
            _reader = DelimiterReaderService.from(new FileReader(_inputFile), _rowStart, _rowEnd, _noteColName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void getNext(JCas jCas) throws IOException, CollectionException {
        _reader.readNext(jCas);
    }

    public boolean hasNext() throws IOException, CollectionException {
        LOGGER.info("hasnext........................................");
        return _reader.hasNext();
    }

    public Progress[] getProgress() {
        return new Progress[0];
    }

    @Override
    public void destroy() {
        super.destroy();
        _reader.destroy();
    }
}
