import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.awssdkv1.TestUtils;
import cloud.localstack.docker.annotation.IHostNameResolver;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

class Tmp implements IHostNameResolver {

    public Tmp() {
    }
    @Override
    public String getHostName() {
        return "localhost";
    }
}
@RunWith(LocalstackTestRunner.class)
//@LocalstackDockerProperties(services = { "s3" })
@LocalstackDockerProperties(services = {"s3", "sqs", "kinesis"})//, hostNameResolver = Tmp.class)
public class Tests {

    @Test
    public void testLocalS3API() {
        AmazonS3 s3 = TestUtils.getClientS3();
        //List<Bucket> buckets = s3.listBuckets();
        s3.createBucket("hello");
        System.out.println("here");
    }
}
