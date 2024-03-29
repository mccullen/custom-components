package icapa.cc;

import icapa.services.AnalysisEngine;
import icapa.services.FileOntologyConsumer;
import icapa.services.OntologyConsumer;
import icapa.services.OntologyWriterService;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LocalFileOntologyWriter extends AbstractFileOntologyWriter {
    private static final Logger LOGGER = Logger.getLogger(LocalFileOntologyWriter.class.getName());

    static public final String PARAM_OUTPUT_FILE = "OutputFile";
    @ConfigurationParameter(
        name = PARAM_OUTPUT_FILE,
        defaultValue = "*",
        mandatory = true
    )
    private String _outputFile;

    static public final String PARAM_APPEND = "Append";
    @ConfigurationParameter(
        name = PARAM_APPEND,
        defaultValue = "true",
        mandatory = false
    )
    private boolean _append;

    private AnalysisEngine _writer;

    public LocalFileOntologyWriter() {
    }

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        setWriter();
        _writer.initialize(context);
    }

    private void setWriter() {
        try {
            boolean append = false;
            if (Files.exists(Paths.get(_outputFile)) && _append) {
                // Only set to true if file exists and they specified to append
                append = true;
            } else {
                append = false;
            }

            File file = new File(_outputFile);
            FileWriter fileWriter = new FileWriter(file, append);
            // Uncomment to recreate rather than append
            //FileWriter fileWriter = new FileWriter(file);
            OntologyConsumer ontologyConsumer = FileOntologyConsumer.from(fileWriter, getParams().getDelimiter(), append);
            _writer = OntologyWriterService.fromParams(ontologyConsumer, getParams().isKeepAll());
        } catch (Exception e) {
            LOGGER.error("Error opening file to write to " + _outputFile, e);
        }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        super.process(jCas);
        _writer.process(jCas);
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            _writer.close();
        } catch (IOException e) {
            LOGGER.error("Error closing writer", e);
        }
    }
}
