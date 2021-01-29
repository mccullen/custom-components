package icapa.cc;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

public class BaseWriter  extends JCasAnnotator_ImplBase {
    public static final Logger LOGGER = Logger.getLogger(BaseWriter.class.getName());

    private long _startTime;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        _startTime = System.nanoTime();
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
    }

    @Override
    public void destroy() {
        super.destroy();
        long endTime = System.nanoTime();
        long durationInMilliseconds = (endTime - _startTime)/1000000;
        LOGGER.info("Execution time in miliseconds: " + durationInMilliseconds);
    }
}
