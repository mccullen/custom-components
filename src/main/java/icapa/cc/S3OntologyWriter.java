package icapa.cc;

import icapa.models.S3OntologyWriterParams;
import icapa.services.AnalysisEngine;
import icapa.services.OntologyWriterService;
import icapa.services.S3OntologyWriterService;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.*;

public class S3OntologyWriter extends AbstractOntologyWriter {
    static public final String PARAM_BUCKET = "Bucket";

    // Configuration parameters
    @ConfigurationParameter(
        name = PARAM_BUCKET,
        description = "Bucket",
        defaultValue = "*",
        mandatory = true
    )
    private String _bucket;

    static public final String PARAM_KEY = "Key";
    @ConfigurationParameter(
        name = PARAM_KEY,
        description = "Key",
        defaultValue = "*",
        mandatory = true
    )
    private String _key;

    // Private variables
    private AnalysisEngine _writer;

    public S3OntologyWriter() {
    }

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        setWriter();
        _writer.initialize(context);
    }

    private void setWriter() {
        // Create an ontology concept writer that writes to a byte stream
        S3OntologyWriterParams s3OntologyWriterParams = new S3OntologyWriterParams();

        // Set regular ontology writer and byte array output stream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // S3 only allows you to upload using an input stream. So our S3OntologyWriterService _writer
        // needs a reference to the output stream so we can copy it over to an input stream later when close()
        // gets called.
        s3OntologyWriterParams.setByteArrayOutputStream(byteArrayOutputStream);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        Writer output = new OutputStreamWriter(bufferedOutputStream);
        getParams().setWriter(output);
        AnalysisEngine writer = OntologyWriterService.from(getParams());
        s3OntologyWriterParams.setAnalysisEngine(writer);

        // Set the bucket and key
        s3OntologyWriterParams.setBucket(_bucket);
        s3OntologyWriterParams.setKey(_key);

        _writer = S3OntologyWriterService.from(s3OntologyWriterParams);
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
    }
}
