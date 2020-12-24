package icapa.services;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;

import java.io.Closeable;

public interface AnalysisEngine extends Closeable {
    void initialize(UimaContext context);
    void process(JCas jCas);
}
