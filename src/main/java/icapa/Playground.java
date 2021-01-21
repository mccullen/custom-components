package icapa;

import icapa.models.Ontology;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Playground {

    public static void main(String[] args) throws Exception {
        String regex = "^\\[(.*?)\\]";
        //String regex = "\\[.*\\]";
        Pattern pattern = Pattern.compile(regex);
        String text = "[DDAR].[NLP_TBI_NOTES]";
        Matcher matcher = pattern.matcher(text);
        String result = matcher.replaceAll("");
        System.out.println(result);
    }
}
