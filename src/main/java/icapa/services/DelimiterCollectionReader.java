package icapa.services;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.io.Reader;

public class DelimiterCollectionReader implements CollectionReader {
    public static String[] currentLine;
    static private final Logger LOGGER = Logger.getLogger(DelimiterCollectionReader.class.getName());
    private Reader _reader;
    private int _rowStart = 0;
    private int _rowEnd = 0;
    private int _currentRow;
    private CSVReader _csvReader;
    private int _noteColIndex;
    private String _noteColName;
    private int _docsProcessed = 0;

    public DelimiterCollectionReader() {
    }

    public static DelimiterCollectionReader from(Reader reader, int rowStart, int rowEnd, String noteColName) {
        DelimiterCollectionReader result = new DelimiterCollectionReader();
        result._reader = reader;
        result._rowStart = rowStart;
        result._currentRow = rowStart;
        result._rowEnd = rowEnd;
        result._noteColName = noteColName;
        // TODO: Remove hard dependency on CSVReader. Create a wrapper class/interface
        result._csvReader = new CSVReaderBuilder(reader).build();
        // Skip to the right row
        String[] headers = new String[0];
        try {
            headers = result._csvReader.readNext();
            // Set _noteColIndex to the index of the specified header
            while (result._noteColIndex < headers.length && !headers[result._noteColIndex].equals(result._noteColName)) {
                ++result._noteColIndex;
            }
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
                documentId.setDocumentID(String.valueOf(_docsProcessed));
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
