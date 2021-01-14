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
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Playground {
    public static void main(String[] args) throws Exception {
        String encoded = "jdbc:teradata://EDWP/CHARSET%3DUTF8,ENCRYPTDATA%3DON,TCP%3DKEEPALIVE,TMODE%3DANSI,LOGMECH%3DKRB5";
        String decoded = URLDecoder.decode(encoded, "UTF-8");
        System.out.println(decoded);
    }
}
