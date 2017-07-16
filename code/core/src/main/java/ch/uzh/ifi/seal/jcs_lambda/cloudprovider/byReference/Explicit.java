package ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference;

import ch.uzh.ifi.seal.jcs_lambda.annotations.ByReference;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.JVMContext;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.dto.InvokeType;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.dto.QueueItem;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.dto.QueueType;
import ch.uzh.ifi.seal.jcs_lambda.exception.RuntimeReferenceVariableException;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import ch.uzh.ifi.seal.jcs_lambda.utility.ReflectionUtil;
import com.google.gson.Gson;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Field;
import java.util.UUID;

public class Explicit {
    /**
     * handle an explicit set
     * @param context context of the explicit set
     * @param variableName variable as string
     */
    public static void set( Object context, String variableName ){
        ByReferenceHandler byReferenceHandler = ByReferenceHandler.getInstance();

        try {
            Field field = context.getClass().getDeclaredField( variableName );
            field.setAccessible( true );
            Object variableValue = field.get( context );

            Logger.info("Explicit set of " + variableName + " with value " + variableValue );
            byReferenceHandler.setVariable( context, variableName, variableValue );
        }
        catch ( Exception e ){
            e.printStackTrace();
            throw new RuntimeReferenceVariableException( "invalid variable by explicit set" );
        }
    }

    /**
     * handle an explicit get
     * @param context context of the explicit get
     * @param variableName variable as string
     */
    public static void get( Object context, String variableName ){
        ByReferenceHandler byReferenceHandler = ByReferenceHandler.getInstance();

        try {
            Field field = context.getClass().getDeclaredField( variableName );
            field.setAccessible( true );

            Logger.info("Explicit get of " + variableName );
            byReferenceHandler.getVariable( context, variableName );
        }
        catch ( Exception e ){
            e.printStackTrace();
            throw new RuntimeReferenceVariableException( "invalid variable by explicit get" );
        }
    }
}
