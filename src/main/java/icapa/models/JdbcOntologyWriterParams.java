package icapa.models;

import icapa.services.OntologyConnection;
import icapa.services.OntologyConsumer;

public class JdbcOntologyWriterParams {
    private JdbcWriterParams jdbcWriterParams; // Params inherited from jdbc writer

    // Params specific to JdbcOntologyWriter
    private String table;
    private boolean keepAll;
    private String createTableSuffix;
    private HeaderProperties documentIdColAndDatatype;

    // Services
    private OntologyConsumer ontologyConsumer;

    public OntologyConsumer getOntologyConsumer() {
        return ontologyConsumer;
    }

    public void setOntologyConsumer(OntologyConsumer ontologyConsumer) {
        this.ontologyConsumer = ontologyConsumer;
    }

    public String getCreateTableSuffix() {
        return createTableSuffix;
    }

    public void setCreateTableSuffix(String createTableSuffix) {
        this.createTableSuffix = createTableSuffix;
    }

    public HeaderProperties getDocumentIdColAndDatatype() {
        return documentIdColAndDatatype;
    }

    public void setDocumentIdColAndDatatype(HeaderProperties documentIdColAndDatatype) {
        this.documentIdColAndDatatype = documentIdColAndDatatype;
    }

    public JdbcWriterParams getJdbcWriterParams() {
        return jdbcWriterParams;
    }

    public void setJdbcWriterParams(JdbcWriterParams jdbcWriterParams) {
        this.jdbcWriterParams = jdbcWriterParams;
    }

    public boolean isKeepAll() {
        return keepAll;
    }

    public void setKeepAll(boolean keepAll) {
        this.keepAll = keepAll;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
