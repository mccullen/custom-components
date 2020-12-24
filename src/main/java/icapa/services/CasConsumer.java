package icapa.services;

import org.apache.uima.jcas.JCas;

import java.io.Closeable;

public interface CasConsumer extends Closeable {
    void process(JCas jCas);
}
