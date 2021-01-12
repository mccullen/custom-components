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
        String url = "jdbc:teradata://EDWP/CHARSET=UTF8,ENCRYPTDATA=ON,TCP=KEEPALIVE,TMODE=ANSI,LOGMECH=TDNEGO";
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
            Connection con = DriverManager.getConnection(url);
            System.out.println(" Connection to Teradata established. \n");

            Statement statement = con.createStatement();
            statement.executeQuery(query);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
