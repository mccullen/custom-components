package icapa.models;

/**
 *
 * */
public class SqlConnectionParams {
    // Connection parameters
    private String driverClassName;
    private String username;
    private String password;
    private String url;
    // variables to create annotation table
    private String createTableSuffix;
    private HeaderProperties documentIdColAndDatatype;

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
}
