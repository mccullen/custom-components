package icapa.models;

import icapa.services.OntologyConnection;

public class JdbcReaderParams {
    private String sqlStatement;
    private String documentTextColName;
    private String documentIdColName;
    private OntologyConnection ontologyConnection;

    public OntologyConnection getOntologyConnection() {
        return ontologyConnection;
    }

    public void setOntologyConnection(OntologyConnection ontologyConnection) {
        this.ontologyConnection = ontologyConnection;
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