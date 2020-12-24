package icapa.services;

import org.apache.uima.jcas.JCas;

public interface CollectionReader {
    void readNext(JCas jCas);
    boolean hasNext();
    void destroy();
}
