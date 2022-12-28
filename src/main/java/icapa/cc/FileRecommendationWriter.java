package icapa.cc;

import icapa.models.Recommendation;
import icapa.services.FileRecommendationWriterService;
import icapa.services.RecommendationWriterService;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileRecommendationWriter extends AbstractRecommendationWriter {
    private static final Logger LOGGER = Logger.getLogger(FileRecommendationWriter.class.getName());

    public static final String PARAM_OUTPUT_FILE = "OutputFile";
    @ConfigurationParameter(
        name = PARAM_OUTPUT_FILE,
        defaultValue = "",
        mandatory = true
    )
    private String _outputFile;

    private RecommendationWriterService _recommendationWriter;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        _recommendationWriter = FileRecommendationWriterService
            .fromParams(_outputFile, super.getDelimiter(), super.getAppend());
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        super.process(jCas);
        // Always append after you write the first record. Even if append was false,
        // you only want the headers written the first time super.process is called.
        super.setAppend(true);
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
