package icapa.cc;

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
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        Writer output = new OutputStreamWriter(bufferedOutputStream);
        AnalysisEngine writer = OntologyWriterService.from(output);
        _writer = S3OntologyWriterService.from(writer, _bucket, _key);
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
