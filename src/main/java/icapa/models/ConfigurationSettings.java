package icapa.models;

import java.io.Serializable;

public class ConfigurationSettings implements Serializable {
    public static final String UMLS_USERNAME_PROP = "umls.username";
    public static final String UMLS_PASSWORD_PROP = "umls.password";
    public static final String INPUT_FILE_PROP = "input.file";
    public static final String NOTE_COLUMN_NAME_PROP = "note.column.name";
    public static final String PIPER_FILE_PROP = "piper.file";

    private String umlsUsername;
    private String umlsPassword;
    private String inputFile;
    private String noteColumnName;
    private String piperFile;


    public String getPiperFile() {
        return piperFile;
    }

    public void setPiperFile(String piperFile) {
        this.piperFile = piperFile;
    }
    public String getUmlsUsername() {
        return umlsUsername;
    }

    public void setUmlsUsername(String umlsUsername) {
        this.umlsUsername = umlsUsername;
    }

    public String getUmlsPassword() {
        return umlsPassword;
    }

    public void setUmlsPassword(String umlsPassword) {
        this.umlsPassword = umlsPassword;
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
