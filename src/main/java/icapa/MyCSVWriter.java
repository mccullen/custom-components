package icapa;

import com.opencsv.CSVWriter;
import org.apache.ctakes.core.cc.XMISerializer;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.charset.Charset;

public class MyCSVWriter extends JCasAnnotator_ImplBase {
    static private final Logger LOGGER = Logger.getLogger( "CSVWriter" );

    static public final String PARAM_OUTPUT_FILE = "OutputFile";
    @ConfigurationParameter(
            name = PARAM_OUTPUT_FILE,
            description = "Output file",
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
            //FileWriter fileWriter = new FileWriter(file, true);
            FileWriter fileWriter = new FileWriter(file);
            writer = new CSVWriter(fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void process(JCas jCas) throws AnalysisEngineProcessException {
        try {
            writeXmi(jCas.getCas());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }
}