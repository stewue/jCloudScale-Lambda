package ch.uzh.ifi.seal.jcs_lambda.cloudprovider.lambdaFunction;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.AmazonWebService;
import ch.uzh.ifi.seal.jcs_lambda.configuration.AwsConfiguration;
import ch.uzh.ifi.seal.jcs_lambda.exception.InvalidCredentialsException;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import ch.uzh.ifi.seal.jcs_lambda.management.FunctionDescription;
import ch.uzh.ifi.seal.jcs_lambda.utility.AwsUtil;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.lambda.model.Runtime;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.UUID;

public class AmazonLambda {

    private static AmazonLambda instance = null;
    private static Gson gson = new Gson();

    private AmazonApiGateway amazonApiGateway;

    private AWSLambda amazonLambda;
    private HashMap<String, FunctionDescription> lambdaFunctionDescriptions = new HashMap<>();

    /**
     * login to all aws services with the credential
     */
    private AmazonLambda(){
        // Create Amazon Lambda Object
        amazonLambda = AWSLambdaClientBuilder.standard()
                .withCredentials( new AWSStaticCredentialsProvider( AmazonWebService.getCredentials() ) )
                .withRegion( AwsConfiguration.AWS_REGION )
                .build();
        Logger.info( "Init AWS Lambda Credentials" );

        loadAllLambdaFunctions();

        amazonApiGateway = AmazonApiGateway.getInstance();
        amazonApiGateway.getRestApiId();
    }

    /**
     * get an instance of the cloud provider (singleton)
     * @return aws cloud provider instance
     */
    public static AmazonLambda getInstance(){
        if( instance == null ){
            instance = new AmazonLambda();
        }

        return instance;
    }

    /**
     * get all lambda functions with our prefix, that exists in aws
     */
    private void loadAllLambdaFunctions () {
        ListFunctionsResult lambdaFunctions = null;
        // Get all lambda functions (and their descriptions) and save them as a hash-map
        try {
            lambdaFunctions = amazonLambda.listFunctions();
        }
        catch ( Exception e ){
            throw new InvalidCredentialsException();
        }

        // save for each function the description
        for( FunctionConfiguration item  : lambdaFunctions.getFunctions() ){
            FunctionDescription description = gson.fromJson(item.getDescription(), FunctionDescription.class);

            String functionNameWithPrefix = item.getFunctionName();
            if( description != null && functionNameWithPrefix.startsWith( AwsConfiguration.AWS_FUNCTION_PREFIX ) ){
                // remove prefix
                String functionName = functionNameWithPrefix.substring( AwsConfiguration.AWS_FUNCTION_PREFIX.length() );

                lambdaFunctionDescriptions.put( functionName, description);
            }
        }

        Logger.info( "Get list of all lambda functions" );
    }

    /**
     * check if a method in the cloud already exists
     * @param methodName full qualified name of the method
     * @return return status
     */
    public boolean existsFunction(String methodName ){
        String awsFunctionName = AwsUtil.convertMethodName( methodName );

        return lambdaFunctionDescriptions.get( awsFunctionName ) != null;
    }

    /**
     * get base url of rest end-points
     * @return a string with the base url
     */
    public String getBaseUrl (){
        return "https://" + amazonApiGateway.getRestApiId() + ".execute-api." + AwsConfiguration.AWS_REGION.getName() + ".amazonaws.com/" + AwsConfiguration.AWS_API_GATEWAY_STAGE_NAME + "/";
    }

    /**
     * create a lambda function with the uploaded file from S3
     * @param functionName the name of the function that we create
     * @param handlerName the start point of the execution
     * @param functionCode Amazon S3 id of the uploaded file
     * @param description methodDescription object
     * @param memory int with memory size (in mb)
     * @param timeout int with timeout amount (in sec)
     */
    public void createOrUpdateFunction ( String functionName, String handlerName, FunctionCode functionCode, FunctionDescription description, int memory, int timeout ){
        // Check if function name already exists
        GetFunctionRequest getFunctionRequest = new GetFunctionRequest();
        getFunctionRequest.setFunctionName( functionName );

        boolean functionExists = true;
        try {
            amazonLambda.getFunction( getFunctionRequest );
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
                amazonLambda.updateFunctionCode(functionCodeRequest);

                // update function configuration
                UpdateFunctionConfigurationRequest functionConfigurationRequest = new UpdateFunctionConfigurationRequest();
                functionConfigurationRequest.setFunctionName(functionName);
                functionConfigurationRequest.setDescription( gson.toJson( description ) );
                functionConfigurationRequest.setHandler(handlerName);
                functionConfigurationRequest.setRole( AwsConfiguration.AWS_ROLE_ARN );
                functionConfigurationRequest.setTimeout( timeout );
                functionConfigurationRequest.setMemorySize( memory );
                amazonLambda.updateFunctionConfiguration(functionConfigurationRequest);

                Logger.info("Lambda Function '" + functionName + "' updated");
            } else {
                // Create the function
                CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest();
                createFunctionRequest.setFunctionName(functionName);
                createFunctionRequest.setDescription( gson.toJson( description ) );
                createFunctionRequest.setCode(functionCode);
                createFunctionRequest.setHandler(handlerName);
                createFunctionRequest.setPublish(true);
                createFunctionRequest.setRole( AwsConfiguration.AWS_ROLE_ARN );
                createFunctionRequest.setRuntime(Runtime.Java8);
                createFunctionRequest.setTimeout( timeout );
                createFunctionRequest.setMemorySize( memory );
                createFunctionResult = amazonLambda.createFunction(createFunctionRequest);

                // Set Permission for gateway api
                AddPermissionRequest addPermissionRequest = new AddPermissionRequest();
                addPermissionRequest.setFunctionName(createFunctionResult.getFunctionArn());
                addPermissionRequest.setAction("lambda:*");
                addPermissionRequest.setPrincipal("apigateway.amazonaws.com");
                addPermissionRequest.setStatementId(UUID.randomUUID().toString());
                amazonLambda.addPermission(addPermissionRequest);

                Logger.info("Lambda Function '" + functionName + "' created");

                amazonApiGateway.createGatewayAPI(functionName, createFunctionResult.getFunctionArn());
            }
        }
        catch ( Exception e ) {
            AmazonWebService.logException( e );
        }
    }
}