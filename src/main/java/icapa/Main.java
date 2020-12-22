package icapa;

import com.lexicalscope.jewel.cli.CliFactory;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.ctakes.core.pipeline.CliOptionals;
import org.apache.ctakes.core.pipeline.PipelineBuilder;
import org.apache.ctakes.core.pipeline.PiperFileReader;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;

import java.io.*;
import java.util.Properties;

public class Main {
    static private final Logger LOGGER = Logger.getLogger( "Main" );
    public static void main(String[] args) throws IOException, CsvValidationException {
        Runner runner = new Runner();
        runner.start();
        System.out.println(Main.class.getName());
    }
}
