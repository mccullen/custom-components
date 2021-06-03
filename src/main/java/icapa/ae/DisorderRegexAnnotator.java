package icapa.ae;

import org.apache.ctakes.core.util.regex.RegexSpanFinder;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.DiseaseDisorderMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.log4j.Logger;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

public class DisorderRegexAnnotator extends JCasAnnotator_ImplBase {
    public static final Logger LOGGER = Logger.getLogger(DisorderRegexAnnotator.class.getName());
    public static final int NE_DISCOVERY_TECH_EXPLICIT_AE = 1;

    public static final String PARAM_REGEX = "Regex";
    @ConfigurationParameter(
        name = PARAM_REGEX,
        description = "Regular expression to use"
    )
    private String _regex;

    public static final String PARAM_CUI = "Cui";
    @ConfigurationParameter(
        name = PARAM_CUI,
        mandatory = false
    )
    private String _cui;

    public static final String PARAM_CODING_SCHEME = "CodingScheme";
    @ConfigurationParameter(
        name = PARAM_CODING_SCHEME,
        mandatory = false
    )
    private String _codingScheme;

    public static final String PARAM_CODE = "Code";
    @ConfigurationParameter(
        name = PARAM_CODE,
        mandatory = false
    )
    private String _code;

    public static final String PARAM_PREFERRED_TEXT = "PreferredText";
    @ConfigurationParameter(
        name = PARAM_PREFERRED_TEXT,
        mandatory = false
    )
    private String _preferredText;

    @Override
    public void process(JCas jCas) {
        try (RegexSpanFinder finder = new RegexSpanFinder(_regex)) {
            finder.findSpans(jCas.getDocumentText()).forEach(span -> {
                IdentifiedAnnotation identifiedAnnotation = new DiseaseDisorderMention(jCas);
                identifiedAnnotation.setTypeID(CONST.NE_TYPE_ID_DISORDER);
                identifiedAnnotation.setBegin(span.getValue1());
                identifiedAnnotation.setEnd(span.getValue2());
                identifiedAnnotation.setDiscoveryTechnique(NE_DISCOVERY_TECH_EXPLICIT_AE);

                UmlsConcept umlsConcept = new UmlsConcept(jCas);
                umlsConcept.setCodingScheme(_codingScheme);
                umlsConcept.setCode(_code);
                umlsConcept.setPreferredText(_preferredText);
                umlsConcept.setCui(_cui);
                FSArray conceptArr = new FSArray(jCas, 1);
                conceptArr.set(0, umlsConcept);
                identifiedAnnotation.setOntologyConceptArr(conceptArr);

                identifiedAnnotation.addToIndexes();
            });
        } catch (Exception ex) {
            LOGGER.error("Error finding identified annotation for " + _cui);
        }
    }
}
