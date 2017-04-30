package ch.uzh.ifi.seal.jcs_lambda.cloudprovider;

import ch.uzh.ifi.seal.jcs_lambda.configuration.AwsCredentials;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;

public class AmazonWebService {
    private static BasicAWSCredentials basicAWSCredentials;

    /**
     * get credential
     * @return credential object
     */
    public static BasicAWSCredentials getCredentials () {
        if( basicAWSCredentials == null ) {
            // Create AWS Credentials
            basicAWSCredentials = new BasicAWSCredentials(AwsCredentials.AWS_ACCESS_KEY_ID, AwsCredentials.AWS_SECRET_KEY_ID);
            Logger.info("Init AWS Credentials");
        }

        return basicAWSCredentials;
    }

    /**
     * output a detail error message from the amazon api
     * @param e an amazon exception
     */
    public static void logException ( Exception e ){
        if( e instanceof AmazonServiceException) {
            AmazonServiceException ase = (AmazonServiceException) e;

            Logger.error( "Caught an AmazonServiceException, which means your request made it to Amazon, but was rejected with an error response for some reason." );
            Logger.error( "Error Message:    " + e.getMessage() );
            Logger.error( "HTTP Status Code: " + ase.getStatusCode() );
            Logger.error( "AWS Error Code:   " + ase.getErrorCode() );
            Logger.error( "Error Type:       " + ase.getErrorType() );
            Logger.error( "Request ID:       " + ase.getRequestId() );
        }
        else if ( e instanceof AmazonClientException) {
            AmazonClientException ace = (AmazonClientException) e;

            Logger.error("Caught an AmazonClientException, which means the client encountered a serious internal problem while trying to communicate with, such as not being able to access the network." );
            Logger.error("Error Message: " + ace.getMessage() );
        }

        System.exit( -1 );
    }
}
