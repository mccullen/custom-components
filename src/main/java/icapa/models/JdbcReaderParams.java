package icapa.models;

import icapa.services.SqlConnection;

public class JdbcReaderParams {
    private String sqlStatement;
    private String documentTextColName;
    private String documentIdColName;
    private SqlConnection sqlConnection;

    public SqlConnection getOntologyConnection() {
        return sqlConnection;
    }

    public void setOntologyConnection(SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
    }

    public String getDocumentIdColName() {
        return documentIdColName;
    }

    public void setDocumentIdColName(String documentIdColName) {
        this.documentIdColName = documentIdColName;
    }

    public String getSqlStatement() {
        return sqlStatement;
    }

    public void setSqlStatement(String sqlStatement) {
        this.sqlStatement = sqlStatement;
    }

    public String getDocumentTextColName() {
        return documentTextColName;
    }

    public void setDocumentTextColName(String documentTextColName) {
        this.documentTextColName = documentTextColName;
    }
}