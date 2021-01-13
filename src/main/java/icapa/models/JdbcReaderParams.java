package icapa.models;

import org.apache.uima.fit.descriptor.ConfigurationParameter;

public class JdbcReaderParams {
    private String sqlStatement;
    private String documentTextColName;
    private String driverClassName;
    private String URL;
    private String username;
    private String password;

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

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}