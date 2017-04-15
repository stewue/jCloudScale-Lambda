package ch.uzh.ifi.seal.jcs_lambda.cloudprovider.queue;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.queue.dto.InvokeType;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.queue.dto.QueueItem;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.queue.dto.QueueType;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class JcsMessageQueue extends MessageQueue {
    private static Gson gson = new Gson();

    private Map<String, Object> registeredObjects = new HashMap<>();

    private int pendingCloudCalculation = 0;
    private boolean asyncRecieverAlreadyRunning = false;
    private Thread asyncRecievingThread;

    /**
     * login to all aws services with the credential
     */
    private JcsMessageQueue(){
        super();
    }

    /**
     * get an instance of the cloud provider (singleton)
     * @return aws cloud provider instance
     */
    public static JcsMessageQueue getInstance(){
        if( instance == null ){
            instance = new JcsMessageQueue();
        }

        return (JcsMessageQueue) instance;
    }

    /**
     *
     */
    public void increasePendingCloudCalculation(){
        pendingCloudCalculation++;

        if( !asyncRecieverAlreadyRunning ){
            startAsnycRecieving();
        }
    }

    /**
     *
     */
    public void decreasePendingCloudCalculation(){
        if( pendingCloudCalculation > 0){
            pendingCloudCalculation--;
        }

        if( pendingCloudCalculation == 0 ){
            asyncRecievingThread.interrupt();
            asyncRecieverAlreadyRunning = false;
            Logger.info( "async reciever stopped" );
        }
    }

    /**
     *
     */
    private void startAsnycRecieving(){
        asyncRecieverAlreadyRunning = true;
        Logger.info( "async reciever started" );
        asyncRecievingThread = new Thread() {
            public void run() {
                try {
                    while( pendingCloudCalculation > 0 ) {
                        ReceiveMessageRequest receiveRq = new ReceiveMessageRequest()
                            .withMaxNumberOfMessages(100)
                            .withQueueUrl( url );

                        Future<ReceiveMessageResult> asyncReceiving = bufferedSqs.receiveMessageAsync(receiveRq);
                        List<Message> messages = asyncReceiving.get().getMessages();

                        for (Message message : messages ) {
                            QueueItem queueItem = gson.fromJson( message.getBody(), QueueItem.class );

                            if( registeredObjects.containsKey( queueItem.recieverId ) ){
                                handleItemFromQueue( queueItem );

                                // remove message
                                String messageReceiptHandle = message.getReceiptHandle();
                                bufferedSqs.deleteMessage(new DeleteMessageRequest( url, messageReceiptHandle ) );
                            }
                        }
                    }
                }
                catch ( Exception e ){
                }
            }
        };

        asyncRecievingThread.start();
    }

    /**
     *
     * @param uuid
     * @param obj
     */
    public void registerObject( String uuid, Object obj ){
        registeredObjects.put( uuid, obj );
    }

    /**
     *
     * @param queueItem
     */
    private void handleItemFromQueue( QueueItem queueItem ){
        Object context = registeredObjects.get( queueItem.recieverId );

        // TODO check QueueType

        if( queueItem.invokeType == InvokeType.SET ){
            handleSet( queueItem, context );
        }
        else if( queueItem.invokeType == InvokeType.GET ){
            handleGet( queueItem, context );
        }

        System.out.println("finished");
    }

    /**
     *
     * @param queueItem
     * @param context
     */
    private void handleSet ( QueueItem queueItem, Object context ){
        String fieldName = queueItem.variable;

        try{
            // decode string-body to object with specified type
            Class clazz = Class.forName( queueItem.variableType );
            Object setObject = gson.fromJson( queueItem.body, clazz );

            // get field and set value
            Field field = context.getClass().getField( fieldName );
            field.setAccessible( true );
            field.set( context, setObject );
        }
        catch ( Exception e ){
            e.printStackTrace();
        }
    }

    /**
     *
     * @param queueItem
     * @param context
     */
    private void handleGet ( QueueItem queueItem, Object context ){
        String fieldName = queueItem.variable;

        try{
            // get field and set value
            Field field = context.getClass().getField( fieldName );
            field.setAccessible( true );
            Object returnObject = field.get( context );

            sendResponseMessage( queueItem, returnObject );
        }
        catch ( Exception e ){

        }
    }

    /**
     *
     * @param recievedQueueItem
     * @param returnObject
     */
    private void sendResponseMessage( QueueItem recievedQueueItem, Object returnObject ){
        QueueItem queueItem = new QueueItem();
        queueItem.senderId = recievedQueueItem.recieverId;
        queueItem.recieverId = recievedQueueItem.senderId;
        queueItem.queueType = QueueType.RESPONSE;
        queueItem.invokeType = recievedQueueItem.invokeType;
        queueItem.variable = recievedQueueItem.variable;
        queueItem.variableType = recievedQueueItem.variableType;
        queueItem.body = gson.toJson( returnObject );

        sendMessage( gson.toJson( queueItem ) );
    }
}
