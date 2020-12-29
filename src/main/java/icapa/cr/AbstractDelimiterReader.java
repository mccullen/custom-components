package icapa.cr;

import icapa.models.DelimiterReaderParams;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

public abstract class AbstractDelimiterReader extends JCasCollectionReader_ImplBase {
    static private final Logger LOGGER = Logger.getLogger(AbstractDelimiterReader.class.getName());

    static public final String PARAM_ROW_START = "RowStart";
    @ConfigurationParameter(
        name = PARAM_ROW_START,
        description = "Row start. Inclusive and starts at 0.",
        mandatory = false,
        defaultValue = "0"
    )
    private int _rowStart;

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

    static public final String PARAM_DOCUMENT_ID_COL_NAME = "DocumentIdColumnName";
    @ConfigurationParameter(
        name = PARAM_DOCUMENT_ID_COL_NAME,
        description = "Document Id column name",
        mandatory = false,
        defaultValue = "documentId"
    )
    private String _documentIdColName;

    static public final String PARAM_DELIMITER = "Delimiter";
    @ConfigurationParameter(
        name = PARAM_DELIMITER,
        description = "Delimiter. Note that this can be a UTF-16 unicode character. Just specify the UTF-16 hex. " +
                      "For example, if you want to use a character that you will not have to escape since it can't " +
                      "be typed, you could use \\u0001 (start of heading)",
        mandatory = false,
        defaultValue = ","
    )
    private char _delimiter;

    // Private fields
    private DelimiterReaderParams _params = new DelimiterReaderParams();

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        LOGGER.info("initializeing");
        _params.setRowStart(_rowStart);
        _params.setRowEnd(_rowEnd);
        _params.setNoteColumnName(_noteColName);
        _params.setDocumentIdColumnName(_documentIdColName);
        _params.setDelimiter(_delimiter);
    }

    public DelimiterReaderParams getParams() {
        return _params;
    }
}
