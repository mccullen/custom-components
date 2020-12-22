package icapa;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import org.apache.ctakes.typesystem.type.refsem.OntologyConcept;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class OntologyCsvWriter extends JCasAnnotator_ImplBase {
    static public final String PARAM_OUTPUT_FILE = "OutputFile";
    @ConfigurationParameter(
        name = PARAM_OUTPUT_FILE,
        description = "Output file",
        defaultValue = "*",
        mandatory = true
    )
    private String _outputFile;
    private ICSVWriter _writer;

    public OntologyCsvWriter() {
    }

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        try {
            File file = new File(_outputFile);
            FileWriter fileWriter = new FileWriter(file);
            _writer = new CSVWriter(fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        Collection<IdentifiedAnnotation> identifiedAnnotations = JCasUtil.select(jCas, IdentifiedAnnotation.class);
        for (IdentifiedAnnotation identifiedAnnotation : identifiedAnnotations) {
            String subject = identifiedAnnotation.getSubject();
            String begin = String.valueOf(identifiedAnnotation.getBegin());
            String end = String.valueOf(identifiedAnnotation.getEnd());
            FSArray ontologyConceptArr = identifiedAnnotation.getOntologyConceptArr();
            if (ontologyConceptArr != null) {
                FeatureStructure[] ontologyFeatureStructures = ontologyConceptArr.toArray();
                Arrays.stream(ontologyFeatureStructures).forEach(ontologyFeatureStructure -> {
                    String code = getFeatureString(ontologyFeatureStructure, "code");
                    String[] row = {begin, end, code};
                    _writer.writeNext(row, false);
                });
            }
        }
    }

    private String getFeatureString(FeatureStructure fs, String featureName) {
        Feature feature = fs.getType().getFeatureByBaseName(featureName);
        String featureString = fs.getStringValue(feature);
        return featureString;
    }


    @Override
    public void destroy() {
        super.destroy();
    }
}
