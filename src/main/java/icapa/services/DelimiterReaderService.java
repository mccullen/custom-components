package icapa.services;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import icapa.Util;
import icapa.models.DelimiterReaderParams;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class DelimiterReaderService implements CollectionReader {
    public static String[] currentLine;
    static private final Logger LOGGER = Logger.getLogger(DelimiterReaderService.class.getName());
    private Reader _reader;
    private int _rowStart = 0;
    private int _rowEnd = 0;
    private int _currentRow;
    private CSVReader _csvReader;
    private Integer _noteColIndex;
    private Integer _documentIdIndex;
    private String _noteColName;
    private int _docsProcessed = 0;
    private Map<String, Integer> _headerToIndex = new HashMap<>();

    public DelimiterReaderService() {
    }

    public static DelimiterReaderService from(DelimiterReaderParams params) {
        DelimiterReaderService result = new DelimiterReaderService();
        result._reader = params.getReader();
        result._rowStart = params.getRowStart();
        result._currentRow = result._rowStart;
        result._rowEnd = params.getRowEnd();
        // TODO: Remove hard dependency on CSVReader. Create a wrapper class/interface
        result._csvReader = new CSVReaderBuilder(result._reader).build();
        // Skip to the right row
        String[] headers = new String[0];
        try {
            headers = result._csvReader.readNext();
            result._headerToIndex = Util.getKeyToIndex(headers);
            // Set _noteColIndex to the index of the specified header
            result._noteColIndex = result._headerToIndex.get(params.getNoteColumnName());
            result._documentIdIndex = result._headerToIndex.get(params.getDocumentIdColumnName());
            result._csvReader.skip(result._rowStart);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void readNext(JCas jCas) {
        try {
            String[] line = _csvReader.readNext();
            currentLine = line;
            if (line != null) {
                String text = line[_noteColIndex];
                //LOGGER.info(text);
                jCas.setDocumentText(text);
                DocumentID documentId = new DocumentID(jCas);
                ++_docsProcessed;
                if (_documentIdIndex == null) {
                    // Index not provided so just use the current document number
                    documentId.setDocumentID(String.valueOf(_docsProcessed));
                } else {
                    String id = line[_documentIdIndex];
                    documentId.setDocumentID(id);
                }
                documentId.addToIndexes();
            } else {
                jCas.setDocumentText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ++_currentRow;
    }

    @Override
    public boolean hasNext() {
        String[] row = new String[0];
        try {
            row = _csvReader.peek();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Has next if ANY of these conditions are true:
        // * User entered a valid (>= 0) RowEnd and you have not read lines up to that point AND there are still rows left to read
        // * User didn't enter a valid RowEnd AND there are still rows left to read (here we are defaulting to reading all the lines)
        boolean result = (_currentRow <= _rowEnd || _rowEnd < 0) && row != null;
        LOGGER.info("docs processed: " + _docsProcessed);
        return result;
    }

    @Override
    public void destroy() {
        try {
            _csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
