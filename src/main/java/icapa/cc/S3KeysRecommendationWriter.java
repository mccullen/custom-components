package icapa.cc;

import com.amazonaws.services.s3.AmazonS3;
import icapa.Util;
import icapa.models.Recommendation;
import icapa.services.FileRecommendationWriterService;
import icapa.services.RecommendationWriterService;
import icapa.services.S3RecommendationWriterService;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.io.Writer;

public class S3KeysRecommendationWriter extends AbstractRecommendationWriter {
    // Configuration parameters
    @ConfigurationParameter(
        name = S3KeysOntologyWriter.PARAM_BUCKET,
        defaultValue = "*",
        mandatory = true
    )
    private String _bucket;

    @ConfigurationParameter(
        name = S3KeysOntologyWriter.PARAM_KEY_PREFIX,
        defaultValue = "*",
        mandatory = true
    )
    private String _keyPrefix;

    @ConfigurationParameter(
        name = S3KeysOntologyWriter.PARAM_PROD,
        defaultValue = "true",
        mandatory = false
    )
    private boolean _prod;

    @ConfigurationParameter(
        name = S3KeysOntologyWriter.PARAM_SIZE,
        defaultValue = "0",
        mandatory = false
    )
    private int _size;

    private S3RecommendationWriterService _recommendationWriter;
    private String _documentId = "";
    private String _key = "";

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        // You can't append to s3, so just force it to false
        setAppend(false);
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        if (_size == 0) {
            regularProcess(jCas);
        } else {
            batchProcess(jCas);
        }
    }

    private void regularProcess(JCas jCas) throws AnalysisEngineProcessException {
        _recommendationWriter = S3RecommendationWriterService.fromParams(getDelimiter(), getAppend());
        super.process(jCas);
        // Create a separate key for each document
        String documentId = Util.getDocumentId(jCas);
        String key = _keyPrefix + "/" + documentId;
        AmazonS3 s3Client = Util.getS3Client(_prod);
        // TODO: Maybe update AbstractRecommendationWriter and RecommendationWriterService with a close method instead
        // to conform to "best practices". This is simpler for now though
        if (_recommendationWriter.getByteArrayOutputStream().size() > 0) {
            Util.writeOutputToS3(_recommendationWriter.getByteArrayOutputStream(), s3Client, _bucket, key);
            _recommendationWriter.getByteArrayOutputStream().reset();
        }
    }

    private void batchProcess(JCas jCas) throws AnalysisEngineProcessException {
        /*
        String _documentId = Util.getDocumentId(jCas);
        _recommendationWriter = S3RecommendationWriterService.fromParams(getDelimiter(), getAppend());
        super.process(jCas);
        // Create a separate key for each document
        String key = _keyPrefix + "/" + documentId;
         */
        _documentId = Util.getDocumentId(jCas);

        if (_recommendationWriter != null && _recommendationWriter.getByteArrayOutputStream().size() > _size) {
            setAppend(true);
            super.process(jCas);
            flush();
        } else {
            if (tableAbsent()) {
                // initialize writer
                setAppend(false); // Write headers when process() gets called
                _recommendationWriter = S3RecommendationWriterService.fromParams(getDelimiter(), getAppend());
                _key = _keyPrefix + "/" + _documentId;
            } else {
                setAppend(true); // keep appending to same buffer. Do not write header if table is present
            }
            // write rows
            super.process(jCas);
            setAppend(true);
        }
    }

    private void flush() {
        if (_recommendationWriter != null) {
            // Only flush if there is stuff in the writer. If there isn't (component is being destoryed and
            // everything was already flushed in process(), then _recommendationWriter is null and there is
            // nothing to flush
            AmazonS3 s3Client = Util.getS3Client(_prod);
            _key = Util.getUpdatedKey(_key, _keyPrefix, _documentId);
            Util.writeOutputToS3(_recommendationWriter.getByteArrayOutputStream(), s3Client, _bucket, _key);
            _recommendationWriter = null; // Force re-instantiating of this in next batch process
        }
    }

    private boolean tableAbsent() {
        return _recommendationWriter == null;
    }

    @Override
    public void writeHeaderLine() {
        _recommendationWriter.writeHeaderLine();
    }

    @Override
    public void writeRecommendationLine(Recommendation recommendation) {
        _recommendationWriter.writeRecommendationLine(recommendation);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (_size > 0) {
            flush();
        }
    }
}
