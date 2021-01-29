package icapa.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import icapa.Util;
import icapa.models.Ontology;

import java.io.*;

// TODO JRM
public class S3OntologyConsumer implements OntologyConsumer {
    private String _bucket;
    private String _key;
    private LocalOntologyConsumer _localOntologyConsumer;

    public static S3OntologyConsumer from(String bucket, String key, char delimiter) {
        S3OntologyConsumer result = new S3OntologyConsumer();
        result._bucket = bucket;
        result._key = key;
        // Set regular ontology writer and byte array output stream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // S3 only allows you to upload using an input stream. So our S3OntologyWriterService _writer
        // needs a reference to the output stream so we can copy it over to an input stream later when close()
        // gets called.
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        Writer writer = new OutputStreamWriter(bufferedOutputStream);
        result._localOntologyConsumer = LocalOntologyConsumer.from(writer, delimiter);
        return result;
    }


    @Override
    public void createAnnotationTableIfAbsent() {
        //AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        AmazonS3 s3Client = Util.getS3Client();
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
        _localOntologyConsumer.createAnnotationTableIfAbsent();
    }

    @Override
    public void insertOntologyIntoAnnotationTable(Ontology ontology) {

    }

    @Override
    public void close() {
        _analysisEngine.close();
        // S3 only allows you to upload using an InputStream (not an OutputStream) so we have to copy our
        // OutputStream to an InputStream.
        // TODO: Consider using PipedOutputStream to convert to input strem here: https://stackoverflow.com/questions/5778658/how-to-convert-outputstream-to-inputstream
        //AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        AmazonS3 s3Client = Util.getS3Client();
        /*
        byte[] bytes = _byteArrayOutputStream.toByteArray();
        InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(bytes));
        ObjectMetadata metadata = new ObjectMetadata();
        s3Client.putObject(_bucket, _key, inputStream, metadata);
         */

        TransferManager tm = TransferManagerBuilder.standard().withS3Client(s3Client).build();
        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = null;
        try {
            in = new PipedInputStream(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            try {
                _byteArrayOutputStream.writeTo(out);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        ObjectMetadata metadata = new ObjectMetadata();
        tm.upload(_bucket, _key, in, metadata);
        /*
        ((Runnable)() -> {

        }).run();

         */
    }
}
