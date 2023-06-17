package icapa.assertion.medfacts.cleartk;

import org.apache.ctakes.assertion.medfacts.cleartk.GenericCleartkAnalysisEngine;
import org.apache.ctakes.core.pipeline.PipeBitInfo;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.jar.GenericJarClassifierFactory;

import java.util.Optional;

@PipeBitInfo(
        name = "Generic Status Discussion Context ClearTK Annotator",
        description = "Annotates the Generic status for Identified Annotations, with preference for generic=True when there is a discussion context.",
        dependencies = { PipeBitInfo.TypeProduct.SENTENCE, PipeBitInfo.TypeProduct.IDENTIFIED_ANNOTATION }
)
public class GenericDiscussionContextCleartkAnalysisEngine extends GenericCleartkAnalysisEngine {

    @Override
    public void setClassLabel(IdentifiedAnnotation entityOrEventMention,
                              Instance<String> instance) throws AnalysisEngineProcessException {
        //System.out.println("[GenericDiscussionContextCleartkAnalysisEngine] setClassLabel()");
        //System.out.println("[GenericDiscussionContextCleartkAnalysisEngine] entityOrEventMention: \"" + entityOrEventMention.getCoveredText() + "\"");
        if (this.isTraining())
        {
            boolean generic = entityOrEventMention.getGeneric();

            // downsampling. initialize probabilityOfKeepingADefaultExample to 1.0 for no downsampling
            if (!generic
                    && coin.nextDouble() >= this.probabilityOfKeepingADefaultExample) {
                return;
            }
            instance.setOutcome(""+generic);
        } else
        {
            //System.out.println("[GenericDiscussionContextCleartkAnalysisEngine] instance.getFeatures()" + instance.getFeatures());
            String label = this.classifier.classify(instance.getFeatures());

            //System.out.println("[GenericDiscussionContextCleartkAnalysisEngine] setGeneric=" + label);
            entityOrEventMention.setGeneric(Boolean.parseBoolean(label));
        }
    }

    public static AnalysisEngineDescription createAnnotatorDescription(String modelPath) throws ResourceInitializationException {
        return AnalysisEngineFactory.createEngineDescription(GenericDiscussionContextCleartkAnalysisEngine.class,
                GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
                modelPath);
    }

    public static AnalysisEngineDescription createAnnotatorDescription() throws ResourceInitializationException {
        return createAnnotatorDescription("/org/apache/ctakes/assertion/models/generic/model.jar");
    }
}
