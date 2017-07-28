package ch.uzh.ifi.seal.jcs_lambda.monitoring;

import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Monitoring {
    private static Monitoring instance = null;
    private Map<MonitoringType,Long> currentResultRecord;

    // save start time of a measurement
    private Map<MonitoringType,Long> ongoingMeasures;

    private Monitoring (){
        currentResultRecord = new HashMap<>();
        ongoingMeasures = new HashMap<>();
    }

    public static Monitoring getInstance(){
        if( instance == null ){
            instance = new Monitoring();
        }

        return instance;
    }

    /**
     * start the monitoring of a specific type
     * @param type monitoringType of the measurement
     */
    public void start( MonitoringType type ){
        ongoingMeasures.put( type, System.currentTimeMillis() );
    }

    /**
     * stop the monitoring of a specific type
     * @param type monitoringType of the measurement
     */
    public void stop( MonitoringType type ){
        long startTimestamp = ongoingMeasures.get( type );
        ongoingMeasures.remove( type );

        long endTimestamp = System.currentTimeMillis();
        long different = endTimestamp - startTimestamp;

        currentResultRecord.put( type, different );
    }

    public long getCurrentMeasurement( MonitoringType type ){
        return currentResultRecord.get( type );
    }

    /**
     * Output the current measurement
     */
    private String outputAsString (){
        String output = "";

        // output value
        for (MonitoringType enumValue : MonitoringType.values() ) {
            Long value = currentResultRecord.get( enumValue );

            if( value == null ){
                value = 0l;
            }

            output +=  value + ";";
        }

        return output;
    }

    /**
     * Output the current measurement as csv
     * @param filename filename where the value is saved
     */
    public void outputCSV ( String filename ){
        String absoluteJavaPath = "jcs_lambda/" + filename + ".csv";

        // create folder if not exists
        File file = new File( absoluteJavaPath );
        file.getParentFile().mkdirs();

        String content = "";
        try{
            content = Files.toString( file, Charsets.UTF_8 );
        }
        catch ( Exception e ){

        }

        try {
            PrintWriter writer = new PrintWriter(absoluteJavaPath, "UTF-8");
            writer.write( content + "\n" +outputAsString() );
            writer.close();
        }
        catch ( Exception e ){

        }
    }

    /**
     * Output the current measurement in the console
     */
    public void outputConsole (){
        String output = outputAsString();

        // output title
        for (MonitoringType enumValue : MonitoringType.values() ) {
            output += enumValue + ";";
        }
        output += "\n";

        Logger.fatal( output );
    }
}