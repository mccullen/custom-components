package icapa.services;

import icapa.models.JdbcReaderParams;
import org.apache.uima.jcas.JCas;

public class JdbcReaderService implements CollectionReader {

    public static CollectionReader fromParams(JdbcReaderParams params) {
        CollectionReader reader = new JdbcReaderService();
        return reader;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void readNext(JCas jCas) {

    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public void destroy() {

    }
}
