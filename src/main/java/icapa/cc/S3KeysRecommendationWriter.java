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

    private S3RecommendationWriterService _recommendationWriter;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        // You can't append to s3, so just force it to false
        setAppend(false);
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        _recommendationWriter = S3RecommendationWriterService.fromParams(_bucket, _keyPrefix, getDelimiter(), getAppend());
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

    @Override
    public void writeHeaderLine() {
        _recommendationWriter.writeHeaderLine();
    }

    @Override
    public void writeRecommendationLine(Recommendation recommendation) {
        _recommendationWriter.writeRecommendationLine(recommendation);
    }
}
