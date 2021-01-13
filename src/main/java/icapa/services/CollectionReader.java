package icapa.services;

import org.apache.uima.jcas.JCas;

public interface CollectionReader {
    void initialize();
    void readNext(JCas jCas);
    boolean hasNext();
    void destroy();
}
