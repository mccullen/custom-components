package icapa.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import icapa.Util;
import icapa.models.Ontology;
import org.apache.log4j.Logger;

import java.io.*;

public class S3OntologyConsumer implements OntologyConsumer {
    public static final Logger LOGGER = Logger.getLogger(S3OntologyConsumer.class.getName());
    private String _bucket;
    private String _key;
    private OntologyConsumer _ontologyConsumer;
    private Writer _writer;
    private boolean _append;
    private ByteArrayOutputStream _byteArrayOutputStream;
    private boolean _prod;
    private int _size;

    public static S3OntologyConsumer from(String bucket, String key, char delimiter, boolean prod) {
        S3OntologyConsumer result = new S3OntologyConsumer();
        result._bucket = bucket;
        result._key = key;
        result._prod = prod;
        result.setWriter();
        result._append = false; // you can't append to s3 buckets, so just hard code it to false
        result.createBucketIfAbsent();
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

    private void createBucketIfAbsent() {
        AmazonS3 s3Client = Util.getS3Client(_prod);
        // Only check if bucket exists. Do not worry about key until you actually begin writing b/c
        // the S3KeysOntology writer will setKey later
        //if (!s3Client.doesObjectExist(_bucket, _key) && !s3Client.doesBucketExistV2(_bucket)) {
        if (!s3Client.doesBucketExistV2(_bucket)) {
            s3Client.createBucket(_bucket);
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

    public void setKey(String key) {
        this._key = key;
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return _byteArrayOutputStream;
    }

    @Override
    public void close() {
        LOGGER.info("Closing s3 ontology writer");
        _ontologyConsumer.close();
        // S3 only allows you to upload using an InputStream (not an OutputStream) so we have to copy our
        // OutputStream to an InputStream.
        // Credit for this conversion strategy: https://stackoverflow.com/questions/5778658/how-to-convert-outputstream-to-inputstream

        //Alternative (less efficient) conversion strategy
        AmazonS3 s3Client = Util.getS3Client(_prod);
        int minLen = String.join(",", Util.getOntologyConceptHeaders()).getBytes().length + 1;
        Util.writeOutputToS3(_byteArrayOutputStream, s3Client, _bucket, _key, minLen);


        /*
        // TODO: This may need to be refactored to work with large files. Maybe do a multipart upload somehow?
        try {
            LOGGER.info("trying to write file to output");
            AmazonS3 s3Client = Util.getS3Client(_prod);
            TransferManager tm = TransferManagerBuilder.standard().withS3Client(s3Client).build();
            PipedOutputStream out = new PipedOutputStream();
            PipedInputStream in = new PipedInputStream(out);
            Thread thread = new Thread(() -> {
                try {
                    LOGGER.info("Writing to output stream");
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
            LOGGER.info("starting thread");
            thread.start();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(_byteArrayOutputStream.size());
            // TODO: This hangs. It finishes uploading, but never exists. Not sure why.
            LOGGER.info("Uploading...");
            Upload upload = tm.upload(_bucket, _key, in, metadata);
            //boolean d = upload.isDone();
            //System.out.println(d);
        } catch (Exception e) {
            LOGGER.error("ERROR loading piped input stream", e);
        }
        */
        /*
        ((Runnable)() -> {

        }).run();

         */
    }
}
