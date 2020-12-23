package icapa;

import org.apache.ctakes.core.cc.XMISerializer;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class Util {
    public static String getXmi(CAS cas) {
        String xmiString = "";
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XmiCasSerializer casSerializer = new XmiCasSerializer(cas.getTypeSystem());
            XMISerializer xmiSerializer = new XMISerializer(outputStream);
            casSerializer.serialize(cas, xmiSerializer.getContentHandler());
            xmiString = new String(outputStream.toByteArray(), Charset.defaultCharset());
        } catch (IOException | SAXException e) {
            e.printStackTrace();
        }
        return xmiString;
    }

    public static <K> Map<K, Integer> getKeyToIndex(K[] keys) {
        Map<K, Integer> result = new HashMap<>();
        for (int i = 0; i < keys.length; ++i) {
            result.put(keys[i], i);
        }
        return result;
    }

    public static String[] getOntologyConceptHeaders() {
        return new String[]{
            Const.ADDRESS_HEADER,
            Const.CODE_HEADER,
            Const.CONDITIONAL_HEADER,
            Const.CONFIDENCE_HEADER,
            Const.CUI_HEADER,
            Const.GENERIC_HEADER,
            Const.POLARITY_HEADER,
            Const.PART_OF_SPEECH_HEADER,
            Const.PREFERRED_TEXT_HEADER,
            Const.REFSEM_HEADER,
            Const.SCHEME_HEADER,
            Const.SCORE_HEADER,
            Const.SUBJECT_HEADER,
            Const.TEXTSEM_HEADER,
            Const.TRUE_TEXT_HEADER,
            Const.TUI_HEADER,
            Const.UNCERTAINTY_HEADER,
            Const.DOCUMENT_ID,
            Const.END_HEADER,
            Const.BEGIN_HEADER
        };
    }

    public static Map<String, Integer> getOntologyHeaderToIndex() {
        String[] headers = Util.getOntologyConceptHeaders();
        return getKeyToIndex(headers);
    }
}
