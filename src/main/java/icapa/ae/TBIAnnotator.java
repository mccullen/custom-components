package icapa.ae;

import org.apache.ctakes.typesystem.type.refsem.OntologyConcept;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;

public class TBIAnnotator extends JCasAnnotator_ImplBase {


    @Override
    public void process(JCas jCas) {
        IdentifiedAnnotation identifiedAnnotation = new IdentifiedAnnotation(jCas);

    }
}
