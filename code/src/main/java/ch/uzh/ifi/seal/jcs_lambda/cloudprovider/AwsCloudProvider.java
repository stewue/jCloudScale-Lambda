package ch.uzh.ifi.seal.jcs_lambda.cloudprovider;

import ch.uzh.ifi.seal.jcs_lambda.configuration.AwsConfiguration;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import ch.uzh.ifi.seal.jcs_lambda.management.MethodDescription;
import ch.uzh.ifi.seal.jcs_lambda.utility.AwsUtil;
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
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AwsCloudProvider {

    private static AwsCloudProvider instance = null;
    private static Gson gson = new Gson();

    private BasicAWSCredentials basicAWSCredentials;

    private AmazonIdentityManagement awsIAM;
    private String roleARN;

    private AmazonApiGateway amazonApiGateway;
    private String restApiId;
    private Resource rootResource;

    private AmazonS3 amazonS3;
    private String s3Bucketname;

    private AWSLambda amazonLamdba;
    private HashMap<String, MethodDescription> lambdaFunctionDescriptions = new HashMap<>();

    /**
     * login to all aws services with the credential
     */
    private AwsCloudProvider (){
        // Create AWS Credentials
        basicAWSCredentials = new BasicAWSCredentials( AwsConfiguration.AWS_ACCESS_KEY_ID, AwsConfiguration.AWS_SECRET_KEY_ID );
        Logger.info( "Init AWS Credentials" );

        // Create IAM (Identity and Access Management) Object
        awsIAM = AmazonIdentityManagementClientBuilder.standard()
            .withCredentials( new AWSStaticCredentialsProvider(basicAWSCredentials) )
            .withRegion( AwsConfiguration.AWS_REGION )
            .build();
        Logger.info( "Init IAM Credentials" );

        // Create ApiGateway Object
        amazonApiGateway = AmazonApiGatewayClientBuilder.standard()
                .withCredentials( new AWSStaticCredentialsProvider(basicAWSCredentials) )
                .withRegion( AwsConfiguration.AWS_REGION )
                .build();
        Logger.info( "Init ApiGateway Credentials" );

        // Create Amazon S3 Object
        amazonS3 = AmazonS3ClientBuilder.standard()
            .withCredentials( new AWSStaticCredentialsProvider(basicAWSCredentials) )
            .withRegion( AwsConfiguration.AWS_REGION )
            .build();
        Logger.info( "Init Amazon S3 Credentials" );

        // Create Amazon Lambda Object
        amazonLamdba = AWSLambdaClientBuilder.standard()
            .withCredentials( new AWSStaticCredentialsProvider(basicAWSCredentials) )
            .withRegion( AwsConfiguration.AWS_REGION )
            .build();
        Logger.info( "Init AWS Lambda Credentials" );

        // Get all lambda functions (and their descriptions) and save them as a hash-map
        ListFunctionsResult lambdaFunctions = amazonLamdba.listFunctions();
        for( FunctionConfiguration item  : lambdaFunctions.getFunctions() ){
            try {
                MethodDescription description = gson.fromJson(item.getDescription(), MethodDescription.class);

                if( description != null ){
                    lambdaFunctionDescriptions.put(item.getFunctionName(), description);
                }
            }
            catch ( Exception e ){

            }
        }

        getRestApiId();
    }

    public static AwsCloudProvider getInstance(){
        if( instance == null ){
            instance = new AwsCloudProvider();
        }

        return instance;
    }

    public MethodDescription getLambdaFunctionDescription ( String functionName ){
        String awsFunctioName = AwsUtil.convertMethodName( functionName );

        return lambdaFunctionDescriptions.get( awsFunctioName );
    }

    public String getBaseUrl (){
        return "https://" + restApiId + ".execute-api." + AwsConfiguration.AWS_REGION.getName() + ".amazonaws.com/" + AwsConfiguration.AWS_API_GATEWAY_STAGE_NAME + "/";
    }

    private void getRestApiId (){
        // Get the id of the restAPI
        if( restApiId == null ){
            GetRestApisResult getRestApisResult = amazonApiGateway.getRestApis( new GetRestApisRequest() );
            for( RestApi item : getRestApisResult.getItems() ){
                if( item.getName().equals( AwsConfiguration.AWS_API_GATEWAY_NAME ) ){
                    restApiId = item.getId();
                    break;
                }
            }
        }

        // if no restAPI exists then create it
        if( restApiId == null){
            // Create Rest API
            CreateRestApiRequest createRestApiRequest = new CreateRestApiRequest();
            createRestApiRequest.setName( AwsConfiguration.AWS_API_GATEWAY_NAME );
            CreateRestApiResult createRestApiResult = amazonApiGateway.createRestApi( createRestApiRequest );
            restApiId = createRestApiResult.getId();
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
            logException( e );
        }
    }

    /**
     * Upload a jar-File to Amazon S3
     * @param file path to the file, that we would upload
     * @return the id of the uploaded file
     */
    public FunctionCode uploadFile( File file ){
        try {
            // check if already a temporary bucket, for uploading the files, exists
            if( s3Bucketname == null ){
                // bucket name must be unique over all users
                s3Bucketname = AwsConfiguration.AWS_BUCKET_PREFIX + "-" + UUID.randomUUID();

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
        catch ( Exception e ){
            logException( e );
        }

        return null;
    }

    /**
     * create a lambda function with the uploaded file from S3
     * @param functionName the name of the function that we create
     * @param handlerName the start point of the execution
     * @param functionCode Amazon S3 id of the uploaded file
     * @param description methodDescription object
     */
    public void createOrUpdateFunction ( String functionName, String handlerName, FunctionCode functionCode, MethodDescription description ){
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

        try {
            // update or create function
            CreateFunctionResult createFunctionResult = null;
            if (functionExists) {
                // Update function code
                UpdateFunctionCodeRequest functionCodeRequest = new UpdateFunctionCodeRequest();
                functionCodeRequest.setFunctionName(functionName);
                functionCodeRequest.setS3Bucket(functionCode.getS3Bucket());
                functionCodeRequest.setS3Key(functionCode.getS3Key());
                amazonLamdba.updateFunctionCode(functionCodeRequest);

                // update function configuration
                UpdateFunctionConfigurationRequest functionConfigurationRequest = new UpdateFunctionConfigurationRequest();
                functionConfigurationRequest.setFunctionName(functionName);
                functionConfigurationRequest.setDescription( gson.toJson( description ) );
                functionConfigurationRequest.setHandler(handlerName);
                functionConfigurationRequest.setRole(getRole());
                functionConfigurationRequest.setTimeout(AwsConfiguration.AWS_TIMEOUT);
                functionConfigurationRequest.setMemorySize(AwsConfiguration.AWS_DEFAULT_MEMORY_SIZE);
                amazonLamdba.updateFunctionConfiguration(functionConfigurationRequest);

                Logger.info("Lambda Function '" + functionName + "' updated");
            } else {
                // Create the function
                CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest();
                createFunctionRequest.setFunctionName(functionName);
                createFunctionRequest.setDescription( gson.toJson( description ) );
                createFunctionRequest.setCode(functionCode);
                createFunctionRequest.setHandler(handlerName);
                createFunctionRequest.setPublish(true);
                createFunctionRequest.setRole(getRole());
                createFunctionRequest.setRuntime(Runtime.Java8);
                createFunctionRequest.setTimeout(AwsConfiguration.AWS_TIMEOUT);
                createFunctionRequest.setMemorySize(AwsConfiguration.AWS_DEFAULT_MEMORY_SIZE);
                createFunctionResult = amazonLamdba.createFunction(createFunctionRequest);

                // Set Permission for gateway api
                AddPermissionRequest addPermissionRequest = new AddPermissionRequest();
                addPermissionRequest.setFunctionName(createFunctionResult.getFunctionArn());
                addPermissionRequest.setAction("lambda:*");
                addPermissionRequest.setPrincipal("apigateway.amazonaws.com");
                addPermissionRequest.setStatementId(UUID.randomUUID().toString());
                amazonLamdba.addPermission(addPermissionRequest);

                Logger.info("Lambda Function '" + functionName + "' created");

                createGatewayAPI(functionName, createFunctionResult.getFunctionArn());
            }
        }
        catch ( Exception e ) {
            logException( e );
        }
    }

    /**
     * create a rest-endpoint for calling it from outside of the amazon cloud
     * @param functionName the name of the function, that is now a part of the path
     * @param functionARN the arn of the function
     */
    private void createGatewayAPI ( String functionName, String functionARN ){
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
        putIntegrationRequest.setUri( "arn:aws:apigateway:" + AwsConfiguration.AWS_REGION.getName() + ":lambda:path/2015-03-31/functions/" + functionARN + "/invocations" );
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
        createDeploymentRequest.setStageName( AwsConfiguration.AWS_API_GATEWAY_STAGE_NAME );
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
            getRoleRequest.setRoleName( AwsConfiguration.AWS_ROLE_NAME );

            GetRoleResult getRoleResult = null;
            try {
                getRoleResult = awsIAM.getRole( getRoleRequest );
            }
            catch ( NoSuchEntityException ex ){

            }

            if( getRoleResult != null ){
                // save only roleARN
                roleARN = getRoleResult.getRole().getArn();

                Logger.info( "IAM role '" + AwsConfiguration.AWS_ROLE_NAME + "' loaded" );
            }
            else{

                // create a new role
                CreateRoleRequest createRoleRequest = new CreateRoleRequest();
                createRoleRequest.setAssumeRolePolicyDocument( "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"Service\":\"lambda.amazonaws.com\"},\"Action\":\"sts:AssumeRole\"}]}" );
                createRoleRequest.setRoleName( AwsConfiguration.AWS_ROLE_NAME );
                CreateRoleResult createRoleResult = awsIAM.createRole( createRoleRequest );

                roleARN = createRoleResult.getRole().getArn();

                // set policy for role
                AttachRolePolicyRequest attachRolePolicyRequest = new AttachRolePolicyRequest();
                attachRolePolicyRequest.setRoleName( AwsConfiguration.AWS_ROLE_NAME );
                attachRolePolicyRequest.setPolicyArn( "arn:aws:iam::aws:policy/AWSLambdaFullAccess" );
                awsIAM.attachRolePolicy( attachRolePolicyRequest );

                Logger.info( "IAM role '" + AwsConfiguration.AWS_ROLE_NAME + "' created and policy set" );
            }

        }

        return roleARN;
    }

    private void logException ( Exception e ){
        if( e instanceof AmazonServiceException ) {
            AmazonServiceException ase = (AmazonServiceException) e;

            Logger.error( "Caught an AmazonServiceException, which means your request made it to Amazon, but was rejected with an error response for some reason." );
            Logger.error( "Error Message:    " + e.getMessage() );
            Logger.error( "HTTP Status Code: " + ase.getStatusCode() );
            Logger.error( "AWS Error Code:   " + ase.getErrorCode() );
            Logger.error( "Error Type:       " + ase.getErrorType() );
            Logger.error( "Request ID:       " + ase.getRequestId() );
        }
        else if ( e instanceof AmazonClientException ) {
            AmazonClientException ace = (AmazonClientException) e;

            Logger.error("Caught an AmazonClientException, which means the client encountered a serious internal problem while trying to communicate with, such as not being able to access the network." );
            Logger.error("Error Message: " + ace.getMessage() );
        }
    }
}
