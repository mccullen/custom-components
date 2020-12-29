package icapa.models;

import java.io.Reader;

public class DelimiterReaderParams {
    private Reader reader;
    private int rowStart;
    private int rowEnd;
    private String noteColumnName;
    private String documentIdColumnName;
    private char delimiter;

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public Reader getReader() {
        return reader;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public int getRowStart() {
        return rowStart;
    }

    public void setRowStart(int rowStart) {
        this.rowStart = rowStart;
    }

    public int getRowEnd() {
        return rowEnd;
    }

    public void setRowEnd(int rowEnd) {
        this.rowEnd = rowEnd;
    }

    public String getNoteColumnName() {
        return noteColumnName;
    }

    public void setNoteColumnName(String noteColumnName) {
        this.noteColumnName = noteColumnName;
    }

    public String getDocumentIdColumnName() {
        return documentIdColumnName;
    }

    public void setDocumentIdColumnName(String documentIdColumnName) {
        this.documentIdColumnName = documentIdColumnName;
    }
}
