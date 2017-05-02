package ch.uzh.ifi.seal.jcs_lambda.cloudprovider.lambdaFunction;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.AmazonWebService;
import ch.uzh.ifi.seal.jcs_lambda.configuration.AwsConfiguration;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AmazonSimpleStorageService {
    private static AmazonSimpleStorageService instance = null;
    private AmazonS3 amazonS3;
    private String s3Bucketname;

    private AmazonSimpleStorageService () {
        // Create Amazon S3 Object
        amazonS3 = AmazonS3ClientBuilder.standard()
                .withCredentials( new AWSStaticCredentialsProvider( AmazonWebService.getCredentials() ) )
                .withRegion( AwsConfiguration.AWS_REGION )
                .build();
        Logger.info( "Init Amazon S3 Credentials" );
    }

    public static AmazonSimpleStorageService getInstance(){
        if( instance == null ){
            instance = new AmazonSimpleStorageService();
        }

        return instance;
    }

    /**
     * Upload a jar-File to Amazon S3
     * @param file path to the file, that we would upload
     * @return the id of the uploaded file
     */
    public FunctionCode uploadFile(File file ){
        try {
            // bucket name must be unique over all users
            s3Bucketname = AwsConfiguration.AWS_BUCKET_PREFIX + UUID.randomUUID();

            // create a new bucket
            amazonS3.createBucket( s3Bucketname );
            Logger.info( "Create a bucket '" + s3Bucketname + "' in Amazon S3 " );

            // upload file
            amazonS3.putObject( new PutObjectRequest( s3Bucketname, file.getName(), file ) );
            Logger.info( "File '" + file.getName() + "' uploaded in Amazon S3" );

            FunctionCode functionCode = new FunctionCode();
            functionCode.setS3Bucket( s3Bucketname );
            functionCode.setS3Key( file.getName() );

            return functionCode;
        }
        catch ( Exception e ){
            AmazonWebService.logException( e );
            return null;
        }
    }

    /**
     * Remove a bucket in Amazon S3
     * @param bucketName name of the bucket
     */
    private void removeBucket ( String bucketName ){
        // get all files in the bucket
        ObjectListing objectListing = amazonS3.listObjects( bucketName );

        // Empty the bucket (remove all files)
        DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest( bucketName );
        List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>();
        for ( S3ObjectSummary objectSummary : objectListing.getObjectSummaries() ) {
            keys.add( new DeleteObjectsRequest.KeyVersion( objectSummary.getKey() ) );
        }
        multiObjectDeleteRequest.setKeys( keys );

        // If bucket is not empty, delete all files
        if( keys.size() > 0){
            amazonS3.deleteObjects(multiObjectDeleteRequest);
            Logger.info( "All Files in the bucket '" + bucketName + "' were removed" );
        }

        // remove bucket itself
        amazonS3.deleteBucket( bucketName );
        Logger.info( "Delete a bucket '" + bucketName + "' in Amazon S3" );
    }

    /**
     * Remove all buckets in S3 with our prefix
     */
    public void removeAllTemporaryCreatedBuckets(){
        try{
            // Get all buckets
            ListBucketsRequest listBucketsRequest = new ListBucketsRequest();
            List<Bucket> buckets = amazonS3.listBuckets( listBucketsRequest );

            for( Bucket bucket : buckets ){
                // Check if bucket has prefix (= is only temporary)
                if( bucket.getName().startsWith( AwsConfiguration.AWS_BUCKET_PREFIX ) ){
                    removeBucket( bucket.getName() );
                }
            }

        }
        catch ( Exception e ){
            AmazonWebService.logException( e );
        }
    }
}
