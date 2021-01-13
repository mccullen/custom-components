package icapa;

import cloud.localstack.awssdkv1.TestUtils;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Playground {
    public static void main(String[] args) {
        // INI file with -d switches
        // Location:
        /*
            -Djavax.security.auth.useSubjectCredsOnly=false
            -Djava.security.auth.login.config=C:\ProgramData\Teradata\jaas.conf
            -Djava.security.krb5.conf=C:\ProgramData\Teradata\krb5.conf
        */
        String url = "jdbc:teradata://EDWP/CHARSET=UTF8,ENCRYPTDATA=ON,TCP=KEEPALIVE,TMODE=ANSI,LOGMECH=KRB5";
        String query = "CREATE TABLE DDAR.Temp (testing INT);";
        try {
            System.out.println("\n Sample T20000JD: \n");
            System.out.println(" Looking for the Teradata JDBC driver... ");
            // Loading the Teradata JDBC driver
            Class.forName("com.teradata.jdbc.TeraDriver");
            System.out.println(" JDBC driver loaded. \n");

            // Attempting to connect to Teradata
            System.out.println(" Attempting to connect to Teradata via" +
                " the JDBC driver...");
            // Creating a Connection object. A Connection represents a session
            // with a specific database. Within the context of a Connection,
            // SQL statements are executed and results are returned.
            // Creating a database connection with the given database URL,
            // user name, and password
            Connection con = null;
            if (args.length > 0) {
                con = DriverManager.getConnection(url, args[0], args[1]);
            } else {
                con = DriverManager.getConnection(url);
            }
            System.out.println(" Connection to Teradata established. \n");

            Statement statement = con.createStatement();
            System.out.println("CREATING TABLE");
            statement.executeQuery(query);
            System.out.println("INSERTING INTO TABLE");
            statement.executeQuery("INSERT INTO DDAR.TEMP (testing) VALUES (2);");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
