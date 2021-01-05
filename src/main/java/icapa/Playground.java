package icapa;

import cloud.localstack.awssdkv1.TestUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class Playground {
    public static void main(String[] args) {
        AmazonS3 s3Client = TestUtils.getClientS3();
        s3Client.createBucket("hello");
    }
}
