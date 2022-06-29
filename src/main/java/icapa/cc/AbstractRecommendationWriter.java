package icapa.cc;

import cc.mallet.util.CommandOption;
import icapa.Util;
import icapa.models.Ontology;
import icapa.models.Recommendation;
import icapa.services.RecommendationWriterService;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.syntax.NumToken;
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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractRecommendationWriter extends JCasAnnotator_ImplBase {
    private static final Logger LOGGER = Logger.getLogger(FileRecommendationWriter.class.getName());
    private static final String TIMEFRAME_REGEX = "annual(ly)?|yearly|monthly|daily|weekly|\\w*-?\\w*\\s*((year(?!ly)s?)|(month(?!ly)s?)|(day(?!ly)s?)|(week(?!ly)s?))";

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
    private Pattern _pattern; // pattern for recommendation
    private Pattern _polarityPattern;
    private String _polarityRegex = "^\\s*no \\s*";
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
        _polarityPattern = Pattern.compile(_polarityRegex, Pattern.CASE_INSENSITIVE);
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
                recommendation.setRecommendationType(getRecommendationType(jCas, s.getBegin(), s.getEnd()));
                recommendation.setStrength(getRecommendationStrength(sentence));
                recommendation.setPolarity(getRecommendationPolarity(sentence));
                // TODO
                //recommendation.setRecommendationGroup();

                writeRecommendationLine(recommendation);
            }
        });
        ++_nDocumentsProcessed;
    }

    private String getRecommendationGroup() {
        // TODO
        return null;
    }

    private String getRecommendationStrength(String sentence) {
        String strength = "";
        String lowerCase = sentence.toLowerCase();
        if (lowerCase.contains("strongly")) {
            strength = "strong";
        } else if (lowerCase.contains("probable")) {
            strength = "probable";
        }
        return strength;
    }

    private int getRecommendationPolarity(String sentence) {
        int polarity = 1;
        String lowerCase = sentence.toLowerCase();
        Matcher matcher = _polarityPattern.matcher(lowerCase);
        if (matcher.find()) {
            polarity = -1;
        }
        return polarity;
    }

    private String getRecommendationType(JCas jCas, int beginIndex, int endIndex) {
        Set<String> recommendationTypes = new HashSet<String>();
        List<Ontology> ontologies = Util.getOntologies(jCas);
        String sentence = jCas.getDocumentText().substring(beginIndex, endIndex);
        for (Ontology o : ontologies) {
            if (o.getBegin() >= beginIndex && o.getEnd() <= endIndex) {
                // Ontology span is w/i bounds of sentence
                if (o.getPreferredText() != null &&
                    o.getCodingScheme() != null && (o.getCodingScheme().equals("ICD10PCS") || o.getCodingScheme().equals("CPT"))) {
                    // Ontology span w/i bounds of sentence and is a procedure code
                    recommendationTypes.add(o.getPreferredText());
                } else if (false) {
                    // TODO: Add other criteria based on key words
                }
            }
        }
        String recommendationType = String.join("|", recommendationTypes);
        return recommendationType == null ? "" : recommendationType;
    }


    private String getTimeframe(String sentence) {
        String timeframe = "";
        Matcher matcher = _timeframePattern.matcher(sentence.toLowerCase());
        if (matcher.find()) {
            // Remove excess whitespace
            timeframe = sentence.substring(matcher.start(), matcher.end()).trim().toLowerCase().replaceAll("\\s+", " ");
            // Split string to get the number and unit. Number: 1,2,3; unit: month,year,day, etc.
            String[] split = timeframe.split(" |-(?!\\d)");
            if (split.length > 0) {
                String number = split[0];
                switch(number) {
                    case "one":
                        number = "1";
                        break;
                    case "two":
                        number = "2";
                        break;
                    case "three":
                        number = "3";
                        break;
                    case "four":
                        number = "4";
                        break;
                    case "five":
                        number = "5";
                        break;
                    case "six":
                        number = "6";
                        break;
                    case "seven":
                        number = "7";
                        break;
                    case "eight":
                        number = "8";
                        break;
                    case "nine":
                        number = "9";
                        break;
                    case "ten":
                        number = "10";
                        break;
                    case "eleven":
                        number = "11";
                        break;
                    case "twelve":
                        number = "12";
                        break;
                }
                split[0] = number;
                timeframe = String.join(" ", split);
            }
        }
        // Remove last char if it is an s
        if (timeframe.length() > 0 && timeframe.charAt(timeframe.length()-1) == 's') {
            timeframe = timeframe.substring(0, timeframe.length() - 1);
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
