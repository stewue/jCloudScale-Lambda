package ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.AmazonWebService;
import ch.uzh.ifi.seal.jcs_lambda.configuration.AwsConfiguration;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class MessageQueue {
    protected static MessageQueue instance = null;

    protected AmazonSQS amazonSQS;

    protected String url;

    /**
     *
     */
    protected MessageQueue (){
        long startTimestamp = System.currentTimeMillis();

        AmazonSQSClientBuilder clientBuilder = AmazonSQSClientBuilder.standard();
        clientBuilder = clientBuilder.withCredentials( new AWSStaticCredentialsProvider( AmazonWebService.getCredentials() ) );
        clientBuilder = clientBuilder.withRegion( AwsConfiguration.AWS_REGION );
        amazonSQS = clientBuilder.build();

        Logger.debug( "Time to init message queue: " + ( ( System.currentTimeMillis() - startTimestamp ) / 1000.0 ) + " sec" );
    }

    /**
     * get an instance of the the message queue (singleton)
     * @return message queue instance
     */
    public static MessageQueue getInstance(){
        if( instance == null ){
            instance = new MessageQueue();
        }

        return instance;
    }

    /**
     *
     * @param url
     */
    public void connect ( String url ){
        this.url = url;
    }

    /**
     *
     * @param body
     */
    public void sendMessage ( String body ){
        SendMessageRequest request = new SendMessageRequest();
        request.setMessageBody( body );
        request.setQueueUrl( url );

        amazonSQS.sendMessage( request );

        Logger.debug( "Message sent with body: " + body );
    }
}
