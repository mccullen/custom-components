import org.apache.ctakes.core.config.ConfigParameterConstants;
import org.apache.ctakes.core.pipeline.PipelineBuilder;
import org.apache.ctakes.core.pipeline.PiperFileReader;
import org.apache.ctakes.dictionary.lookup2.util.UmlsUserApprover;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

public class PlaygroundTest {

    @Test
    public void testPipeline() throws Exception {
        // set, load, build
        PiperFileReader piperFileReader = new PiperFileReader();
        PipelineBuilder pipelineBuilder = piperFileReader.getBuilder();
        String umlsKey = getProperty("umls.key");
        pipelineBuilder.set(UmlsUserApprover.KEY_PARAM, umlsKey);
        //pipelineBuilder.set(ConfigParameterConstants.PARAM_LOOKUP_XML, "org/apache/ctakes/dictionary/lookup/fast/icd.xml");
        //piperFileReader.loadPipelineFile("./reference/piper-files/stress-test.piper");
        //piperFileReader.loadPipelineFile("./reference/piper-files/s3-bucket-reader.piper");
        String home = System.getenv("CTAKES_HOME");
        //String path = home + "\\sectionizer.piper";
        String path = home + "\\mip-radiology.piper";
        piperFileReader.loadPipelineFile(path);
        pipelineBuilder.run();
    }

    public static final String getProperty(String key) {
        String property = "";
        try {
            InputStream input = new FileInputStream("config.properties");
            Properties props = new Properties();
            props.load(input);
            property = props.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return property;
    }

    public void testPattern() throws Exception {
        //Pattern pattern = Pattern.compile("(?<!\\*\\*\\*([\\s\\S]+))^\\s*finding(s|\\(s\\))?\\s*(?!\\*\\*\\*END)");
    }
}
