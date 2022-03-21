package icapa.cc;

import icapa.Util;
import icapa.models.Recommendation;
import icapa.services.RecommendationWriterService;
import org.apache.ctakes.typesystem.type.textsem.EntityMention;
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

public abstract class AbstractRecommendationWriter extends JCasAnnotator_ImplBase {
    private static final Logger LOGGER = Logger.getLogger(FileRecommendationWriter.class.getName());
    private static final String TIMEFRAME_REGEX = "\\w*-?\\w*\\s((years?)|(months?)|(days?)|(weeks?))";

    public static final String PARAM_REGEX = "Regex";
    @ConfigurationParameter(
        name = PARAM_REGEX,
        defaultValue = "(recommend(s|ing|ed)?)|(should be considered)|(further evaluations?)|(advise(s|ing|ed)?)|(follow(\\s*|-)?up)",
        mandatory = false
    )
    private String _regex;

    public static final String PARAM_APPEND = "Append";
    @ConfigurationParameter(
        name = PARAM_APPEND,
        defaultValue = "false",
        mandatory = false
    )
    private boolean _append;

    public static final String PARAM_DELIMITER = "Delimiter";
    @ConfigurationParameter(
        name = PARAM_DELIMITER,
        defaultValue = ",",
        mandatory = false
    )
    private char _delimiter;

    public static final String PARAM_TIMEFRAME_REGEX = "TimeframeRegex";
    @ConfigurationParameter(
        name = PARAM_TIMEFRAME_REGEX,
        defaultValue = TIMEFRAME_REGEX,
        mandatory = false
    )
    private String _timeframeRegex;


    private Pattern _timeframePattern;
    private Pattern _pattern;
    private int _nDocumentsProcessed = 0;

    public abstract void writeHeaderLine();
    public abstract void writeRecommendationLine(Recommendation recommendation);

    public boolean getAppend() {
        return _append;
    }

    public char getDelimiter() {
        return _delimiter;
    }

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        _pattern = Pattern.compile(_regex, Pattern.CASE_INSENSITIVE);
        _timeframePattern = Pattern.compile(_timeframeRegex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        if (!_append && _nDocumentsProcessed == 0) {
            writeHeaderLine();
        }
        JCasUtil.select(jCas, Sentence.class).forEach(s -> {
            String sentence = jCas.getDocumentText().substring(s.getBegin(), s.getEnd());
            String match = getMatch(sentence);

            if (match != null && !match.equals("")) {
                Recommendation recommendation = new Recommendation();
                recommendation.setBeginIndex(s.getBegin());
                recommendation.setEndIndex(s.getEnd());
                recommendation.setSentenceNumber(s.getSentenceNumber());
                recommendation.setSentenceAddress(s.getAddress());
                recommendation.setMatch(match);
                recommendation.setSegment(s.getSegmentId());
                recommendation.setDocumentId(Util.getDocumentId(jCas));
                recommendation.setSentence(sentence);
                recommendation.setTimeframe(getTimeframe(sentence));
                // TODO
                recommendation.setStrength("");
                recommendation.setRecommendationType("");
                recommendation.setPolarity(0);

                writeRecommendationLine(recommendation);
            }
        });
        ++_nDocumentsProcessed;
    }

    private String getTimeframe(String sentence) {
        String timeframe = "";
        Matcher matcher = _timeframePattern.matcher(sentence.toLowerCase());
        if (matcher.find()) {
            timeframe = sentence.substring(matcher.start(), matcher.end());
        }
        return timeframe;
    }

    /**
     * Get match. If more than one match, then bar separate them
     */
    private String getMatch(String sentence) {
        Matcher matcher = _pattern.matcher(sentence.toLowerCase());
        StringBuilder matchBuilder = new StringBuilder("");
        int i = 0;
        while (matcher.find()) {
            if (i > 0) {
                matchBuilder.append("|");
            }
            matchBuilder.append(sentence.substring(matcher.start(), matcher.end()));
            ++i;
        }
        String match = matchBuilder.toString();
        if (!match.equals("") && match.charAt(match.length()-1) == '|') {
            match = match.substring(0, match.length() - 1);
        }
        return match;
    }
}
