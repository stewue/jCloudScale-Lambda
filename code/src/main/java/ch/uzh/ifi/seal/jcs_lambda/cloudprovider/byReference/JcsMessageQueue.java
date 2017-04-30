package ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.JVMContext;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.dto.InvokeType;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.dto.QueueItem;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.dto.QueueType;
import ch.uzh.ifi.seal.jcs_lambda.configuration.AwsConfiguration;
import ch.uzh.ifi.seal.jcs_lambda.exception.RuntimeReferenceVariableException;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import ch.uzh.ifi.seal.jcs_lambda.utility.ReflectionUtil;
import com.amazonaws.services.sqs.model.*;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JcsMessageQueue extends MessageQueue {
    private static Gson gson = new Gson();

    private Map<String, Object> registeredObjects = new HashMap<>();

    private int pendingRequests = 0;
    private boolean asyncReceiverAlreadyRunning = false;
    private Thread asyncReceivingThread;

    private JcsMessageQueue(){
        super();
        connect( AwsConfiguration.AWS_QUEUE_URL );

        // local instance try to clean queue
        if( !JVMContext.getContext() ){
            purgeQueue();
        }
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
     * remove all messages from the queue
     */
    public void purgeQueue (){
        try {
            PurgeQueueRequest purgeQueueRequest = new PurgeQueueRequest();
            purgeQueueRequest.setQueueUrl(url);
            amazonSQS.purgeQueue(purgeQueueRequest);

            Logger.info("Purge SQS ");
        }
        catch ( Exception e ){
            // you can purge a queue only once a minute
        }
    }

    /**
     * increase number of pending requests
     */
    public void increasePendingRequests(){
        pendingRequests++;
    }

    /**
     * decrease number of pending requests
     */
    public void decreasePendingRequests(){
        if( pendingRequests > 0){
            pendingRequests--;
        }

        if( pendingRequests == 0 && asyncReceiverAlreadyRunning ){
            asyncReceivingThread.interrupt();
            asyncReceiverAlreadyRunning = false;
            Logger.info( "async receiver stopped" );
        }
    }

    /**
     * local client wait on request and stop if no pending request exists
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
                    while( pendingRequests > 0 ) {
                        ReceiveMessageRequest receiveRq = new ReceiveMessageRequest()
                                .withMaxNumberOfMessages(10)
                                .withQueueUrl( url );

                        ReceiveMessageResult receiving = amazonSQS.receiveMessage(receiveRq);
                        List<Message> messages = receiving.getMessages();

                        Logger.debug( "Async receiver has " + messages.size() + " messages received" );
                        for (Message message : messages ) {
                            QueueItem queueItem = gson.fromJson( message.getBody(), QueueItem.class );
                            String messageReceiptHandle = message.getReceiptHandle();

                            if( registeredObjects.containsKey( queueItem.receiverId ) ){
                                asyncHandleItemFromQueue( queueItem );

                                Logger.debug( "Handle queue item: " + queueItem.toString() );
                                // remove message
                                amazonSQS.deleteMessage(new DeleteMessageRequest( url, messageReceiptHandle ) );
                            }
                            else
                            {
                                // message not for me => release message
                                releaseMessage( messageReceiptHandle );
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

    /**
     * Cloud send some requests to local client and wait on answer
     * @param responseSenderId cloud object id
     * @return variable value
     */
    public QueueItem receiveSyncMessage( String responseSenderId ){
        while( pendingRequests > 0 ) {
            ReceiveMessageRequest receiveRq = new ReceiveMessageRequest()
                    .withMaxNumberOfMessages(10)
                    .withQueueUrl( url );

            ReceiveMessageResult receiving = amazonSQS.receiveMessage(receiveRq);
            List<Message> messages = receiving.getMessages();

            Logger.debug( "Sync receiver has " + messages.size() + " messages received" );
            for (Message message : messages ) {
                try {
                    QueueItem queueItem = gson.fromJson(message.getBody(), QueueItem.class);
                    String messageReceiptHandle = message.getReceiptHandle();

                    if (queueItem.receiverId.equals(responseSenderId)) {
                        Logger.debug( "Handle queue item: " + queueItem.toString() );

                        // remove message
                        amazonSQS.deleteMessage(new DeleteMessageRequest(url, messageReceiptHandle));
                        return queueItem;
                    } else {
                        // message not for me => release message
                        releaseMessage( messageReceiptHandle );
                    }
                }
                catch ( Exception e ){
                    e.printStackTrace();
                }
            }
        }

        throw new RuntimeReferenceVariableException( "Unknown error while receiving message from client" );
    }

    /**
     * release message (it's now visible for other receivers
     * @param messageReceiptHandle aws message receipt
     */
    private void releaseMessage( String messageReceiptHandle ){
        ChangeMessageVisibilityRequest visibilityRequest = new ChangeMessageVisibilityRequest();
        visibilityRequest.setReceiptHandle( messageReceiptHandle );
        visibilityRequest.setQueueUrl(url);
        visibilityRequest.setVisibilityTimeout(0);
        amazonSQS.changeMessageVisibility(visibilityRequest);
    }

    /**
     * register an object, that a cloud instance can get or set a variable
     * @param uuid object id
     * @param obj object this context
     */
    public void registerObject( String uuid, Object obj ){
        registeredObjects.put( uuid, obj );
    }

    /**
     * handle an asynchronous request
     * @param queueItem current queue item
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
     * handle an asynchronous set request
     * @param queueItem current queue item
     * @param context origin object
     */
    private void handleSet ( QueueItem queueItem, Object context ){
        String fieldName = queueItem.variable;

        try{
            // decode string-body to object with specified type
            Class clazz = ReflectionUtil.getClassFromString( queueItem.variableType );
            Object setObject = gson.fromJson( queueItem.body, clazz );

            // get field and set value
            Field field = context.getClass().getDeclaredField( fieldName );
            field.setAccessible( true );
            field.set( context, setObject );
        }
        catch ( Exception e ){
            e.printStackTrace();
            throw new RuntimeReferenceVariableException( "Error while handle set request" );
        }
    }

    /**
     * handle an asynchronous get request
     * @param queueItem current queue item
     * @param context origin object
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
            throw new RuntimeReferenceVariableException( "Error while handle get request" );
        }
    }

    /**
     * send a response message on a request
     * @param ReceivedQueueItem received item
     * @param returnObject response message
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
