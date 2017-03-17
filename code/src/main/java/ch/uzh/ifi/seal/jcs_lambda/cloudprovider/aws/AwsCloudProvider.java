package ch.uzh.ifi.seal.jcs_lambda.cloudprovider.aws;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.CloudProvider;
import ch.uzh.ifi.seal.jcs_lambda.configuration.JcsConfiguration;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.apigateway.model.*;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.*;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.lambda.model.Runtime;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;

import java.io.File;
import java.util.*;

public class AwsCloudProvider implements CloudProvider {

    BasicAWSCredentials basicAWSCredentials;

    AmazonIdentityManagement awsIAM;
    String roleARN;

    AmazonApiGateway amazonApiGateway;
    String restApiId;
    Resource rootResource;

    AmazonS3 amazonS3;
    String s3Bucketname;

    AWSLambda amazonLamdba;
    HashMap<String, FunctionConfiguration> lambdaFunctionConfigurations = new HashMap<>();

    /**
     * login to all aws services with the credential
     */
    public AwsCloudProvider (){
        // Create AWS Credentials
        basicAWSCredentials = new BasicAWSCredentials( JcsConfiguration.AWS_ACCESS_KEY_ID, JcsConfiguration.AWS_SECRET_KEY_ID );
        Logger.info( "Init AWS Credentials" );

        // Create IAM (Identity and Access Management) Object
        awsIAM = AmazonIdentityManagementClientBuilder.standard()
            .withCredentials( new AWSStaticCredentialsProvider(basicAWSCredentials) )
            .withRegion( JcsConfiguration.AWS_REGION )
            .build();
        Logger.info( "Init IAM Credentials" );

        // Create ApiGateway Object
        amazonApiGateway = AmazonApiGatewayClientBuilder.standard()
                .withCredentials( new AWSStaticCredentialsProvider(basicAWSCredentials) )
                .withRegion( JcsConfiguration.AWS_REGION )
                .build();
        Logger.info( "Init ApiGateway Credentials" );

        // Create Amazon S3 Object
        amazonS3 = AmazonS3ClientBuilder.standard()
            .withCredentials( new AWSStaticCredentialsProvider(basicAWSCredentials) )
            .withRegion( JcsConfiguration.AWS_REGION )
            .build();
        Logger.info( "Init Amazon S3 Credentials" );

        // Create Amazon Lambda Object
        amazonLamdba = AWSLambdaClientBuilder.standard()
            .withCredentials( new AWSStaticCredentialsProvider(basicAWSCredentials) )
            .withRegion( JcsConfiguration.AWS_REGION )
            .build();
        Logger.info( "Init AWS Lambda Credentials" );

        // Get all lambda functions and save them as a hash-map
        ListFunctionsResult lambdaFunctions = amazonLamdba.listFunctions();
        for( FunctionConfiguration item  : lambdaFunctions.getFunctions() ){
            lambdaFunctionConfigurations.put( item.getFunctionName(), item );
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
        List<KeyVersion> keys = new ArrayList<>();
        for ( S3ObjectSummary objectSummary : objectListing.getObjectSummaries() ) {
            keys.add( new KeyVersion( objectSummary.getKey() ) );
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
    private void removeAllTemporaryBuckets (){
        // Get all buckets
        ListBucketsRequest listBucketsRequest = new ListBucketsRequest();
        List<Bucket> buckets = amazonS3.listBuckets( listBucketsRequest );

        for( Bucket bucket : buckets ){
            // Check if bucket has prefix (= is only temporary)
            if( bucket.getName().startsWith( JcsConfiguration.AWS_BUCKET_PREFIX ) ){
                removeBucket( bucket.getName() );
            }
        }
    }

    /**
     * Upload a jar-File to Amazon S3
     * @param file path to the file, that we would upload
     * @return the id of the uploaded file
     */
    private FunctionCode uploadFile( File file ){
        // check if already a temporary bucket, for uploading the files, exists
        if( s3Bucketname == null ){
            // bucket name must be unique over all users
            s3Bucketname = JcsConfiguration.AWS_BUCKET_PREFIX + "-" + UUID.randomUUID();

            // create a new bucket
            amazonS3.createBucket( s3Bucketname );
            Logger.info( "Create a bucket '" + s3Bucketname + "' in Amazon S3 " );
        }

        // upload file
        amazonS3.putObject( new PutObjectRequest( s3Bucketname, file.getName(), file ) );
        Logger.info( "File '" + file.getName() + "' uploaded in Amazon S3" );

        FunctionCode functionCode = new FunctionCode();
        functionCode.setS3Bucket( s3Bucketname );
        functionCode.setS3Key( file.getName() );

        return functionCode;
    }

    /**
     * create a lambda function with the uploaded file from S3
     * @param functionName the name of the function that we create
     * @param handlerName the start point of the execution
     * @param functionCode Amazon S3 id of the uploaded file
     */
    private void createFunction ( String functionName, String handlerName, FunctionCode functionCode ){
        // Check if function name already exists
        GetFunctionRequest getFunctionRequest = new GetFunctionRequest();
        getFunctionRequest.setFunctionName( functionName );

        boolean functionExists = true;
        try {
            amazonLamdba.getFunction( getFunctionRequest );
        }
        catch ( ResourceNotFoundException ex ){
            functionExists = false;
        }

        // update or create function
        CreateFunctionResult createFunctionResult = null;
        if( functionExists ){
            // Update function code
            UpdateFunctionCodeRequest functionCodeRequest = new UpdateFunctionCodeRequest();
            functionCodeRequest.setFunctionName( functionName );
            functionCodeRequest.setS3Bucket( functionCode.getS3Bucket() );
            functionCodeRequest.setS3Key( functionCode.getS3Key() );
            amazonLamdba.updateFunctionCode( functionCodeRequest );

            // update function configuration
            UpdateFunctionConfigurationRequest functionConfigurationRequest = new UpdateFunctionConfigurationRequest();
            functionConfigurationRequest.setFunctionName( functionName );
            functionConfigurationRequest.setHandler( handlerName );
            functionConfigurationRequest.setRole( getRole() );
            functionConfigurationRequest.setTimeout( JcsConfiguration.AWS_TIMEOUT );
            functionConfigurationRequest.setMemorySize( JcsConfiguration.AWS_DEFAULT_MEMORY_SIZE );
            amazonLamdba.updateFunctionConfiguration( functionConfigurationRequest );

            Logger.info( "Lambda Function '" + functionName + "' updated" );
        }
        else {
            // Create the function
            CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest();
            createFunctionRequest.setFunctionName( functionName );
            createFunctionRequest.setCode(  functionCode );
            createFunctionRequest.setHandler( handlerName );
            createFunctionRequest.setPublish( true );
            createFunctionRequest.setRole( getRole() );
            createFunctionRequest.setRuntime( Runtime.Java8 );
            createFunctionRequest.setTimeout( JcsConfiguration.AWS_TIMEOUT );
            createFunctionRequest.setMemorySize( JcsConfiguration.AWS_DEFAULT_MEMORY_SIZE );
            createFunctionResult = amazonLamdba.createFunction( createFunctionRequest );

            // Set Permission for gateway api
            AddPermissionRequest addPermissionRequest = new AddPermissionRequest();
            addPermissionRequest.setFunctionName( createFunctionResult.getFunctionArn() );
            addPermissionRequest.setAction( "lambda:*" );
            addPermissionRequest.setPrincipal( "apigateway.amazonaws.com" );
            addPermissionRequest.setStatementId( UUID.randomUUID().toString() );
            amazonLamdba.addPermission( addPermissionRequest );

            Logger.info( "Lambda Function '" + functionName + "' created" );

            createGatewayAPI( "TEST", createFunctionResult.getFunctionArn()  );
        }
    }

    /**
     * create a rest-endpoint for calling it from outside of the amazon cloud
     * @param functionName the name of the function, that is now a part of the path
     * @param functionARN the arn of the function
     */
    private void createGatewayAPI ( String functionName, String functionARN ){

        // Get the id of the restAPI
        if( restApiId == null ){
            GetRestApisResult getRestApisResult = amazonApiGateway.getRestApis( new GetRestApisRequest() );
            for( RestApi item : getRestApisResult.getItems() ){
                if( item.getName().equals( JcsConfiguration.AWS_API_GATEWAY_NAME ) ){
                    restApiId = item.getId();
                    break;
                }
            }
        }

        // if no restAPI exists then create it
        if( restApiId == null){
            // Create Rest API
            CreateRestApiRequest createRestApiRequest = new CreateRestApiRequest();
            createRestApiRequest.setName( JcsConfiguration.AWS_API_GATEWAY_NAME );
            CreateRestApiResult createRestApiResult = amazonApiGateway.createRestApi( createRestApiRequest );
            restApiId = createRestApiResult.getId();
        }

        // Get the root resource of the restAPI
        if( rootResource == null ) {
            // Search root resource
            GetResourcesRequest getResourcesRequest = new GetResourcesRequest();
            getResourcesRequest.setRestApiId( restApiId );
            GetResourcesResult getResourcesResult = amazonApiGateway.getResources( getResourcesRequest );

            for (Resource resource : getResourcesResult.getItems()) {
                if (resource.getParentId() == null) {
                    rootResource = resource;
                    break;
                }
            }
        }

        // Create Resource
        CreateResourceRequest createResourceRequest = new CreateResourceRequest();
        createResourceRequest.setRestApiId( restApiId );
        createResourceRequest.setParentId( rootResource.getId() );
        createResourceRequest.setPathPart( functionName );
        CreateResourceResult createResourceResult = amazonApiGateway.createResource( createResourceRequest );

        // Create Method
        PutMethodRequest putMethodRequest = new PutMethodRequest();
        putMethodRequest.setRestApiId( restApiId );
        putMethodRequest.setResourceId( createResourceResult.getId() );
        putMethodRequest.setHttpMethod( "POST" );
        putMethodRequest.setAuthorizationType( "None" );
        amazonApiGateway.putMethod( putMethodRequest );

        // Create Integration Request
        PutIntegrationRequest putIntegrationRequest = new PutIntegrationRequest();
        putIntegrationRequest.setHttpMethod( "POST" );
        putIntegrationRequest.setRestApiId( restApiId );
        putIntegrationRequest.setResourceId( createResourceResult.getId() );
        putIntegrationRequest.setType( IntegrationType.AWS );
        putIntegrationRequest.setUri( "arn:aws:apigateway:" + JcsConfiguration.AWS_REGION.getName() + ":lambda:path/2015-03-31/functions/" + functionARN + "/invocations" );
        putIntegrationRequest.setIntegrationHttpMethod( "POST" );
        putIntegrationRequest.setContentHandling( ContentHandlingStrategy.CONVERT_TO_TEXT );
        amazonApiGateway.putIntegration( putIntegrationRequest );

        // Create Method Response
        PutMethodResponseRequest putMethodResponseRequest = new PutMethodResponseRequest();
        putMethodResponseRequest.setHttpMethod( "POST" );
        putMethodResponseRequest.setRestApiId( restApiId );
        putMethodResponseRequest.setResourceId( createResourceResult.getId() );
        putMethodResponseRequest.setStatusCode( "200" );
        putMethodResponseRequest.setResponseModels( new HashMap<String, String>(){{ put("application/json","Empty"); }} );
        amazonApiGateway.putMethodResponse( putMethodResponseRequest );

        // Create Integration Response
        PutIntegrationResponseRequest putIntegrationResponseRequest = new PutIntegrationResponseRequest();
        putIntegrationResponseRequest.setHttpMethod( "POST" );
        putIntegrationResponseRequest.setRestApiId( restApiId );
        putIntegrationResponseRequest.setResourceId( createResourceResult.getId() );
        putIntegrationResponseRequest.setStatusCode( "200" );
        amazonApiGateway.putIntegrationResponse( putIntegrationResponseRequest );

        // Create new deployment stage
        // ToDo do this step only once after all changes
        CreateDeploymentRequest createDeploymentRequest = new CreateDeploymentRequest();
        createDeploymentRequest.setRestApiId( restApiId );
        createDeploymentRequest.setStageName( JcsConfiguration.AWS_API_GATEWAY_STAGE_NAME );
        CreateDeploymentResult createDeploymentResult = amazonApiGateway.createDeployment( createDeploymentRequest );

        Logger.info( "API Gateway for Lambda Function '" + functionName + "' created" );
    }

    /**
     * create a role and set the policy, that the role has only access to aws lambda
     * @return the arn of the role
     */
    private String getRole (){
        if( roleARN == null ){
            // Check if role already exists
            GetRoleRequest getRoleRequest = new GetRoleRequest();
            getRoleRequest.setRoleName( JcsConfiguration.AWS_ROLE_NAME );

            GetRoleResult getRoleResult = null;
            try {
                getRoleResult = awsIAM.getRole( getRoleRequest );
            }
            catch ( NoSuchEntityException ex ){
            }

            if( getRoleResult != null ){
                // save only roleARN
                roleARN = getRoleResult.getRole().getArn();

                Logger.info( "IAM role '" + JcsConfiguration.AWS_ROLE_NAME + "' loaded" );
            }
            else{

                // create a new role
                CreateRoleRequest createRoleRequest = new CreateRoleRequest();
                createRoleRequest.setAssumeRolePolicyDocument( "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"Service\":\"lambda.amazonaws.com\"},\"Action\":\"sts:AssumeRole\"}]}" );
                createRoleRequest.setRoleName( JcsConfiguration.AWS_ROLE_NAME );
                CreateRoleResult createRoleResult = awsIAM.createRole( createRoleRequest );

                roleARN = createRoleResult.getRole().getArn();

                // set policy for role
                AttachRolePolicyRequest attachRolePolicyRequest = new AttachRolePolicyRequest();
                attachRolePolicyRequest.setRoleName( JcsConfiguration.AWS_ROLE_NAME );
                attachRolePolicyRequest.setPolicyArn( "arn:aws:iam::aws:policy/AWSLambdaFullAccess" );
                awsIAM.attachRolePolicy( attachRolePolicyRequest );

                Logger.info( "IAM role '" + JcsConfiguration.AWS_ROLE_NAME + "' created and policy set" );
            }

        }

        return roleARN;
    }

    /**
     * Upload a function to AWS and register it
     * @param functionName the name of the function that we create
     * @param handlerName the start point of the execution
     * @param file path to the file, that we would upload
     */
    @Override
    public void registerMethod ( String functionName, String handlerName, File file ){
        try {
            // Check if lambda function already exists and isn't change (same checksum)
            // TODO checksum
            if( lambdaFunctionConfigurations.get( functionName ) == null ){
                // Upload File to S3
                FunctionCode functionCode = uploadFile( file );

                // Create Function with uploaded File
                createFunction( functionName, handlerName, functionCode );

                System.out.println( functionName + " => https://" + restApiId + ".execute-api." + JcsConfiguration.AWS_REGION.getName() + ".amazonaws.com/" + JcsConfiguration.AWS_API_GATEWAY_STAGE_NAME + "/" + functionName );

                // ToDo: only once after all Methods are registered
                // Remove buckets
                removeAllTemporaryBuckets();
            }
        }
        catch (AmazonServiceException ase) {
            Logger.error( "Caught an AmazonServiceException, which means your request made it to Amazon, but was rejected with an error response for some reason." );
            Logger.error( "Error Message:    " + ase.getMessage() );
            Logger.error( "HTTP Status Code: " + ase.getStatusCode() );
            Logger.error( "AWS Error Code:   " + ase.getErrorCode() );
            Logger.error( "Error Type:       " + ase.getErrorType() );
            Logger.error( "Request ID:       " + ase.getRequestId() );
        }
        catch (AmazonClientException ace) {
            Logger.error("Caught an AmazonClientException, which means the client encountered a serious internal problem while trying to communicate with, such as not being able to access the network." );
            Logger.error("Error Message: " + ace.getMessage() );
        }
    }
}
