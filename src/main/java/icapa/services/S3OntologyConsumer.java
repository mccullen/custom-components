package icapa.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import icapa.Util;
import icapa.models.Ontology;
import org.apache.log4j.Logger;

import java.io.*;

// TODO JRM
public class S3OntologyConsumer implements OntologyConsumer {
    public static final Logger LOGGER = Logger.getLogger(S3OntologyConsumer.class.getName());
    private String _bucket;
    private String _key;
    private OntologyConsumer _ontologyConsumer;
    private Writer _writer;
    private boolean _append;
    private ByteArrayOutputStream _byteArrayOutputStream;

    public static S3OntologyConsumer from(String bucket, String key, char delimiter) {
        S3OntologyConsumer result = new S3OntologyConsumer();
        result._bucket = bucket;
        result._key = key;
        result.setWriter();
        result.setAppendAndCreateBucketIfAbsent();
        result._ontologyConsumer = FileOntologyConsumer.from(result._writer, delimiter, result._append);
        return result;
    }

    private void setWriter() {
        // Set regular ontology writer and byte array output stream
        _byteArrayOutputStream = new ByteArrayOutputStream(); // store this for close() method later, where you have to convert to input stream
        // S3 only allows you to upload using an input stream. So our S3OntologyWriterService _writer
        // needs a reference to the output stream so we can copy it over to an input stream later when close()
        // gets called.
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(_byteArrayOutputStream); // Wrap output stream into buffered stream for efficiency
        _writer = new OutputStreamWriter(bufferedOutputStream);

    }

    private void setAppendAndCreateBucketIfAbsent() {
        //AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        AmazonS3 s3Client = Util.getS3Client();
        if (s3Client.doesObjectExist(_bucket, _key)) {
            // If you wanted to delete the bucket and recreate you could do this
            // But you could really shoot yourself in the foot if you delete by accident so it
            // is better to have the user explictly delete on their own. Instead, we will append.
            //s3Client.deleteObject(_bucket, _key);

            // Object exists, so you want to append to it
            _append = true;
        } else {
            // Object does NOT exist, so check if bucket exists
            _append = false;
            if (!s3Client.doesBucketExistV2(_bucket)) {
                // Bucket does NOT exist, so create it
                s3Client.createBucket(_bucket);
            }
        }
    }

    @Override
    public void createAnnotationTableIfAbsent() {
        _ontologyConsumer.createAnnotationTableIfAbsent();
    }

    @Override
    public void insertOntologyIntoAnnotationTable(Ontology ontology) {
        _ontologyConsumer.insertOntologyIntoAnnotationTable(ontology);
    }

    @Override
    public void close() {
        _ontologyConsumer.close();
        // S3 only allows you to upload using an InputStream (not an OutputStream) so we have to copy our
        // OutputStream to an InputStream.
        // Credit for this conversion strategy: https://stackoverflow.com/questions/5778658/how-to-convert-outputstream-to-inputstream

        //AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        /*
        Alternative (less efficient) conversion strategy
        byte[] bytes = _byteArrayOutputStream.toByteArray();
        InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(bytes));
        ObjectMetadata metadata = new ObjectMetadata();
        s3Client.putObject(_bucket, _key, inputStream, metadata);
         */

        // TODO: This may need to be refactored to work with large files. Maybe do a multipart upload somehow?
        try {
            AmazonS3 s3Client = Util.getS3Client();
            TransferManager tm = TransferManagerBuilder.standard().withS3Client(s3Client).build();
            PipedOutputStream out = new PipedOutputStream();
            PipedInputStream in = new PipedInputStream(out);
            Thread thread = new Thread(() -> {
                try {
                    _byteArrayOutputStream.writeTo(out);
                } catch (IOException e) {
                    LOGGER.error("Error writing byte array output stream to piped output", e);
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                            _byteArrayOutputStream.close();
                        } catch (IOException e) {
                            LOGGER.error("Error closing piped output stream", e);
                        }
                    }
                }
            });
            thread.start();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(_byteArrayOutputStream.size());
            tm.upload(_bucket, _key, in, metadata);
        } catch (Exception e) {
            LOGGER.error("ERROR loading piped input stream", e);
        }
        /*
        ((Runnable)() -> {

        }).run();

         */
    }
}
