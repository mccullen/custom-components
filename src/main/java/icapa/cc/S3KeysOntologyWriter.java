package icapa.cc;

import icapa.Util;
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

public class S3KeysOntologyWriter extends AbstractFileOntologyWriter {
    private static final Logger LOGGER = Logger.getLogger(S3KeysOntologyWriter.class.getName());

    static public final String PARAM_BUCKET = "Bucket";
    @ConfigurationParameter(
        name = PARAM_BUCKET,
        defaultValue = "*",
        mandatory = true
    )
    private String _bucket;

    static public final String PARAM_KEY_PREFIX = "KeyPrefix";
    @ConfigurationParameter(
        name = PARAM_KEY_PREFIX,
        defaultValue = "*",
        mandatory = true
    )
    private String _keyPrefix;

    static public final String PARAM_PROD = "Prod";
    @ConfigurationParameter(
        name = PARAM_PROD,
        defaultValue = "true",
        mandatory = false
    )
    private boolean _prod;


    public S3KeysOntologyWriter() {
    }

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        LOGGER.info("Initializing S3KeysOntology Writer");
        LOGGER.info("Bucket: " + _bucket);
        LOGGER.info("Key prefix: " + _keyPrefix);
        LOGGER.info("Prod: " + _prod);
    }

    private void setWriter() {
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        super.process(jCas);
        // Create a separate key for each document
        String documentId = Util.getDocumentId(jCas);
        String key = _keyPrefix + "/" + documentId;

        // Create a new consumer using the unique key for this document
        /*
        OntologyConsumer ontologyConsumer = S3OntologyConsumer.from(_bucket, key, getParams().getDelimiter(), _prod);
        ontologyConsumer.createAnnotationTableIfAbsent();
        AnalysisEngine ae = OntologyWriterService.fromParams(ontologyConsumer, getParams().isKeepAll());
        ae.process(jCas);
         */
        OntologyConsumer ontologyConsumer = S3OntologyConsumer.from(_bucket, key, getParams().getDelimiter(), _prod);
        AnalysisEngine ae = OntologyWriterService.fromParams(ontologyConsumer, getParams().isKeepAll());
        ae.initialize(null);
        ae.process(jCas);

        // Close the AE after processing since you need to write the results to s3 for each document.
        // The close() method on the s3ontologyConsumer basically just writes the results to the key
        try {
            ae.close();
        } catch (IOException e) {
            LOGGER.error("Error closing AE", e);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}