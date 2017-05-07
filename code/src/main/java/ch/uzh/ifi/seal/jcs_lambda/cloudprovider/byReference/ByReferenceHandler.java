package ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.JVMContext;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.dto.InvokeType;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.dto.QueueItem;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.dto.QueueType;
import ch.uzh.ifi.seal.jcs_lambda.configuration.AwsConfiguration;
import ch.uzh.ifi.seal.jcs_lambda.exception.RuntimeReferenceVariableException;
import ch.uzh.ifi.seal.jcs_lambda.utility.ReflectionUtil;
import com.google.gson.Gson;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Field;
import java.util.UUID;

public class ByReferenceHandler {
    private static ByReferenceHandler instance;

    private Gson gson = new Gson();

    private JcsMessageQueue messageQueue;

    private ByReferenceHandler(){
        messageQueue = JcsMessageQueue.getInstance();
        messageQueue.connect( AwsConfiguration.AWS_QUEUE_URL );
    }

    public static ByReferenceHandler getInstance(){
        if( instance == null ){
            instance = new ByReferenceHandler();
        }

        return instance;
    }

    /**
     * cloud send a get request to local client
     * @param joinPoint current point of execution
     * @return variable value from client
     */
    public Object getVariable( ProceedingJoinPoint joinPoint ){
        try {
            // Send request
            QueueItem queueItem = new QueueItem();
            queueItem.senderId = UUID.randomUUID().toString();
            queueItem.receiverId = JVMContext.getContextId();
            queueItem.queueType = QueueType.REQUEST;
            queueItem.invokeType = InvokeType.GET;
            queueItem.variable = joinPoint.getSignature().getName();

            Object context = joinPoint.getThis();
            Class clazz = context.getClass();
            Field field = clazz.getDeclaredField( queueItem.variable );
            field.setAccessible( true );

            queueItem.variableType = field.getGenericType().getTypeName();

            messageQueue.increasePendingRequests();
            messageQueue.sendMessage( gson.toJson( queueItem ) );

            // wait on response
            QueueItem responseItem = messageQueue.receiveSyncMessage( queueItem.senderId );
            messageQueue.decreasePendingRequests();

            Class clazzVariableType = ReflectionUtil.getClassFromString( responseItem.variableType );
            return gson.fromJson( responseItem.body, clazzVariableType );
        }
        catch ( Exception e ){
            e.printStackTrace();
            throw new RuntimeReferenceVariableException( "invalid casting" );
        }
    }

    /**
     * cloud send a set request to local client
     * @param joinPoint current point of execution
     */
    public void setVariable( ProceedingJoinPoint joinPoint ){
        try {
            // Send request
            QueueItem queueItem = new QueueItem();
            queueItem.senderId = UUID.randomUUID().toString();
            queueItem.receiverId = JVMContext.getContextId();
            queueItem.queueType = QueueType.REQUEST;
            queueItem.invokeType = InvokeType.SET;
            queueItem.variable = joinPoint.getSignature().getName();

            Object context = joinPoint.getThis();
            Class clazz = context.getClass();
            Field field = clazz.getDeclaredField( queueItem.variable );
            field.setAccessible( true );
            queueItem.variableType = field.getType().getName();
            queueItem.body = gson.toJson( joinPoint.getArgs()[0] );

            messageQueue.increasePendingRequests();
            messageQueue.sendMessage( gson.toJson( queueItem ) );

            // wait on response
            messageQueue.receiveSyncMessage( queueItem.senderId );
            messageQueue.decreasePendingRequests();

            // ignore response
        }
        catch ( Exception e ){
            e.printStackTrace();
            throw new RuntimeReferenceVariableException( "invalid casting" );
        }
    }
}
