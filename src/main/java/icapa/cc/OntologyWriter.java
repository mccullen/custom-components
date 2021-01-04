package icapa.cc;

import icapa.services.AnalysisEngine;
import icapa.services.OntologyWriterService;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OntologyWriter extends AbstractOntologyWriter {
    static public final String PARAM_OUTPUT_FILE = "OutputFile";
    @ConfigurationParameter(
        name = PARAM_OUTPUT_FILE,
        description = "Output file",
        defaultValue = "*",
        mandatory = true
    )
    private String _outputFile;

    private AnalysisEngine _writer;

    public OntologyWriter() {
    }

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        setWriter();
        _writer.initialize(context);
    }

    private void setWriter() {
        try {
            File file = new File(_outputFile);
            // Uncomment to append rather than recreate file
            //FileWriter fileWriter = new FileWriter(file, true);
            FileWriter fileWriter = new FileWriter(file);
            getParams().setWriter(fileWriter);
            _writer = OntologyWriterService.from(getParams());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        _writer.process(jCas);
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            _writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
