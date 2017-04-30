package ch.uzh.ifi.seal.jcs_lambda.cloudprovider.lambdaFunction;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.AmazonWebService;
import ch.uzh.ifi.seal.jcs_lambda.configuration.AwsConfiguration;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.apigateway.model.*;

import java.util.HashMap;

public class AmazonApiGateway {
    private static AmazonApiGateway instance = null;

    private com.amazonaws.services.apigateway.AmazonApiGateway amazonApiGateway;
    private String restApiId;
    private Resource rootResource;

    private boolean releaseNewDeploymentStage =  false;

    private AmazonApiGateway () {
        // Create ApiGateway Object
        amazonApiGateway = AmazonApiGatewayClientBuilder.standard()
                .withCredentials( new AWSStaticCredentialsProvider( AmazonWebService.getCredentials() ) )
                .withRegion( AwsConfiguration.AWS_REGION )
                .build();
        Logger.info( "Init ApiGateway Credentials" );
    }

    public static AmazonApiGateway getInstance(){
        if( instance == null ){
            instance = new AmazonApiGateway();
        }

        return instance;
    }

    /**
     * Get the rest api id
     * if no rest api exists, then create one
     * @return string with rest api id
     */
    public String getRestApiId (){
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

        return restApiId;
    }

    /**
     * create a rest-endpoint for calling it from outside of the amazon cloud
     * @param functionName the name of the function, that is now a part of the path
     * @param functionARN the arn of the function
     */
    public void createGatewayAPI ( String functionName, String functionARN ){
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

        releaseNewDeploymentStage = true;

        Logger.info( "API Gateway for Lambda Function '" + functionName + "' created" );
    }

    /**
     * Release new deployment stage if a new rest end point was added
     */
    public void releaseDeploymentStage(){
        if( releaseNewDeploymentStage ) {
            // Create new deployment stage
            CreateDeploymentRequest createDeploymentRequest = new CreateDeploymentRequest();
            createDeploymentRequest.setRestApiId(restApiId);
            createDeploymentRequest.setStageName(AwsConfiguration.AWS_API_GATEWAY_STAGE_NAME);
            amazonApiGateway.createDeployment(createDeploymentRequest);

            Logger.info("API Gateway: new deployment stage released");
        }
    }
}
