package icapa;

import org.apache.uima.jcas.JCas;

public interface CasConsumer {
    void process(JCas jCas);
    void destroy();
}
