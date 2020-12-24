package icapa.cr;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import icapa.services.CollectionReader;
import icapa.services.DelimiterCollectionReader;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class S3DelimiterReader extends JCasCollectionReader_ImplBase {

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

    static public final String PARAM_ROW_START = "RowStart";
    @ConfigurationParameter(
        name = PARAM_ROW_START,
        description = "Row start. Inclusive and starts at 0.",
        mandatory = false,
        defaultValue = "0"
    )
    private int _rowStart;

    static public final String PARAM_ROW_END = "RowEnd";
    @ConfigurationParameter(
        name = PARAM_ROW_END,
        description = "Row end. Inclusive",
        mandatory = false,
        defaultValue = "-1" // If -1, will read until the end
    )
    private int _rowEnd;

    static public final String PARAM_NOTE_COL_NAME = "NoteColumnName";
    @ConfigurationParameter(
        name = PARAM_NOTE_COL_NAME,
        description = "Note Column name",
        mandatory = false,
        defaultValue = "note"
    )
    private String _noteColName;

    private CollectionReader _reader;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        S3Object s3Object = s3Client.getObject(_bucket, _key);
        InputStream inputStream = s3Object.getObjectContent();
        Reader reader = new InputStreamReader(inputStream);
        _reader = DelimiterCollectionReader.from(reader, _rowStart, _rowEnd, _noteColName);
    }

    @Override
    public void getNext(JCas jCas) throws IOException, CollectionException {
        _reader.readNext(jCas);
    }

    @Override
    public boolean hasNext() throws IOException, CollectionException {
        return _reader.hasNext();
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
