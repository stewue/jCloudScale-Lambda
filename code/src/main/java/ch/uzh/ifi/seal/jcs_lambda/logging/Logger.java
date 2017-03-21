package ch.uzh.ifi.seal.jcs_lambda.logging;

import ch.uzh.ifi.seal.jcs_lambda.configuration.JcsConfiguration;

public class Logger {
    /**
     * print an error message in the console if log level is high enough
     * @param msg log message
     */
    public static void fatal ( String msg ){
        if(JcsConfiguration.LOG_LEVEL.getLevel() >= LogLevel.FATAL.getLevel() ){
            output( msg );
        }
    }

    /**
     * print an error message in the console if log level is high enough
     * @param msg log message
     */
    public static void error ( String msg ){
        if(JcsConfiguration.LOG_LEVEL.getLevel() >= LogLevel.ERROR.getLevel() ){
            output( msg );
        }
    }

    /**
     * print an error message in the console if log level is high enough
     * @param msg log message
     */
    public static void warn ( String msg ){
        if(JcsConfiguration.LOG_LEVEL.getLevel() >= LogLevel.WARN.getLevel() ){
            output( msg );
        }
    }

    /**
     * print an error message in the console if log level is high enough
     * @param msg log message
     */
    public static void info ( String msg ){
        if(JcsConfiguration.LOG_LEVEL.getLevel() >= LogLevel.INFO.getLevel() ){
            output( msg );
        }
    }

    /**
     * print an error message in the console if log level is high enough
     * @param msg log message
     */
    public static void debug ( String msg ){
        if(JcsConfiguration.LOG_LEVEL.getLevel() >= LogLevel.DEBUG.getLevel() ){
            output( msg );
        }
    }

    /**
     * print an error message in the console
     * @param msg log message
     */
    private static void output( String msg ){
        System.out.println( msg );
    }
}
