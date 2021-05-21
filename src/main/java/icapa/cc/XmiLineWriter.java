package icapa.cc;

import com.opencsv.CSVWriter;
import icapa.Util;
import icapa.ae.TimeAnnotator;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * For each document, write the XMI as a single line to the OutputFile
 * */
public class XmiLineWriter extends TimeAnnotator {
    static private final Logger LOGGER = Logger.getLogger(XmiLineWriter.class.getName());

    static public final String PARAM_OUTPUT_FILE = "OutputFile";
    @ConfigurationParameter(
            name = PARAM_OUTPUT_FILE,
            defaultValue = "*",
            mandatory = true
    )
    private String _outputFile;

    private CSVWriter writer;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        try {
            final File file = new File(_outputFile);
            //FileWriter fileWriter = new FileWriter(file, true);// To append
            FileWriter fileWriter = new FileWriter(file);
            writer = new CSVWriter(fileWriter);
        } catch (IOException e) {
            LOGGER.error("Error opening file " + _outputFile, e);
        }
    }

    public void process(JCas jCas) throws AnalysisEngineProcessException {
        try {
            writeXmi(jCas.getCas());
        } catch (IOException | SAXException e) {
            LOGGER.error("Error writing xmi", e);
        }
    }

    /**
     * Serialize a CAS to a file in XMI format
     *
     * @param cas  CAS to serialize
     * @throws IOException  -
     * @throws SAXException -
     */
    private void writeXmi(final CAS cas) throws IOException, SAXException {
        String xmiString = Util.getXmi(cas);
        String[] row = { xmiString };
        writer.writeNext(row);
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            // Closing will cause the writer to actually do the writing.
            writer.close();
        } catch (IOException e) {
            LOGGER.error("Error closing xmi writer", e);
        }
    }
}
