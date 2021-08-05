package icapa.cr;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class S3BucketReader extends JCasCollectionReader_ImplBase {
    static private final Logger LOGGER = Logger.getLogger(S3BucketReader.class.getName());

    static public final String PARAM_BUCKET = "Bucket";
    @ConfigurationParameter(
        name = PARAM_BUCKET,
        description = "Bucket from which to read documents",
        mandatory = false,
        defaultValue = "0"
    )
    private String _bucket;

    /*
    static public final String PARAM_SERVICE_ENDPOINT = "ServiceEndpoint";
    @ConfigurationParameter(
        name = PARAM_SERVICE_ENDPOINT,
        description = "Service endpoint where bucket resides",
        mandatory = false,
        defaultValue = "0"
    )
    private int _serviceEndpoint;

    static public final String PARAM_SIGNING_REGION = "SigningRegion";
    @ConfigurationParameter(
        name = PARAM_BUCKET,
        description = "Region where bucket is located",
        mandatory = false,
        defaultValue = "0"
    )
    private int _signingRegion;
     */

    private AmazonS3 _s3Client;
    private ListObjectsV2Result _result;
    private ListObjectsV2Request _req;
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        _s3Client = AmazonS3ClientBuilder.defaultClient();

        // maxKeys is set to 1000 to demonstrate the use of
        // ListObjectsV2Result.getNextContinuationToken()
        ListObjectsV2Request _req = new ListObjectsV2Request().withBucketName(_bucket).withMaxKeys(1000);
    }

    @Override
    public void getNext(JCas jCas) {
        _result = _s3Client.listObjectsV2(_req);

        for (S3ObjectSummary objectSummary : _result.getObjectSummaries()) {
            System.out.printf(" - %s (size: %d)\n", objectSummary.getKey(), objectSummary.getSize());
            S3Object object = _s3Client.getObject(new GetObjectRequest(objectSummary.getBucketName(), objectSummary.getKey()));
            InputStream inputStream = object.getObjectContent();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String documentText = bufferedReader.lines().collect(Collectors.joining("\n"));
            jCas.setDocumentText(documentText);
        }
        // If there are more than maxKeys keys in the bucket, get a continuation token
        // and list the next objects.
        String token = _result.getNextContinuationToken();
        System.out.println("Next Continuation Token: " + token);
        _req.setContinuationToken(token);
    }

    @Override
    public boolean hasNext() {
        return _result.isTruncated();
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[0];
    }
}
