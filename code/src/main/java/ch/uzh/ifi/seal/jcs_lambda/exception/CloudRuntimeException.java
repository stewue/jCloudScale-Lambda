package ch.uzh.ifi.seal.jcs_lambda.exception;

import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;

public class CloudRuntimeException extends RuntimeException
{
    public CloudRuntimeException(String msg)
    {
        super(msg);
    }

    public CloudRuntimeException ( String msg, StackTraceElement [] stackTraceElements ){
        super(msg);

        Logger.fatal( (char)27 + "[31mHere the exception from the aws cloud" );
        Logger.fatal( (char)27 + "[31m-------------------------------------" );
        for( StackTraceElement element : stackTraceElements ){
            Logger.fatal( (char)27 + "[31m" + element.toString() );
        }
    }
}
