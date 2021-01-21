package icapa.models;

import java.io.Serializable;

public class ConfigurationSettings implements Serializable {
    public static final String UMLS_KEY_PROP = "umls.key";
    public static final String INPUT_FILE_PROP = "input.file";
    public static final String NOTE_COLUMN_NAME_PROP = "note.column.name";
    public static final String PIPER_FILE_PROP = "piper.file";
    public static final String LOOKUP_XML_PROP = "lookup.xml";

    private String umlsKey;
    private String inputFile;
    private String noteColumnName;
    private String piperFile;
    private String lookupXml;

    public String getUmlsKey() {
        return umlsKey;
    }

    public void setUmlsKey(String umlsKey) {
        this.umlsKey = umlsKey;
    }

    public String getLookupXml() {
        return lookupXml;
    }

    public void setLookupXml(String lookupXml) {
        this.lookupXml = lookupXml;
    }

    public String getPiperFile() {
        return piperFile;
    }

    public void setPiperFile(String piperFile) {
        this.piperFile = piperFile;
    }

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public String getNoteColumnName() {
        return noteColumnName;
    }

    public void setNoteColumnName(String noteColumnName) {
        this.noteColumnName = noteColumnName;
    }

}
