package icapa.models;

import icapa.services.SqlConnection;
import org.apache.uima.fit.descriptor.ConfigurationParameter;

public class JdbcReaderParams {
    private String sqlStatement;
    private String documentTextColName;
    private String documentIdColName;
    private SqlConnection sqlConnection;

    public SqlConnection getSqlConnection() {
        return sqlConnection;
    }

    public void setSqlConnection(SqlConnection sqlConnection) {
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