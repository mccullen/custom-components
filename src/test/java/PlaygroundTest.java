import org.apache.ctakes.core.pipeline.PipelineBuilder;
import org.apache.ctakes.core.pipeline.PiperFileReader;
import org.apache.ctakes.dictionary.lookup2.util.UmlsUserApprover;
import org.junit.Test;

public class PlaygroundTest {
    @Test
    public void test() throws Exception {
        // set, load, build
        System.out.println(System.getProperty("user.dir"));
        PiperFileReader piperFileReader = new PiperFileReader();
        PipelineBuilder pipelineBuilder = piperFileReader.getBuilder();
        pipelineBuilder.set(UmlsUserApprover.KEY_PARAM, "TODO");
        piperFileReader.loadPipelineFile("TODO");
        pipelineBuilder.run();

    }
}
