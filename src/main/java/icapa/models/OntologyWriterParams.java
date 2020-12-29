package icapa.models;

import java.io.Writer;

public class OntologyWriterParams {
    private Writer writer;
    private char delimiter;

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
