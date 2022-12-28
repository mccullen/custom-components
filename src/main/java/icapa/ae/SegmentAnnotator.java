package icapa.ae;

import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textspan.Segment;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

public class SegmentAnnotator extends JCasAnnotator_ImplBase {
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        JCasUtil.select(jCas, Segment.class).forEach(segment -> {
            JCasUtil.select(jCas, Sentence.class).forEach(sentence -> {
                if (sentence.getBegin() >= segment.getBegin() && sentence.getEnd() <= segment.getEnd()) {
                    sentence.setSegmentId(segment.getId());
                }
            });
            JCasUtil.select(jCas, IdentifiedAnnotation.class).forEach(ia -> {
                if (ia.getBegin() >= segment.getBegin() && ia.getEnd() <= segment.getEnd()) {
                    ia.setSegmentID(segment.getId());
                }
            });
        });
    }
}
