package ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.dto.InvokeType;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.dto.QueueItem;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.dto.QueueType;
import ch.uzh.ifi.seal.jcs_lambda.configuration.AwsConfiguration;
import ch.uzh.ifi.seal.jcs_lambda.exception.RuntimeVariableReferenceException;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import com.amazonaws.services.sqs.model.*;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JcsMessageQueue extends MessageQueue {
    private static Gson gson = new Gson();

    private Map<String, Object> registeredObjects = new HashMap<>();

    private int pendingCloudCalculation = 0;
    private boolean asyncReceiverAlreadyRunning = false;
    private Thread asyncReceivingThread;

    private JcsMessageQueue(){
        super();
        connect( AwsConfiguration.AWS_QUEUE_URL );
    }

    /**
     * get an instance of the jcs message queue (singleton)
     * @return message queue instance
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
    }

    /**
     *
     */
    public void decreasePendingCloudCalculation(){
        if( pendingCloudCalculation > 0){
            pendingCloudCalculation--;
        }

        if( pendingCloudCalculation == 0 && asyncReceiverAlreadyRunning ){
            asyncReceivingThread.interrupt();
            asyncReceiverAlreadyRunning = false;
            Logger.info( "async receiver stopped" );
        }
    }

    /**
     *
     */
    public void startAsyncReceiving(){

        // stop if it's already running
        if( asyncReceiverAlreadyRunning ){
            return;
        }

        asyncReceiverAlreadyRunning = true;
        Logger.info( "async receiver started" );

        asyncReceivingThread = new Thread() {
            public void run() {
                try {
                    while( pendingCloudCalculation > 0 ) {
                        ReceiveMessageRequest receiveRq = new ReceiveMessageRequest()
                                .withQueueUrl( url );

                        /*Future<ReceiveMessageResult> asyncReceiving = bufferedSqs.receiveMessageAsync(receiveRq);
                        List<Message> messages = asyncReceiving.get().getMessages();*/

                        ReceiveMessageResult receiving = bufferedSqs.receiveMessage(receiveRq);
                        List<Message> messages = receiving.getMessages();


                        System.out.println( "SIZE: " + messages.size() );
                        for (Message message : messages ) {
                            System.out.println( message.toString() );
                            QueueItem queueItem = gson.fromJson( message.getBody(), QueueItem.class );

                            String messageReceiptHandle = message.getReceiptHandle();

                            if( registeredObjects.containsKey( queueItem.receiverId ) ){
                                asyncHandleItemFromQueue( queueItem );

                                System.out.println( "done****" );
                                // remove message
                                bufferedSqs.deleteMessage(new DeleteMessageRequest( url, messageReceiptHandle ) );
                            }
                            else
                            {
                                ChangeMessageVisibilityRequest visibilityRequest = new ChangeMessageVisibilityRequest();
                                visibilityRequest.setReceiptHandle( messageReceiptHandle );
                                visibilityRequest.setQueueUrl( url );
                                visibilityRequest.setVisibilityTimeout( 0 );
                                bufferedSqs.changeMessageVisibility( visibilityRequest );
                                System.out.println( "done" );
                            }
                        }
                    }
                }
                catch ( Exception e ){
                    // ignore exceptions because, it always fails if we stop the thread
                }
            }
        };
        asyncReceivingThread.start();
    }

    public QueueItem receiveSyncMessage( String responseSenderId ){
        while( pendingCloudCalculation > 0 ) {
            ReceiveMessageRequest receiveRq = new ReceiveMessageRequest()
                    .withMaxNumberOfMessages(10)
                    .withQueueUrl( url );

            ReceiveMessageResult receiving = bufferedSqs.receiveMessage(receiveRq);
            List<Message> messages = receiving.getMessages();

            System.out.println( "SIZE: " + messages.size() );
            for (Message message : messages ) {
                try {
                    QueueItem queueItem = gson.fromJson(message.getBody(), QueueItem.class);

                    String messageReceiptHandle = message.getReceiptHandle();
                    if (queueItem.receiverId.equals(responseSenderId)) {
                        // remove message
                        bufferedSqs.deleteMessage(new DeleteMessageRequest(url, messageReceiptHandle));
                        return queueItem;
                    } else {
                        ChangeMessageVisibilityRequest visibilityRequest = new ChangeMessageVisibilityRequest();
                        visibilityRequest.setReceiptHandle(messageReceiptHandle);
                        visibilityRequest.setQueueUrl(url);
                        visibilityRequest.setVisibilityTimeout(0);
                        bufferedSqs.changeMessageVisibility(visibilityRequest);
                    }
                }
                catch ( Exception e ){
                    e.printStackTrace();
                }
            }
        }

        throw new RuntimeVariableReferenceException( "Unknown error while receiving message from client" );
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
    private void asyncHandleItemFromQueue( QueueItem queueItem ){
        Object context = registeredObjects.get( queueItem.receiverId );

        if( queueItem.queueType == QueueType.REQUEST ){
            if( queueItem.invokeType == InvokeType.SET ){
                handleSet( queueItem, context );
            }
            else if( queueItem.invokeType == InvokeType.GET ){
                handleGet( queueItem, context );
            }
        }
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
            Field field = context.getClass().getDeclaredField( fieldName );
            field.setAccessible( true );
            field.set( context, setObject );
        }
        catch ( Exception e ){
            e.printStackTrace();
            throw new RuntimeVariableReferenceException( "Error while handle set request" );
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
            Field field = context.getClass().getDeclaredField( fieldName );
            field.setAccessible( true );
            Object returnObject = field.get( context );

            sendResponseMessage( queueItem, returnObject );
        }
        catch ( Exception e ){
            e.printStackTrace();
            throw new RuntimeVariableReferenceException( "Error while handle get request" );
        }
    }

    /**
     *
     * @param ReceivedQueueItem
     * @param returnObject
     */
    private void sendResponseMessage( QueueItem ReceivedQueueItem, Object returnObject ){
        QueueItem queueItem = new QueueItem();
        queueItem.senderId = ReceivedQueueItem.receiverId;
        queueItem.receiverId = ReceivedQueueItem.senderId;
        queueItem.queueType = QueueType.RESPONSE;
        queueItem.invokeType = ReceivedQueueItem.invokeType;
        queueItem.variable = ReceivedQueueItem.variable;
        queueItem.variableType = ReceivedQueueItem.variableType;
        queueItem.body = gson.toJson( returnObject );

        sendMessage( gson.toJson( queueItem ) );
    }
}
