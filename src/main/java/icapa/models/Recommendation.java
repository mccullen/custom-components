package icapa.models;

public class Recommendation {
    private Integer beginIndex;
    private Integer endIndex;
    private Integer sentenceNumber;
    private Integer sentenceAddress;
    private String match;
    private String segment;

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
}
