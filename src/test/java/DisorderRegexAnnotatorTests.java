import icapa.ae.DisorderRegexAnnotator;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class DisorderRegexAnnotatorTests {
    @Test
    public void addsAnnotations() throws Exception {
        JCas jCas = JCasFactory.createJCas();
        String text = "The patient has TBI.";
        jCas.setDocumentText(text);
        String expectedCode = "S06.2X9D";
        String expectedCodingScheme = "ICD10CM";
        String expectedCui = "C12345";
        AnalysisEngine ae = AnalysisEngineFactory.createEngine(
            DisorderRegexAnnotator.class,
            DisorderRegexAnnotator.PARAM_CODE, expectedCode,
            DisorderRegexAnnotator.PARAM_CODING_SCHEME, expectedCodingScheme,
            DisorderRegexAnnotator.PARAM_CUI, expectedCui,
            DisorderRegexAnnotator.PARAM_REGEX, "TBI");

        SimplePipeline.runPipeline(jCas, ae);

        Collection<IdentifiedAnnotation> identifiedAnnotations = JCasUtil.select(jCas, IdentifiedAnnotation.class);
        IdentifiedAnnotation ia = identifiedAnnotations.iterator().next();
        FSArray ontologyConceptArr = ia.getOntologyConceptArr();
        FeatureStructure[] ontologyConceptFeatureStructures = ontologyConceptArr.toArray();
        FeatureStructure fs = ontologyConceptFeatureStructures[0];
        UmlsConcept umlsConcept = (UmlsConcept)fs;
        assertEquals(expectedCode, umlsConcept.getCode());
        assertEquals(expectedCodingScheme, umlsConcept.getCodingScheme());
        assertEquals(expectedCui, umlsConcept.getCui());
        /*
        identifiedAnnotations.forEach(ia -> {
            System.out.println(ia);
            Arrays.stream(ontologyConceptFeatureStructures).forEach(fs -> {
                UmlsConcept umlsConcept = (UmlsConcept)fs;
            });
        });
         */
    }
}
