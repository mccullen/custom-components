package icapa.cc.ontology;

import icapa.cc.ontology.AbstractFileOntologyWriter;
import icapa.services.*;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LocalFileOntologyWriter extends AbstractFileOntologyWriter {
    static public final String PARAM_OUTPUT_FILE = "OutputFile";
    @ConfigurationParameter(
        name = PARAM_OUTPUT_FILE,
        description = "Output file",
        defaultValue = "*",
        mandatory = true
    )
    private String _outputFile;

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
            if (Files.exists(Paths.get(_outputFile))) {
                append = true;
            } else {
                append = false;
            }

            File file = new File(_outputFile);
            FileWriter fileWriter = new FileWriter(file, append);
            // Uncomment to recreate rather than append
            //FileWriter fileWriter = new FileWriter(file);
            OntologyConsumer ontologyConsumer = FileOntologyConsumer.from(fileWriter, getParams().getDelimiter(), append);
            _writer = JdbcOntologyWriterService.fromParams(ontologyConsumer, getParams().isKeepAll());
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
