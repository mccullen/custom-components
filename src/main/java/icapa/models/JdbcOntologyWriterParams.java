package icapa.models;

import icapa.services.OntologyConnection;

public class JdbcOntologyWriterParams {
    private JdbcWriterParams jdbcWriterParams; // Params inherited from jdbc writer

    // Params specific to JdbcOntologyWriter
    private String table;
    private boolean keepAll;
    private String createTableSuffix;
    private HeaderProperties documentIdColAndDatatype;

    private OntologyConnection ontologyConnection;

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

    public OntologyConnection getOntologyConnection() {
        return ontologyConnection;
    }

    public void setOntologyConnection(OntologyConnection ontologyConnection) {
        this.ontologyConnection = ontologyConnection;
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
