package icapa.models;

public class Ontology {
    private int identifiedAnnotationAddress;
    private String code;
    private boolean conditional;
    private String cui;
    private boolean generic;
    private int polarity;
    private int end;
    private int begin;
    private String preferredText;
    private String refsem;
    private String codingScheme;
    private double score;
    private String subject;
    private String textsem;
    private String tui;
    private int uncertainty;
    private String trueText;
    private String documentId;
    private String partsOfSpeech;

    private String entityType;
    private String segment;
    private String discoveryTechnique;
    private int historyOf;
    private String originalText;
    private String oid;
    private String oui;
    private boolean disambiguated;
    private int ontologyConceptAddress;

    public int getOntologyConceptAddress() {
        return ontologyConceptAddress;
    }

    public void setOntologyConceptAddress(int ontologyConceptAddress) {
        this.ontologyConceptAddress = ontologyConceptAddress;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getDiscoveryTechnique() {
        return discoveryTechnique;
    }

    public void setDiscoveryTechnique(String discoveryTechnique) {
        this.discoveryTechnique = discoveryTechnique;
    }

    public int getHistoryOf() {
        return historyOf;
    }

    public void setHistoryOf(int historyOf) {
        this.historyOf = historyOf;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getOui() {
        return oui;
    }

    public void setOui(String oui) {
        this.oui = oui;
    }

    public boolean isDisambiguated() {
        return disambiguated;
    }

    public void setDisambiguated(boolean disambiguated) {
        this.disambiguated = disambiguated;
    }

    public int getIdentifiedAnnotationAddress() {
        return identifiedAnnotationAddress;
    }

    public void setIdentifiedAnnotationAddress(int identifiedAnnotationAddress) {
        this.identifiedAnnotationAddress = identifiedAnnotationAddress;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isConditional() {
        return conditional;
    }

    public void setConditional(boolean conditional) {
        this.conditional = conditional;
    }

    public String getCui() {
        return cui;
    }

    public void setCui(String cui) {
        this.cui = cui;
    }

    public boolean isGeneric() {
        return generic;
    }

    public void setGeneric(boolean generic) {
        this.generic = generic;
    }

    public int getPolarity() {
        return polarity;
    }

    public void setPolarity(int polarity) {
        this.polarity = polarity;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public String getPreferredText() {
        return preferredText;
    }

    public void setPreferredText(String preferredText) {
        this.preferredText = preferredText;
    }

    public String getRefsem() {
        return refsem;
    }

    public void setRefsem(String refsem) {
        this.refsem = refsem;
    }

    public String getCodingScheme() {
        return codingScheme;
    }

    public void setCodingScheme(String codingScheme) {
        this.codingScheme = codingScheme;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTextsem() {
        return textsem;
    }

    public void setTextsem(String textsem) {
        this.textsem = textsem;
    }

    public String getTui() {
        return tui;
    }

    public void setTui(String tui) {
        this.tui = tui;
    }

    public int getUncertainty() {
        return uncertainty;
    }

    public void setUncertainty(int uncertainty) {
        this.uncertainty = uncertainty;
    }

    public String getTrueText() {
        return trueText;
    }

    public void setTrueText(String trueText) {
        this.trueText = trueText;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getPartsOfSpeech() {
        return partsOfSpeech;
    }

    public void setPartsOfSpeech(String partsOfSpeech) {
        this.partsOfSpeech = partsOfSpeech;
    }
}
