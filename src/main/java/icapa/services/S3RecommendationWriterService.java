package icapa.services;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import icapa.Util;
import icapa.models.Recommendation;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Map;

public class S3RecommendationWriterService implements RecommendationWriterService {
    private static final Logger LOGGER = Logger.getLogger(S3RecommendationWriterService.class.getName());

    private ICSVWriter _csvWriter;
    private Writer _writer;
    private char _delimiter;
    private Map<String, Integer> _headerToIndex;
    private String[] _headers;
    private boolean _applyQuotesToAll = false;
    private ByteArrayOutputStream _byteArrayOutputStream;

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return _byteArrayOutputStream;
    }

    public static S3RecommendationWriterService fromParams(char delimiter, boolean append) {
        S3RecommendationWriterService service = new S3RecommendationWriterService();
        service._delimiter = delimiter;
        service.setWriter();
        service.init();

        return service;
    }

    private void init() {
        try {
            _csvWriter = new CSVWriterBuilder(_writer).withSeparator(_delimiter).build();
            _headers = Util.getRecommendationHeaders();
            _headerToIndex = Util.getKeyToIndex(_headers);
        } catch (Exception e) {
            LOGGER.error("Error initializing file recommendation writer service", e);
        }
    }

    private void setWriter() {
        // Set regular ontology writer and byte array output stream
        _byteArrayOutputStream = new ByteArrayOutputStream(); // store this for close() method later, where you have to convert to input stream
        // S3 only allows you to upload using an input stream. So our S3OntologyWriterService _writer
        // needs a reference to the output stream so we can copy it over to an input stream later when close()
        // gets called.
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(_byteArrayOutputStream); // Wrap output stream into buffered stream for efficiency
        _writer = new OutputStreamWriter(bufferedOutputStream);
    }

    @Override
    public void writeHeaderLine() {
        _csvWriter.writeNext(_headers, _applyQuotesToAll);
    }

    @Override
    public void writeRecommendationLine(Recommendation recommendation) {
        String[] row = Util.getRecommendationAsStringArray(recommendation, _headerToIndex);
        _csvWriter.writeNext(row, _applyQuotesToAll);
        try {
            _csvWriter.flush();
        } catch (IOException e) {
            LOGGER.error("Error flushing csv writer in file recommendation writer service.", e);
        }
    }
}
