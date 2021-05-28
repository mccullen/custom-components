package icapa.ae;

import org.apache.ctakes.core.util.annotation.IdentifiedAnnotationBuilder;
import org.apache.ctakes.core.util.annotation.SemanticGroup;
import org.apache.ctakes.core.util.regex.RegexSpanFinder;
import org.apache.ctakes.typesystem.type.refsem.OntologyConcept;
import org.apache.ctakes.typesystem.type.textsem.DiseaseDisorderMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import java.util.function.Function;

public class TBIAnnotator extends JCasAnnotator_ImplBase {

    public static final String PARAM_REGEX = "Regex";
    @ConfigurationParameter(
        name = PARAM_REGEX,
        description = "Regular expression to use"
    )
    private String _regex;

    public static final String PARAM_CUI = "Cui";
    @ConfigurationParameter(
        name = PARAM_CUI
    )
    private String _cui;

    public static final String PARAM_CODING_SCHEME = "CodingScheme";
    @ConfigurationParameter(
        name = PARAM_CODING_SCHEME
    )
    private String _codingScheme;

    public static final String PARAM_CODE = "Code";
    @ConfigurationParameter(
        name = PARAM_CODE
    )
    private String _code;

    @Override
    public void process(JCas jCas) {
        try (RegexSpanFinder finder = new RegexSpanFinder("TBI")) {
            finder.findSpans(jCas.getDocumentText()).forEach(span -> {

            });
            Function<JCas, ? extends IdentifiedAnnotation> f = DiseaseDisorderMention::new;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
