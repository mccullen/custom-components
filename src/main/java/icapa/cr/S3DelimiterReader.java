package icapa.cr;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import icapa.Util;
import icapa.services.CollectionReader;
import icapa.services.DelimiterReaderService;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class S3DelimiterReader extends AbstractDelimiterReader {

    // Configuration Parameters
    static public final String PARAM_BUCKET = "Bucket";
    @ConfigurationParameter(
        name = PARAM_BUCKET,
        description = "Bucket name",
        defaultValue = "*",
        mandatory = false
    )
    private String _bucket;

    static public final String PARAM_KEY = "Key";
    @ConfigurationParameter(
        name = PARAM_KEY,
        description = "key",
        defaultValue = "*",
        mandatory = false
    )
    private String _key;

    // Private fields
    private CollectionReader _reader;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        //AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        AmazonS3 s3Client = Util.getS3Client();

        S3Object s3Object = s3Client.getObject(_bucket, _key);
        InputStream inputStream = s3Object.getObjectContent();
        Reader reader = new InputStreamReader(inputStream);
        getParams().setReader(reader);
        _reader = DelimiterReaderService.from(getParams());
        _reader.initialize();
    }

    @Override
    public void getNext(JCas jCas) throws IOException, CollectionException {
        _reader.readNext(jCas);
    }

    @Override
    public boolean hasNext() throws IOException, CollectionException {
        return Util.hasNext(_reader);
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[0];
    }

    @Override
    public void destroy() {
        super.destroy();
        _reader.destroy();
    }
}
