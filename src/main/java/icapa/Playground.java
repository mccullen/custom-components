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
import java.util.List;

public class Playground {
    public static void main(String[] args) {
        // endpoint = endpoint == null ? Localstack.INSTANCE.getEndpointSQS() : endpoint;
        //        return (AmazonSQS)((AmazonSQSClientBuilder)((AmazonSQSClientBuilder)AmazonSQSClientBuilder.standard().withEndpointConfiguration(getEndpointConfiguration(endpoint))).withCredentials(getCredentialsProvider())).build();
        //    }
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
            //.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost.localstack.cloud:4566", "us-east-1"))
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", "us-east-1"))
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("test", "test")));
        builder.setPathStyleAccessEnabled(true);
        AmazonS3 s3Client = builder.build();
        String bucketName = "hello";
        Bucket bucket = s3Client.createBucket(bucketName);
        List<Bucket> buckets = s3Client.listBuckets();
        // Now write to the bucket
        String key = "world";
        String content = "Changed contents";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        s3Client.putObject(bucketName, key, inputStream, new ObjectMetadata());

        System.out.println("here");
    }
}
