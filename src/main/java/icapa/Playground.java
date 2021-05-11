package icapa;

import icapa.ae.MyAnnotator;
import icapa.models.Ontology;
import org.apache.ctakes.clinicalpipeline.ClinicalPipelineFactory;
import org.apache.ctakes.core.pipeline.PipelineBuilder;
import org.apache.ctakes.core.pipeline.PiperFileReader;
import org.apache.ctakes.core.util.PropertyAeFactory;
import org.apache.ctakes.dictionary.lookup2.util.UmlsUserApprover;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Playground {

    public static void main(String[] args) throws Exception {
        System.setProperty(UmlsUserApprover.KEY_PARAM, "08cde565-a6b0-4a50-8035-1a3d6ceb3835");
        //test1();
        //test2();
        test3();
    }

    private static void test1() throws Exception {
        //System.setProperty(UmlsUserApprover.KEY_PARAM, "my-umls-key");
        JCas jCas = JCasFactory.createJCas();
        jCas.setDocumentText("The patient had traumatic brain injury. Reported headaches");
        AnalysisEngineDescription aed = ClinicalPipelineFactory.getDefaultPipeline();
        SimplePipeline.runPipeline(jCas, aed);
    }
    private static void test2() throws Exception {
        PipelineBuilder pipelineBuilder = new PipelineBuilder();
        pipelineBuilder.set(UmlsUserApprover.KEY_PARAM, "08cde565-a6b0-4a50-8035-1a3d6ceb3835");
        AnalysisEngineDescription aed = ClinicalPipelineFactory.getDefaultPipeline();
        pipelineBuilder.addDescription(aed);

        //pipelineBuilder.add(MyAnnotator.class);
        pipelineBuilder.run("The patient had tbi");
    }

    private static void test3() throws Exception {
        // Works
        PiperFileReader piperReader = new PiperFileReader();
        PipelineBuilder builder = piperReader.getBuilder();
        builder.set("umlsKey", "08cde565-a6b0-4a50-8035-1a3d6ceb3835");
        piperReader.loadPipelineFile("C:/root/vdt/icapa/nlp/custom-components-repos/custom-components/reference/piper-files/default.piper");
        builder.run("tesing on tbi paitent");
    }
}
