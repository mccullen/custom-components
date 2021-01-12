package icapa;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.ctakes.core.cc.XMISerializer;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
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
            Const.PARTS_OF_SPEECH_HEADER,
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

    public static String getFeatureString(FeatureStructure fs, String featureName) {
        Feature feature = fs.getType().getFeatureByBaseName(featureName);
        String featureString = fs.getStringValue(feature);
        return featureString;
    }

    public static double getFeatureDouble(FeatureStructure fs, String featureName) {
        Feature feature = fs.getType().getFeatureByBaseName(featureName);
        double featureString = fs.getDoubleValue(feature);
        return featureString;
    }

    public static int getFeatureInt(FeatureStructure fs, String featureName) {
        Feature feature = fs.getType().getFeatureByBaseName(featureName);
        int featureString = fs.getIntValue(feature);
        return featureString;
    }

    public static boolean getFeatureBoolean(FeatureStructure fs, String featureName) {
        Feature feature = fs.getType().getFeatureByBaseName(featureName);
        boolean featureString = fs.getBooleanValue(feature);
        return featureString;
    }

    public static long getRuntimeInMiliseconds(Runnable runnable) {
        long startTime = System.nanoTime();
        runnable.run();
        long endTime = System.nanoTime();
        long durationInMilliseconds = (endTime - startTime)/1000000;
        return durationInMilliseconds;
    }

    public static AmazonS3 getS3Client() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", "us-east-1"))
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("test", "test")));
        builder.setPathStyleAccessEnabled(true);
        AmazonS3 s3Client = builder.build();
        return s3Client;
    }
}
