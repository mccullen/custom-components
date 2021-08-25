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

public class TagSectionAnnotator extends JCasAnnotator_ImplBase {
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

    // Utility class to mark the start/end of the section tag
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

        List<Position> tags = new ArrayList<>();
        // Add all positions to mark the start and end of each tagged section
        while (startMatcher.find()) { // find a match to the start tag
            if (endMatcher.find(startMatcher.start())) { // From the start tag match, find an end tag match
                // Add the position. The start/end indexes of the position are set to everything in between, but
                // not including, the match.
                tags.add(new Position(startMatcher.end(), endMatcher.start()));

                // Add the segment to the cas
                Segment segment = new Segment(jCas);
                segment.setId(_header);
                segment.setPreferredText(_header);
                segment.addToIndexes();
            }
        }

        if (tags.size() > 0) {
            // There are tagged sections, so set the segmentId for all identified annotations that are FULLY
            // in between (inclusive) the start and end of the position index
            JCasUtil.select(jCas, IdentifiedAnnotation.class).forEach(ia -> {
                // Get the position of the identified annotation
                Position iaPosition = new Position(ia.getBegin(), ia.getEnd());

                // Search the positions of the tagged sections and return the index of the matching section (where
                // the start/end indexes are the same. If there is no such position (likely), then return the index where
                // it would be inserted minus 1.
                // We are going to reset this index so that it indexes to the first
                // position which has a start that is on or before the start of the identified annotation
                int index = Collections.binarySearch(tags, iaPosition, comparator); // -insert - 1
                if (index < 0) { // if index would be 0, then it would return -1, if index would be  3, -4 would return
                    index = Math.abs(index) - 1; // gets the index it would be
                    // Now, you need to go to the previous position.
                    // if it would be inserted at a position before all the tags, index would be 0 right now, meaning
                    // this identified annotation is before any tag starts. So it is okay to skip over it in the
                    // conditional below. If it starts after the first tag's start position (say, index = 1), then
                    // you would need to check the the start/end of the tag before (index-1) that index to see
                    // if it falls w/i its bounds. So, we are subtracting 1 here because we need to get the index
                    // of the first possible tag which COULD contain this annotation. Recall our comparator compares
                    // the start of the position, so the tag at the index just before the annotation would be inserted
                    // would have a start < the start of the annotation.
                    index = index - 1;
                }
                if (index < tags.size() && index >= 0) {
                    Position tag = tags.get(index);
                    if (ia.getBegin() >= tag._start && ia.getEnd() <= tag._end) {
                        ia.setSegmentID(_header);
                    }
                }
            });
        }
    }
}
