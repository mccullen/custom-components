package icapa.models;

import java.io.Writer;

public class OntologyWriterParams {
    private Writer writer;
    private char delimiter;
    private boolean keepAll;

    public boolean isKeepAll() {
        return keepAll;
    }

    public void setKeepAll(boolean keepAll) {
        this.keepAll = keepAll;
    }

    public Writer getWriter() {
        return writer;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }
}
