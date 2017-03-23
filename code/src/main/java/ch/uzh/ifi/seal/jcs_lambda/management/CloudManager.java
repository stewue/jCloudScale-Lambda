package ch.uzh.ifi.seal.jcs_lambda.management;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.AwsCloudProvider;
import ch.uzh.ifi.seal.jcs_lambda.utility.AwsUtil;
import ch.uzh.ifi.seal.jcs_lambda.utility.builder.JarBuilder;
import com.amazonaws.services.lambda.model.FunctionCode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CloudManager {
    private static CloudManager instance = null;

    private HashMap<String, CloudMethodEntity> cloudMethods = new HashMap<>();

    private CloudManager (){

    }

    /**
     * get the cloud manager instance (singleton)
     * @return cloud manager instance
     */
    public static CloudManager getInstance(){
        if( instance == null ){
            instance = new CloudManager();
        }

        return instance;
    }

    /**
     * add the method to the list of registered method
     * @param methodEntity current method entity
     */
    public void registerMethod( CloudMethodEntity methodEntity ){
        cloudMethods.put( methodEntity.getFullQualifiedName(), methodEntity );
    }

    /**
     * get method by full qualified name
     * @param fullQualifiedName full qualified name
     * @return cloud method entity object
     */
    public CloudMethodEntity getMethodByName ( String fullQualifiedName ){
        return cloudMethods.get( fullQualifiedName );
    }

    /**
     * build a jar file with all dependencies and create/update all necessary functions in the cloud
     */
    public void buildAndUpload (){

        AwsCloudProvider awsCloudProvider = AwsCloudProvider.getInstance();

        HashMap<String, CloudMethodEntity> functionNeedUpdate = new HashMap<>();

        // Check if a function was updated or is new
        for( Map.Entry<String, CloudMethodEntity> entry : cloudMethods.entrySet() ) {
            CloudMethodEntity method = entry.getValue();

            method.setUrl( AwsUtil.getRestEndPointUrl( method.getFullQualifiedName() ) );

            // get current description from cloud
            FunctionDescription functionDescription = awsCloudProvider.getLambdaFunctionDescription( method.getFullQualifiedName() );

            // it's a new function, because no description exists
            if( functionDescription == null ){
                functionNeedUpdate.put( method.getFullQualifiedName(), method );
            }
            // need update, because checksum isn't the same
            else if ( method.getChecksum() == null || !functionDescription.getChecksum().equals( method.getChecksum() ) ){
                functionNeedUpdate.put( method.getFullQualifiedName(), method );
            }
            else {
                // no update necessary, because checksum is the same
            }
        }

        // TODO true only temporary
        // if at least one function need an update
        if( !functionNeedUpdate.isEmpty() ){
            // build jar file with maven
            JarBuilder.mvnBuild();
            // get jar file
            File file = new File( "target/jcs_lambda-jar-with-dependencies.jar" );

            // upload jar file to amazon s3
            FunctionCode functionCode = awsCloudProvider.uploadFile( file );

            // update each function and set new description and code ("link" to file in amazon s3)
            for( Map.Entry<String, CloudMethodEntity> entry : functionNeedUpdate.entrySet() ) {
                CloudMethodEntity method = entry.getValue();

                String functionName = AwsUtil.convertMethodName( method.getFullQualifiedName() );
                String handlerName = method.getTemporaryPackageName() + ".LambdaFunctionHandler::handleRequest";
                FunctionDescription functionDescription = new FunctionDescription( method.getChecksum() );

                awsCloudProvider.createOrUpdateFunction( functionName, handlerName, functionCode, functionDescription);
            }

            // Remove buckets
            awsCloudProvider.removeAllTemporaryCreatedBuckets();
        }
    }
}
