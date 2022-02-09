package icapa.cc;

import icapa.models.Recommendation;
import icapa.services.RecommendationWriterService;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecommendationWriter extends JCasAnnotator_ImplBase {
    private static final Logger LOGGER = Logger.getLogger(RecommendationWriter.class.getName());

    private static final String PARAM_REGEX = "Regex";
    @ConfigurationParameter(
        name = PARAM_REGEX,
        defaultValue = "",
        mandatory = false
    )
    private String _regex = "(recommend(s|ing|ed)?)|(suggest(s|ing|ed)?)|(consider(s|ing|ed)?)|(advise(s|ing|ed)?)";

    private Pattern _pattern;
    private RecommendationWriterService recommendationWriterService;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        _pattern = Pattern.compile(_regex, Pattern.CASE_INSENSITIVE);
        recommendationWriterService.writeHeaderLine();
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        JCasUtil.select(jCas, Sentence.class).forEach(s -> {
            String sentence = jCas.getDocumentText().substring(s.getBegin(), s.getEnd()).toLowerCase();
            Matcher matcher = _pattern.matcher(sentence);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end(); // Returns offset after start index
                Recommendation recommendation = new Recommendation();
                recommendation.setBeginIndex(start);
                recommendation.setEndIndex(end);
                recommendation.setMatch(sentence.substring(start, start + end));
                recommendation.setSegment(s.getSegmentId());
                recommendation.setSentenceAddress(s.getAddress());
                recommendation.setSentenceNumber(s.getSentenceNumber());
                recommendationWriterService.writeRecommendationLine(recommendation);
            }
        });
    }
}
