package icapa.cr;

import icapa.Util;
import icapa.services.CollectionReader;
import icapa.services.DelimiterReaderService;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DelimiterReader extends AbstractDelimiterReader {
    static private final Logger LOGGER = Logger.getLogger(DelimiterReader.class.getName());

    // Configuration Parameters
    static public final String PARAM_INPUT_FILE = "InputFile";
    @ConfigurationParameter(
        name = PARAM_INPUT_FILE,
        mandatory = false,
        defaultValue = "*"
    )
    private String _inputFile;

    // Private fields
    private CollectionReader _reader;

    public DelimiterReader() {
        LOGGER.info("Constructing Delimiter reader");
    }

    /**
     * Called after construtor. At this point, your configuration params will be set.
     */
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        try {
            // The abstract class will set the params that it can. But it can't set the reader until
            // it gets the _inputFile so set it here.
            getParams().setReader(new FileReader(_inputFile));
            _reader = DelimiterReaderService.from(getParams());
            _reader.initialize();
        } catch (FileNotFoundException e) {
            LOGGER.error("Error loading input file " + _inputFile, e);
        }
    }

    @Override
    public void getNext(JCas jCas) {
        _reader.readNext(jCas);
    }

    @Override
    public boolean hasNext() {
        return Util.hasNext(_reader);
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[0];
    }
}
