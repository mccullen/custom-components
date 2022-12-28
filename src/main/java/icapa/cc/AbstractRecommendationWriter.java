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

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
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
        defaultValue = "true",
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

    public void setAppend(boolean val) {
        _append = val;
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
        if (!_append) {
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
                recommendation.setRecommendationGroup(getRecommendationGroup(jCas, s.getBegin(), s.getEnd()));

                writeRecommendationLine(recommendation);
            }
        });
        ++_nDocumentsProcessed;
    }

    private static final String[] CUIS = "C0220908,C0813145,C0421343,C0199232,C1533688,C3665373,C1305399,C1285359,C0204881,C0576743,C0199230,C1533631,C0419586,C1313868,C1313941,C0027617,C0203028,C0017463,C0373483,C0038577,C0740178,C0814438,C0702254,C0419587,C1710031,C0042792,C0421340,C1882982,C0740218,C3693691,C0420004,C0028792,C0474238,C0740216,C0740221,C0420037,C0430039,C2919584,C1327695,C1327647,C0740215,C0740222,C0740214,C0740211,C0700434,C0419997,C0420053,C1277720,C0420048,C0420061,C0474237,C2711249,C1278302,C0200464,C0262923,C0281477,C0201811,C0200288,C0451250,C0420002,C0420010,C0430934,C0700230,C0586142,C1313920,C0420001,C0420008,C0459959,C0700229,C0430047,C0430053,C0430055,C0419994,C0420014,C0420039,C0420059,C0422389,C0474236,C0420000,C0420005,C0420013,C0430936,C0260888,C0420006,C0420060,C0422390,C1442795,C0430049,C0430935,C0474239,C0557948,C1134625,C0199231,C0420020,C1272248,C1444548,C3693614,C2229277,C0430436,C1278208,C0420028,C0740223,C0184646,C0365737,C0199233,C2919896,C0200848,C0451297,C0948769,C0557949,C0740210,C0740213,C0740219,C0740212,C0420012,C0557946,C0202514,C0430091,C0420032,C4274755,C0730227,C0420042,C1278213,C0260597,C0500456,C1998852,C1273309,C1273415,C4274457,C0459958,C0523178,C0202400,C0202399,C0557997,C0557947,C0586099,C0588354,C0586059,C0586060,C0587938,C0586100,C0868699,C3263684,C0420003,C0420033,C0420050,C0430077,C0430437,C0420009,C0420045,C0420054,C0420024,C0576745,C0581010,C0586045,C0420052,C0420055,C0420080,C0430082,C0430229,C0420011,C0420038,C0420058,C0557950,C0430051,C0474241,C1273372,C1271843,C1271845,C1273373,C1303179,C1272183,C1277181,C1277718,C1277819,C1444492,C1277719,C1278201,C1278300,C1278301,C1443298,C1273434,C3839551,C1446113,C3697084,C3532355,C1303255,C1303279,C1278219,C4076638,C0474748,C0202310,C4280984,C5230435,C3686465,C4482177,C2210395,C2236946,C4702081,C2958735,C2924406,C0420017,C0740220,C1261350,C1277824,C0588158,C0420195,C1278207,C1278205,C1278206,C0451130,C0278382,C1277820,C4049836,C1278203,C1444370,C4749289,C1444368,C1444369,C3165395,C4032438,C1269729,C1409605,C0700441,C3494640,C0199488,C0202309,C0281182,C0430421,C0576744,C0419681,C0419682,C0422395,C0576747,C1456823,C0422393,C0422394,C0419999,C0430232,C0430240,C0586056,C0586057,C0586138,C0587940,C0420040,C0420063,C0420073,C0420074,C0430231,C0419679,C0420041,C0420066,C0420075,C0420077,C0420034,C0420046,C0420064,C0420065,C0420076,C0420079,C0422493,C0422533,C0422491,C0422535,C0422539,C0474755,C0419577,C0419588,C0420036,C0420067,C0420069,C0420070,C0420078,C0430090,C0457218,C0420030,C0420062,C0420068,C0420072,C0422487,C0422488,C0422489,C0422492,C0422537,C0422538,C1278202,C0422534,C0430422,C4040712,C1274121,C4274734,C1272312,C3838792,C4305526,C4758387,C2711835,C3875051,C4546206,C4545306,C4546205,C1271666,C1271973,C1273454,C1273455,C1277821,C1278215,C1278217,C1271649,C1271971,C1271974,C1273435,C1294203,C1303095,C1303257,C1271972,C1278216,C1319637,C1271888,C1271976,C1273458,C1278214,C1278218,C1303026,C1303125,C1271778,C1271975,C1319363,C1445958,C1446186,C5191793,C3532356,C1303161,C3698466,C1277724,C1277823,C1998440,C3686679,C2367051,C1504213,C4536888,C0200578,C4316075,C2585425,C2586285,C3862871,C3862870,C3860196,C0202029,C0202513,C0730228,C0576746,C0584070,C0584071,C0422391,C0455319,C0578464,C0584612,C0584613,C0419579,C0455309,C0420047,C0422434,C0422501,C0422505,C0422701,C0419578,C0419580,C0419581,C0422432,C0422704,C0422433,C0422502,C0422702,C0422703,C0422500,C0422504,C0422506,C0422705,C0430667,C2585873,C2584436,C2585729,C2585537,C3697869,C1629841,C4274879,C1997519,C2317271,C1273420,C3839266,C4302846,C4040553,C4544989,C1562969,C1446152,C1319743,C4275155,C1273439,C1319742,C1303277,C1273433,C1319556,C1319643,C5191616,C4749290,C1319551,C1319744,C1277721,C4510387,C4273606,C4275185,C2910600,C0554836,C0554837,C0740224,C5229375,C1278212,C2367053,C2367052,C2367054,C3862434,C3863726,C5190116,C4481096,C3862876,C2586319,C1271977,C3651063,C0427507,C2959343,C0584072,C1277825,C0490032,C2585792,C2584743,C3839256,C4546203,C1302091,C1272264,C4305525,C1273506,C1303247,C4274772,C4274777,C1319571,C1960906,C1273459,C1446022,C4274728,C1446148,C1445879,C2732812,C4551321,C4720851,C4720889,C5223365,C5225568,C3862878,C3863725,C2367049,C2237047,C3863723,C3863724,C3862877,C3862872,C0202511,C0420023,C0420021,C0420022,C0419583,C2585725,C4040303,C3839207,C4546324,C4546204,C4076659,C1319575,C4720937,C3862879,C2318660,C4064399,C4537737,C2960691,C0523224,C0523226,C0523223,C2732240,C4305313,C4304929,C4304817,C4075002,C4275234,C0200842,C0200844,C0200573,C0200572,C4294756,C2208511,C5223368,C4064400,C3862874,C2164118,C3862875,C3862873,C2711379,C1997967,C1446144,C2960330,C4520515,C5223366,C4294753,C4296011,C2164125,C2121271,C0523163,C0374057,C3662488,C2585790,C4305364,C4316333,C0523225,C0200501,C2164119,C2164123,C2164124,C4065282,C0200571,C0200575,C2732397,C4274765,C5223367,C4294894,C4294893,C2164120,C2125433,C2732962,C2732730,C2164121,C2164122,C2208512,C2044996,C2125434,C0200567,C2733039,C2125435,C2044997,C4523069,C3702246,C3869445,C0514807,C4711665".split(",");
    private String getRecommendationGroup(JCas jCas, int beginIndex, int endIndex) {
        String group = "Other";
        List<Ontology> ontologies = Util.getOntologies(jCas);
        String sentence = jCas.getDocumentText().substring(beginIndex, endIndex);
        for (Ontology o : ontologies) {
            if (o.getBegin() >= beginIndex && o.getEnd() <= endIndex) {
                // Ontology span is w/i bounds of sentence
                if (Arrays.stream(CUIS).anyMatch(c -> c.equals(o.getCui()))) {
                    group = "Screening";
                }
            }
        }
        return group;
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
                    o.getCodingScheme() != null &&
                    (o.getCodingScheme().equals("ICD10PCS") || o.getCodingScheme().equals("CPT") || o.getTui().equals("T061") || o.getTui().equals("T060"))) {
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
