package ch.uzh.ifi.seal.jcs_lambda.exception;

import ch.uzh.ifi.seal.jcs_lambda.logging.LogMessage;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;

public class CloudRuntimeException extends RuntimeException
{
    public CloudRuntimeException(String msg)
    {
        super(msg);
    }

    public CloudRuntimeException ( String msg, StackTraceElement [] stackTraceElements ){
        super(msg);

        Logger.fatal( LogMessage.red( "Here the exception from the aws cloud" ) );
        Logger.fatal( LogMessage.red( "-------------------------------------" ) );
        for( StackTraceElement element : stackTraceElements ){
            Logger.fatal( LogMessage.red( element.toString() ) );
        }
    }
}
