package icapa.models;

public class JdbcOntologyWriterParams {
    private String table;
    private String driverClassName;
    private String documentIdColumn;
    private String url;
    private String username;
    private String password;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getDocumentIdColumn() {
        return documentIdColumn;
    }

    public void setDocumentIdColumn(String documentIdColumn) {
        this.documentIdColumn = documentIdColumn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
