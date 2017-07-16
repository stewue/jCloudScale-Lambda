package ch.uzh.ifi.seal.jcs_lambda.cloudprovider.lambdaFunction;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.AmazonWebService;
import ch.uzh.ifi.seal.jcs_lambda.configuration.AwsConfiguration;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.DeleteLogGroupRequest;

public class AmazonCloudWatchLog {
    private static AmazonCloudWatchLog instance = null;
    private AWSLogs cloudWatchLogs;

    private AmazonCloudWatchLog() {
        // Create Amazon S3 Object
        cloudWatchLogs = AWSLogsClientBuilder.standard()
                .withCredentials( new AWSStaticCredentialsProvider( AmazonWebService.getCredentials() ) )
                .withRegion( AwsConfiguration.AWS_REGION )
                .build();
        Logger.info( "Init Amazon Cloud Watch Logs Credentials" );
    }

    public static AmazonCloudWatchLog getInstance(){
        if( instance == null ){
            instance = new AmazonCloudWatchLog();
        }

        return instance;
    }

    /**
     * Delete a log group of the cloud watch
     * @param logGroupName name of the log group
     */
    public void deleteLogGroup ( String logGroupName ){
        try {
            DeleteLogGroupRequest deleteLogGroupRequest = new DeleteLogGroupRequest();
            deleteLogGroupRequest.setLogGroupName(logGroupName);
            cloudWatchLogs.deleteLogGroup(deleteLogGroupRequest);

            Logger.info("Remove Log Group: " + logGroupName);
        }
        catch ( Exception e ){

        }
    }
}
