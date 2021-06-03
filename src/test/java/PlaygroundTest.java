import org.apache.ctakes.core.config.ConfigParameterConstants;
import org.apache.ctakes.core.pipeline.PipelineBuilder;
import org.apache.ctakes.core.pipeline.PiperFileReader;
import org.apache.ctakes.dictionary.lookup2.util.UmlsUserApprover;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PlaygroundTest {
    @Test
    public void testPipeline() throws Exception {
        // set, load, build
        PiperFileReader piperFileReader = new PiperFileReader();
        PipelineBuilder pipelineBuilder = piperFileReader.getBuilder();
        String umlsKey = getProperty("umls.key");
        pipelineBuilder.set(UmlsUserApprover.KEY_PARAM, umlsKey);
        pipelineBuilder.set(ConfigParameterConstants.PARAM_LOOKUP_XML, "org/apache/ctakes/dictionary/lookup/fast/icd.xml");
        //piperFileReader.loadPipelineFile("./reference/piper-files/stress-test.piper");
        piperFileReader.loadPipelineFile("./reference/piper-files/disorder-ae.piper");
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

}
