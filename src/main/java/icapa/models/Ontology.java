package icapa.models;

import icapa.Const;

public class Ontology {
    private int address;
    private String code;
    private boolean conditional;
    private float confidence;
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

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
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

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
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
