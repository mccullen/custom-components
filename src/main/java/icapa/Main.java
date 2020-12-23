package icapa;

import com.opencsv.exceptions.CsvValidationException;
import org.apache.log4j.Logger;

import java.io.IOException;

public class Main {
    static private final Logger LOGGER = Logger.getLogger( "Main" );
    public static void main(String[] args) throws IOException, CsvValidationException {
        Runner runner = new Runner();
        runner.start();
        System.out.println(Main.class.getName());
    }
}
