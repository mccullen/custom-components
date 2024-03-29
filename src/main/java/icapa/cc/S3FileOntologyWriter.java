package icapa.cc;

import icapa.services.AnalysisEngine;
import icapa.services.OntologyConsumer;
import icapa.services.OntologyWriterService;
import icapa.services.S3OntologyConsumer;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;

public class S3FileOntologyWriter extends AbstractFileOntologyWriter {
    private static final Logger LOGGER = Logger.getLogger(S3FileOntologyWriter.class.getName());

    static public final String PARAM_BUCKET = "Bucket";

    // Configuration parameters
    @ConfigurationParameter(
        name = PARAM_BUCKET,
        defaultValue = "*",
        mandatory = true
    )
    private String _bucket;

    static public final String PARAM_KEY = "Key";
    @ConfigurationParameter(
        name = PARAM_KEY,
        defaultValue = "*",
        mandatory = true
    )
    private String _key;

    static public final String PARAM_PROD = "Prod";
    @ConfigurationParameter(
        name = PARAM_PROD,
        defaultValue = "true",
        mandatory = false
    )
    private boolean _prod;


    // Private variables
    private AnalysisEngine _writer;

    public S3FileOntologyWriter() {
    }

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        LOGGER.info("Initializing S3FileOntology Writer");
        LOGGER.info("Bucket: " + _bucket);
        LOGGER.info("Key: " + _key);
        LOGGER.info("Prod: " + _prod);
        setWriter();
        _writer.initialize(context);
    }

    private void setWriter() {
        OntologyConsumer ontologyConsumer = S3OntologyConsumer.from(_bucket, _key, getParams().getDelimiter(), _prod);
        _writer = OntologyWriterService.fromParams(ontologyConsumer, getParams().isKeepAll());
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        super.process(jCas);
        _writer.process(jCas);
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            _writer.close();
        } catch (IOException e) {
            LOGGER.error("Error closing writer", e);
        }
    }
}
