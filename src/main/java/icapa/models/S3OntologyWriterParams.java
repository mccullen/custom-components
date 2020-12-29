package icapa.models;

import icapa.services.AnalysisEngine;

import java.io.ByteArrayOutputStream;

public class S3OntologyWriterParams {
    private AnalysisEngine analysisEngine;
    private String bucket;
    private String key;
    private ByteArrayOutputStream byteArrayOutputStream;

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return byteArrayOutputStream;
    }

    public void setByteArrayOutputStream(ByteArrayOutputStream byteArrayOutputStream) {
        this.byteArrayOutputStream = byteArrayOutputStream;
    }

    public AnalysisEngine getAnalysisEngine() {
        return analysisEngine;
    }

    public void setAnalysisEngine(AnalysisEngine analysisEngine) {
        this.analysisEngine = analysisEngine;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
