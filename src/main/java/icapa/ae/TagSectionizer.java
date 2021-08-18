package icapa.ae;

import javafx.geometry.Pos;
import javafx.util.Pair;
import org.apache.ctakes.typesystem.type.textsem.DiseaseDisorderMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagSectionizer extends JCasAnnotator_ImplBase {
    public static final String PARAM_START_REGEX = "StartRegex";
    @ConfigurationParameter(
        name = PARAM_START_REGEX,
        description = "Regular expression to use"
    )
    private String _startRegex;

    public static final String PARAM_END_REGEX = "EndRegex";
    @ConfigurationParameter(
        name = PARAM_END_REGEX,
        description = "Regular expression to use"
    )
    private String _endRegex;

    public static final String PARAM_HEADER = "Header";
    @ConfigurationParameter(
        name = PARAM_HEADER,
        description = "Header"
    )
    private String _header;

    private Pattern _startPattern;
    private Pattern _endPattern;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        _startPattern = Pattern.compile(_startRegex);
        _endPattern = Pattern.compile(_endRegex);
    }

    private class Position {
        public int _start;
        public int _end;

        public Position(int start, int end) {
            _start = start;
            _end = end;
        }

        @Override
        public boolean equals(Object obj) {
            boolean equal = false;
            if (obj.getClass() == getClass()) {
                Position rhs = (Position)obj;
                equal = rhs._start == _start && rhs._end == _end;
            }
            return equal;
        }
    }

    @Override
    public void process(JCas jCas) {
        String documentText = jCas.getDocumentText();
        Matcher startMatcher = _startPattern.matcher(documentText);
        Matcher endMatcher = _endPattern.matcher(documentText);

        Comparator<Position> comparator = new Comparator<Position>() {
            @Override
            public int compare(Position o1, Position o2) {
                int result = 0;
                if (o1.equals(o2)) {
                    result = 0;
                } else if (o1._start < o2._start) {
                    // Order based on start if start and end are different
                    result = -1;
                } else {
                    result = 1;
                }
                return result;
            }
        };

        List<Position> positions = new ArrayList<>();
        while (startMatcher.find()) {
            if (endMatcher.find(startMatcher.start())) {
                positions.add(new Position(startMatcher.end(), endMatcher.start()));
                Segment segment = new Segment(jCas);
                segment.setId(_header);
                segment.setPreferredText(_header);
                segment.addToIndexes();
            }
        }

        if (positions.size() > 0) {
            Collection<IdentifiedAnnotation> as = JCasUtil.select(jCas, IdentifiedAnnotation.class);
            JCasUtil.select(jCas, IdentifiedAnnotation.class).forEach(ia -> {
                Position position = new Position(ia.getBegin(), ia.getEnd());
                int index = Collections.binarySearch(positions, position, comparator); // -insert - 1
                if (index < 0) { // 0 -> -1  3 -> -4
                    index = Math.abs(index) - 2;
                }
                if (index < positions.size() && index >= 0) {
                    Position tag = positions.get(index);
                    if (ia.getBegin() >= tag._start && ia.getEnd() <= tag._end) {
                        ia.setSegmentID(_header);
                        ia.setSubject("TEST");
                    }
                }
            });
            JCasUtil.select(jCas, IdentifiedAnnotation.class).forEach(ia -> {
                System.out.println(ia.getSegmentID());
            });
        }
    }
}
