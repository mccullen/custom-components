package icapa;

import com.lexicalscope.jewel.cli.CliFactory;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import icapa.cr.DelimiterReader;
import icapa.models.ConfigurationSettings;
import org.apache.ctakes.core.pipeline.CliOptionals;
import org.apache.ctakes.core.pipeline.PipelineBuilder;
import org.apache.ctakes.core.pipeline.PiperFileReader;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// NOTE: Runner must implement Serializable because each worker uses its private member _config
public class Runner implements Serializable {
    static private final Logger LOGGER = Logger.getLogger(Runner.class.getName());
    public static final String CONFIG_FILENAME = "config.properties";
    public static int n = 0;
    private icapa.models.ConfigurationSettings _config = new icapa.models.ConfigurationSettings();

    public void start() {
        setConfig();
        runBuilder();
        //temp();
    }

    private void temp() {
        try {
            PiperFileReader piperReader = new PiperFileReader();
            PipelineBuilder builder = piperReader.getBuilder();
            String[] args = { "--user", _config.getUmlsUsername(), "--pass", _config.getUmlsPassword(), "-p", _config.getPiperFile()};
            CliOptionals options = CliFactory.parseArguments(CliOptionals.class, args);
            piperReader.setCliOptionals(options);
            piperReader.loadPipelineFile(_config.getPiperFile());
            builder.run();
        } catch (Exception e) {
            System.out.println("ERROR");
        }
    }

    private void setConfig() {
        try (InputStream input = new FileInputStream(CONFIG_FILENAME)) {
            setConfigurationSettings(input);
        } catch (FileNotFoundException e) {
            loadDefaultConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDefaultConfig() {
        try (InputStream input = Runner.class.getClassLoader().getResourceAsStream(CONFIG_FILENAME)) {
            setConfigurationSettings(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void setConfigurationSettings(InputStream input) {
        Properties prop = new Properties();
        try {
            prop.load(input);
            _config.setInputFile(prop.getProperty(icapa.models.ConfigurationSettings.INPUT_FILE_PROP));
            _config.setNoteColumnName(prop.getProperty(icapa.models.ConfigurationSettings.NOTE_COLUMN_NAME_PROP));
            _config.setUmlsUsername(prop.getProperty(icapa.models.ConfigurationSettings.UMLS_USERNAME_PROP));
            _config.setUmlsPassword(prop.getProperty(icapa.models.ConfigurationSettings.UMLS_PASSWORD_PROP));
            _config.setPiperFile(prop.getProperty(ConfigurationSettings.PIPER_FILE_PROP));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runBuilder() {
        SparkConf conf = new SparkConf().setAppName("app").setMaster("local[1]");
        SparkSession ss = SparkSession.builder().config(conf).getOrCreate();
        JavaSparkContext sc = JavaSparkContext.fromSparkContext(ss.sparkContext());

        // TODO: Replace with this so you can run using dynamic properties
        //SparkSession ss = SparkSession.builder().getOrCreate();
        //JavaSparkContext sc = JavaSparkContext.fromSparkContext(ss.sparkContext());

        int nLines = getNLines();
        LOGGER.info(nLines);
        List<Integer> rowNumbers = IntStream.range(1, nLines).boxed().collect(Collectors.toList());
        JavaRDD<Integer> rdds = sc.parallelize(rowNumbers);
        long startTime = System.nanoTime();
        rdds.foreachPartition(p -> {
            ++n;
            System.out.println("N:::::::::::::" + n);
            int rowStart = 0;
            int rowEnd = 0;
            int i = 0;
            while (p.hasNext()) {
                int current = p.next();
                if (i == 0) {
                    rowStart = current;
                }
                if (!p.hasNext()) {
                    rowEnd = current;
                }
                ++i;
            }
            System.out.println("here");

            PiperFileReader piperReader = new PiperFileReader();
            PipelineBuilder builder = piperReader.getBuilder();
            System.out.println("ROWS FROM " + rowStart + " TO " + rowEnd);
            String readerLine = "reader " + DelimiterReader.class.getName() + " " +
                getParamString(DelimiterReader.PARAM_INPUT_FILE, _config.getInputFile()) + " " +
                getParamString(DelimiterReader.PARAM_ROW_START, String.valueOf(rowStart)) + " " +
                getParamString(DelimiterReader.PARAM_ROW_END, String.valueOf(rowEnd)) + " " +
                getParamString(DelimiterReader.PARAM_NOTE_COL_NAME, _config.getNoteColumnName());
            piperReader.parsePipelineLine(readerLine);
            String[] args = { "--user", _config.getUmlsUsername(), "--pass", _config.getUmlsPassword(), "-p", _config.getPiperFile()};
            CliOptionals options = CliFactory.parseArguments(CliOptionals.class, args);
            piperReader.setCliOptionals(options);
            piperReader.loadPipelineFile(_config.getPiperFile());
            piperReader.parsePipelineLine("add icapa.cc.OntologyWriter OutputFile=C:/root/tmp/mimiciii/ctakes-out/" + String.valueOf(rowStart) + ".csv");
            // TODO: Uncomment if you want to try to write everything to same file. I have not had success with this yet
            //piperReader.parsePipelineLine("add icapa.cc.OntologyWriter OutputFile=C:/root/tmp/mimiciii/ctakes-out/test.csv");
            builder.run();
            System.out.println("****************** DONE *********************************");
        });
        rdds.collect();
        sc.close();
        ss.stop();
        long endTime = System.nanoTime();
        long durationInMilliseconds = (endTime - startTime)/1000000;
        System.out.println(durationInMilliseconds);
    }

    private String getParamString(String name, String value) {
        return name + "=" + value;
    }

    private int getNLines() {
        int nLines = 0;
        try {
            // TODO: Maybe a better way to do this? Not sure if this is efficient
            nLines = new CSVReaderBuilder(new FileReader(_config.getInputFile())).build().readAll().size();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvException e) {
            e.printStackTrace();
        }
        return nLines;
        /*
        int nLines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(_config.getInputFile()))) {
            while (reader.readLine() != null) {
                ++nLines;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nLines;
         */
    }
}
