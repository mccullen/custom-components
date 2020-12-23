package icapa;

import org.apache.uima.jcas.JCas;

public interface CollectionReader {
    void readNext(JCas jcas);
    boolean hasNext();
}
