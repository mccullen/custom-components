import icapa.ae.DisorderRegexAnnotator;
import icapa.ae.TagSectionizer;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class TagSectionizerTests {

    @Test
    public void testSectionizer() throws Exception {
        JCas jCas = JCasFactory.createJCas();
        String text = "The patient has tbi\n" +
            "\n" +
            "study:\n" +
            "The study of the bat and concussion to head.\n" +
            "\n" +
            "Finding:\n" +
            "\n" +
            "The patient passed out and took some exams and drank milk and almost died\n" +
            "\n" +
            "***CRITICAL RESULT COMMUNICATION***\n" +
            "the patient has post traumatic stress disorder\n" +
            "\n" +
            "Finding: The patient has cancer\n" +
            "the patient went for a run and got bit by a shark\n" +
            "***END CRITICAL RESULT COMMUNICATION***\n" +
            "\n" +
            "the is a test of the concussion of patient\n" +
            "\n" +
            "Finding: the patient has ptsd. post traumatic stress distorder.";
        jCas.setDocumentText(text);
        IdentifiedAnnotation a = new IdentifiedAnnotation(jCas);
        String find = "cancer";
        int start = text.indexOf(find);
        int end = start + find.length();
        a.setBegin(start);
        a.setEnd(end);
        a.addToIndexes();

        AnalysisEngine ae = AnalysisEngineFactory.createEngine(
            TagSectionizer.class,
            TagSectionizer.PARAM_HEADER, "Test",
            TagSectionizer.PARAM_START_REGEX, "XXX",
            TagSectionizer.PARAM_END_REGEX, "YYY");

        SimplePipeline.runPipeline(jCas, ae);

        Collection<IdentifiedAnnotation> identifiedAnnotations = JCasUtil.select(jCas, IdentifiedAnnotation.class);
        identifiedAnnotations.forEach(ia -> {
            String section = ia.getSegmentID();
            System.out.println(section);
        });
    }
}
