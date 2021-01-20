package icapa.models;

import icapa.services.SqlConnection;

public class JdbcOntologyWriterParams {
    private String table;
    private String documentIdColumn;
    private SqlConnection sqlConnection;

    public SqlConnection getSqlConnection() {
        return sqlConnection;
    }

    public void setSqlConnection(SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getDocumentIdColumn() {
        return documentIdColumn;
    }

    public void setDocumentIdColumn(String documentIdColumn) {
        this.documentIdColumn = documentIdColumn;
    }
}