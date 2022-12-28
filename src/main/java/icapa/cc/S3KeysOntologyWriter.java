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

    static public final String PARAM_SIZE = "Size";
    @ConfigurationParameter(
        name = PARAM_SIZE,
        defaultValue = "0",
        mandatory = false
    )
    private int _size;

    private String _key = "";
    private S3OntologyConsumer _ontologyConsumer;
    private AnalysisEngine _ae;
    private String _documentId;

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
        if (_size == 0) {
            regularProcess(jCas);
        } else {
            batchProcess(jCas);
        }
    }

    private void batchProcess(JCas jCas) {
        _documentId = Util.getDocumentId(jCas);

        if (tableSizeExceeded()) {
            _ae.process(jCas);
            // Flush results
            flush();

            _ontologyConsumer = null;
        } else {
            if (tableNotCreated()) {
                // Create table

                _ontologyConsumer = S3OntologyConsumer.from(_bucket, null, getParams().getDelimiter(), _prod);
                // Crate an AE from that consumer and then process it in accordance with the ontologyConsumer's logic
                _ae = OntologyWriterService.fromParams(_ontologyConsumer, getParams().isKeepAll());
                _ae.initialize(null);

                // Reset key
                _key = _keyPrefix + "/" + _documentId;
            }
            // Insert ontologies into table (not writing yet since size has not exceeded, will write in if block above)
            _ae.process(jCas);
        }
    }

    // TODO clean up
    private void regularProcess(JCas jCas) {
        // Create a separate key for each document
        String documentId = Util.getDocumentId(jCas);
        String key = _keyPrefix + "/" + documentId;

        // Create a new consumer using the unique key for this document
        //OntologyConsumer ontologyConsumer = S3OntologyConsumer.from(_bucket, key, getParams().getDelimiter(), _prod);
        //ontologyConsumer.createAnnotationTableIfAbsent();
        //AnalysisEngine ae = OntologyWriterService.fromParams(ontologyConsumer, getParams().isKeepAll());
        //ae.process(jCas);

        // Create an ontology consumer, which just ensures you have a class which can create the ontology table
        // and insert into it. The S3 consumer also implements a close() method which will flush the rest of the
        // results (see below).
        OntologyConsumer ontologyConsumer = S3OntologyConsumer.from(_bucket, key, getParams().getDelimiter(), _prod);

        // Crate an AE from that consumer and then process it in accordance with the ontologyConsumer's logic
        AnalysisEngine ae = OntologyWriterService.fromParams(ontologyConsumer, getParams().isKeepAll());
        ae.initialize(null); // Creates annotation table if absent, inserting the headers at the first line
        ae.process(jCas); // Inserts ontologies into table

        // Close the AE after processing since you need to write the results to s3 for each document.
        // The close() method on the s3ontologyConsumer basically just writes the results to the key
        try {
            ae.close();
        } catch (IOException e) {
            LOGGER.error("Error closing AE", e);
        }
    }

    private boolean tableNotCreated() {
        return _ontologyConsumer == null;
    }

    private boolean tableSizeExceeded() {
        return _ontologyConsumer != null && _ontologyConsumer.getByteArrayOutputStream().size() > _size;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (_size > 0) {
            flush();
        }
    }

    private void setKey() {
        String prev = _key.substring(_keyPrefix.length() + 1);
        if (_documentId != null) {
            if (!_documentId.equals(prev)) {
                _key += "-" + _documentId;
            }
        }
        _ontologyConsumer.setKey(_key);
    }

    private void flush() {
        if (_ae != null) {
            try {
                setKey();
                _ae.close(); // flush
            } catch (IOException e) {
                LOGGER.error("Error closing AE", e);
            }
        }
    }
}