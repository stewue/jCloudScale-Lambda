package ch.uzh.ifi.seal.jcs_lambda.monitoring;

import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Monitoring {
    private static Monitoring instance = null;
    private Map<MonitoringType,Long> currentResultRecord;

    // save start time of a messure
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

    public void start( MonitoringType type ){
        ongoingMeasures.put( type, System.currentTimeMillis() );
    }

    public void stop( MonitoringType type ){
        long startTimestamp = ongoingMeasures.get( type );
        ongoingMeasures.remove( type );

        long endTimestamp = System.currentTimeMillis();
        long different = endTimestamp - startTimestamp;

        currentResultRecord.put( type, different );
    }

    public long getCurrentMessure( MonitoringType type ){
        return currentResultRecord.get( type );
    }

    public void outputAll (){

        String output = "";

        // Title
        for (MonitoringType enumValue : MonitoringType.values() ) {
            output += enumValue + ";";
        }

        output += "\n";

        // Value
        for (MonitoringType enumValue : MonitoringType.values() ) {
            Long value = currentResultRecord.get( enumValue );

            if( value == null ){
                value = 0l;
            }

            output +=  value + ";";
        }

        Logger.info( output );
    }
}
