package icapa.models;

public class JdbcOntologyConsumerParams {
    private String driverClassName;
    private String username;
    private String password;
    private String url;
    private String table;
    private HeaderProperties documentIdColAndDatatype;
    private String createTableSuffix;
    private int batchSize;

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public HeaderProperties getDocumentIdColAndDatatype() {
        return documentIdColAndDatatype;
    }

    public void setDocumentIdColAndDatatype(HeaderProperties documentIdColAndDatatype) {
        this.documentIdColAndDatatype = documentIdColAndDatatype;
    }

    public String getCreateTableSuffix() {
        return createTableSuffix;
    }

    public void setCreateTableSuffix(String createTableSuffix) {
        this.createTableSuffix = createTableSuffix;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
