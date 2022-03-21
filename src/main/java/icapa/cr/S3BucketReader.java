package icapa.cr;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.*;
import icapa.Util;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
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
import java.util.Iterator;
import java.util.stream.Collectors;

public class S3BucketReader extends JCasCollectionReader_ImplBase {
    static private final Logger LOGGER = Logger.getLogger(S3BucketReader.class.getName());

    static public final String PARAM_BUCKET = "Bucket";
    @ConfigurationParameter(
        name = PARAM_BUCKET,
        description = "Bucket from which to read documents",
        mandatory = true,
        defaultValue = ""
    )
    private String _bucket;

    static public final String PARAM_PREFIX = "Prefix";
    @ConfigurationParameter(
        name = PARAM_PREFIX,
        description = "Prefix to use for getting notes from the given bucket",
        mandatory = false,
        defaultValue = ""
    )
    private String _prefix;

    static public final String PARAM_PROD = "Prod";
    @ConfigurationParameter(
        name = PARAM_PROD,
        mandatory = false,
        defaultValue = "true"
    )
    private boolean _prod;


    static public final String PARAM_TEST = "Test";
    @ConfigurationParameter(
        name = PARAM_TEST,
        description = "Set to true if testing on localstack for testing",
        mandatory = false,
        defaultValue = "false"
    )
    private boolean _test;

    private AmazonS3 _s3Client;
    private Iterator<S3ObjectSummary> _objectSummaries;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        if (_test) {
            _s3Client = Util.getS3Client(_prod);
        } else {
            _s3Client = AmazonS3ClientBuilder.defaultClient();
        }
        S3Objects s3Objects;
        if (_prefix != null && !_prefix.equals("")) {
            s3Objects = S3Objects.withPrefix(_s3Client, _bucket, _prefix);
        } else {
            s3Objects = S3Objects.inBucket(_s3Client, _bucket);
        }
        _objectSummaries = s3Objects.iterator();
    }

    @Override
    public void getNext(JCas jCas) {
        S3ObjectSummary objectSummary = _objectSummaries.next();

        System.out.printf(" - %s (size: %d)\n", objectSummary.getKey(), objectSummary.getSize());
        S3Object object = _s3Client.getObject(new GetObjectRequest(objectSummary.getBucketName(), objectSummary.getKey()));
        InputStream inputStream = object.getObjectContent();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String documentText = bufferedReader.lines().collect(Collectors.joining("\n"));
        jCas.setDocumentText(documentText);
        DocumentID documentID = new DocumentID(jCas);
        String id = objectSummary.getKey();
        documentID.setDocumentID(id);
        documentID.addToIndexes();
    }

    @Override
    public boolean hasNext() {
        return _objectSummaries.hasNext();
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[0];
    }
}
