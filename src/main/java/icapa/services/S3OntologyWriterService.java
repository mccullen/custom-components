package icapa.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import icapa.models.S3OntologyWriterParams;
import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;

import java.io.*;

/**
 * This is a very thin wrapper around another analysis component.
 * It delegates most responsabiilities to the delegate but has some additional s3
 * initialization and closing logic.
 * */
public class S3OntologyWriterService implements AnalysisEngine {
    private AnalysisEngine _analysisEngine;
    private String _bucket;
    private String _key;
    private ByteArrayOutputStream _byteArrayOutputStream;

    public static S3OntologyWriterService from(S3OntologyWriterParams params) {
        S3OntologyWriterService result = new S3OntologyWriterService();
        result._analysisEngine = params.getAnalysisEngine();
        result._bucket = params.getBucket();
        result._key = params.getKey();
        result._byteArrayOutputStream = params.getByteArrayOutputStream();
        return result;
    }

    @Override
    public void initialize(UimaContext context) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        if (s3Client.doesObjectExist(_bucket, _key)) {
            // Object exists, so delete it so you can write to it later
            s3Client.deleteObject(_bucket, _key);
        } else {
            // Object does NOT exist, so check if bucket exists
            if (!s3Client.doesBucketExistV2(_bucket)) {
                // Bucket does NOT exist, so create it
                s3Client.createBucket(_bucket);
            }
        }
        _analysisEngine.initialize(context);
    }

    @Override
    public void process(JCas jCas) {
        _analysisEngine.process(jCas);
    }

    @Override
    public void close() throws IOException {
        _analysisEngine.close();
        // S3 only allows you to upload using an InputStream (not an OutputStream) so we have to copy our
        // OutputStream to an InputStream.
        // TODO: Consider using PipedOutputStream to convert to input strem here: https://stackoverflow.com/questions/5778658/how-to-convert-outputstream-to-inputstream
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        byte[] bytes = _byteArrayOutputStream.toByteArray();
        InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(bytes));
        ObjectMetadata metadata = new ObjectMetadata();
        s3Client.putObject(_bucket, _key, inputStream, metadata);
    }
}
