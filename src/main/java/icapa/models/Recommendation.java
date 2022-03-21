package icapa.models;

public class Recommendation {

    private String documentId;
    private Integer beginIndex;
    private Integer endIndex;
    private Integer sentenceNumber;
    private Integer sentenceAddress;
    private String match;
    private String segment;
    private String strength;
    private String timeframe;
    private String recommendationType;
    private String sentence;
    private Integer polarity;

    public Integer getPolarity() {
        return polarity;
    }

    public void setPolarity(Integer polarity) {
        this.polarity = polarity;
    }

    public Integer getBeginIndex() {
        return beginIndex;
    }

    public void setBeginIndex(Integer beginIndex) {
        this.beginIndex = beginIndex;
    }

    public Integer getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(Integer endIndex) {
        this.endIndex = endIndex;
    }

    public Integer getSentenceNumber() {
        return sentenceNumber;
    }

    public void setSentenceNumber(Integer sentenceNumber) {
        this.sentenceNumber = sentenceNumber;
    }

    public Integer getSentenceAddress() {
        return sentenceAddress;
    }

    public void setSentenceAddress(Integer sentenceAddress) {
        this.sentenceAddress = sentenceAddress;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public String getRecommendationType() {
        return recommendationType;
    }

    public void setRecommendationType(String recommendationType) {
        this.recommendationType = recommendationType;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }
}
