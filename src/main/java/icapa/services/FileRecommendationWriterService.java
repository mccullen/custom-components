package icapa.services;


import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import icapa.Util;
import icapa.cc.LocalFileOntologyWriter;
import icapa.models.Recommendation;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class FileRecommendationWriterService implements RecommendationWriterService {
    private static final Logger LOGGER = Logger.getLogger(FileRecommendationWriterService.class.getName());

    private String _outputFile;
    private ICSVWriter _csvWriter;
    private boolean _append;
    private char _delimiter;
    private Map<String, Integer> _headerToIndex;
    private String[] _headers;
    private boolean _applyQuotesToAll = false;

    public static FileRecommendationWriterService fromParams(String outputPath, char delimiter, boolean append) {
        FileRecommendationWriterService service = new FileRecommendationWriterService();

        service._outputFile = outputPath;
        service._delimiter = delimiter;
        service._append = append;
        service.init();

        return service;
    }

    private void init() {
        try {
            File file = new File(_outputFile);
            FileWriter fileWriter = new FileWriter(file, _append);

            _csvWriter = new CSVWriterBuilder(fileWriter).withSeparator(_delimiter).build();
            _headers = Util.getRecommendationHeaders();
            _headerToIndex = Util.getKeyToIndex(_headers);
        } catch (Exception e) {
            LOGGER.error("Error initializing file recommendation writer service", e);
        }
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
