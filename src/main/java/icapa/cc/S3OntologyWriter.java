package icapa.cc;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import icapa.services.CasConsumer;
import icapa.services.OntologyCasConsumer;
import javassist.bytecode.ByteArray;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.*;

public class S3OntologyWriter extends JCasAnnotator_ImplBase {
    static public final String PARAM_BUCKET = "Bucket";
    @ConfigurationParameter(
        name = PARAM_BUCKET,
        description = "Bucket",
        defaultValue = "*",
        mandatory = true
    )
    private String _bucket;

    static public final String PARAM_KEY = "Key";
    @ConfigurationParameter(
        name = PARAM_BUCKET,
        description = "Key",
        defaultValue = "*",
        mandatory = true
    )
    private String _key;

    private CasConsumer _writer;
    private BufferedOutputStream _bufferedOutputStream;
    private ByteArrayOutputStream _byteArrayOutputStream;

    public S3OntologyWriter() {
    }

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        setDependencies();
    }

    private void setDependencies() {
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        //InputStream inputStream = s3Object.getObjectContent();
        if (s3Client.doesObjectExist(_bucket, _key)) {
            S3Object s3Object = s3Client.getObject(_bucket, _key);
            _byteArrayOutputStream = new ByteArrayOutputStream();
            _bufferedOutputStream = new BufferedOutputStream(_byteArrayOutputStream);
            Writer writer = new OutputStreamWriter(_bufferedOutputStream);
            _writer = OntologyCasConsumer.from(writer);
        } else {
            Bucket bucket = s3Client.createBucket(_bucket);
        }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        _writer.process(jCas);
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            _writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        byte[] bytes = _byteArrayOutputStream.toByteArray();
        InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(bytes));
        ObjectMetadata metadata = new ObjectMetadata();
        s3Client.putObject(_bucket, _key, inputStream, metadata);
    }
}
