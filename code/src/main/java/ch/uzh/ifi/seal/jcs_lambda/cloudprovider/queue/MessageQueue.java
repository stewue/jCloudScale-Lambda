package ch.uzh.ifi.seal.jcs_lambda.cloudprovider.queue;

import ch.uzh.ifi.seal.jcs_lambda.configuration.AwsConfiguration;
import ch.uzh.ifi.seal.jcs_lambda.configuration.AwsCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

import java.util.UUID;

public class MessageQueue {
    protected static MessageQueue instance = null;

    protected AmazonSQSAsync bufferedSqs;

    protected String url;

    /**
     *
     */
    protected MessageQueue (){
        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials( AwsCredentials.AWS_ACCESS_KEY_ID, AwsCredentials.AWS_SECRET_KEY_ID );

        AmazonSQSAsync sqsAsync = AmazonSQSAsyncClientBuilder.standard()
                .withCredentials( new AWSStaticCredentialsProvider(basicAWSCredentials) )
                .withRegion( AwsConfiguration.AWS_REGION )
                .build();

        // Create the buffered client
        bufferedSqs = new AmazonSQSBufferedAsyncClient(sqsAsync);
    }

    /**
     * get an instance of the the message queue (singleton)
     * @return aws cloud provider instance
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
     */
    public void create(){
        String url = AwsConfiguration.AWS_QUEUE_PREFIX + UUID.randomUUID();

        connect( url );
    }

    /**
     *
     * @param body
     */
    public void sendMessage ( String body ){
        SendMessageRequest request = new SendMessageRequest();
        request.setMessageBody( body );
        request.setQueueUrl( url );

        SendMessageResult sendResult = bufferedSqs.sendMessage( request );
    }
}
