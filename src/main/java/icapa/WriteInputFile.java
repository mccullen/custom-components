package icapa;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class WriteInputFile {
    public static void main(String[] args) throws Exception {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", "us-east-1"))
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("test", "test")));
        builder.setPathStyleAccessEnabled(true);
        AmazonS3 s3Client = builder.build();
        String bucketName = "bucket";
        Bucket bucket = s3Client.createBucket(bucketName);
        List<Bucket> buckets = s3Client.listBuckets();
        // Now write to the bucket
        String key = "input";
        InputStream inputStream = new FileInputStream(new File("C:/root/tmp/mimiciii/pyctakes_notes_in.mimiciii.top3.csv"));
        ObjectMetadata metadata = new ObjectMetadata();
        s3Client.putObject(bucketName, key, inputStream, metadata);
    }
}
