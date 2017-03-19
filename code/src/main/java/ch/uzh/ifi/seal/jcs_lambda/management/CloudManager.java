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

    public static CloudManager getInstance(){
        if( instance == null ){
            instance = new CloudManager();
        }

        return instance;
    }

    public void registerMethod( CloudMethodEntity methodEntity ){
        cloudMethods.put( methodEntity.getFullQualifiedName(), methodEntity );
    }

    public CloudMethodEntity getMethodByName ( String fullQualifiedName ){
        return cloudMethods.get( fullQualifiedName );
    }

    public void buildAndUpload (){

        AwsCloudProvider awsCloudProvider = AwsCloudProvider.getInstance();
        // ToDo better name
        HashMap<String, CloudMethodEntity> needUpdate = new HashMap<>();

        // Check if a function was updated or is new
        for( Map.Entry<String, CloudMethodEntity> entry : cloudMethods.entrySet() ) {
            CloudMethodEntity method = entry.getValue();

            method.setUrl( AwsUtil.getRestEndPointUrl( method.getFullQualifiedName() ) );

            // get current description for cloud
            MethodDescription methodDescription = awsCloudProvider.getLambdaFunctionDescription( method.getFullQualifiedName() );

            // new function, because no description exists
            if( methodDescription == null ){
                needUpdate.put( method.getFullQualifiedName(), method );
            }
            // need update, because checksum isn't the same
            else if ( !methodDescription.getChecksum().equals( method.getChecksum() ) ){
                needUpdate.put( method.getFullQualifiedName(), method );
            }
        }

        if( !needUpdate.isEmpty() ){
            JarBuilder.mvnBuild();
            File file = new File( "target/jcs_lambda-jar-with-dependencies.jar" );

            FunctionCode functionCode = awsCloudProvider.uploadFile( file );

            for( Map.Entry<String, CloudMethodEntity> entry : needUpdate.entrySet() ) {
                CloudMethodEntity method = entry.getValue();

                String functionName = AwsUtil.convertMethodName( method.getFullQualifiedName() );
                String handlerName = method.getTemporaryPackageName() + ".LambdaFunctionHandler::handleRequest";
                MethodDescription methodDescription = new MethodDescription( method.getChecksum() );

                awsCloudProvider.createOrUpdateFunction( functionName, handlerName, functionCode, methodDescription );
            }

            // Remove buckets
            awsCloudProvider.removeAllTemporaryCreatedBuckets();
        }
    }
}
