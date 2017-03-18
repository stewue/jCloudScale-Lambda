package ch.uzh.ifi.seal.jcs_lambda.management;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.AwsCloudProvider;
import ch.uzh.ifi.seal.jcs_lambda.utility.builder.JarBuilder;

import java.io.File;
import java.lang.reflect.Method;
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
        JarBuilder.mvnBuild();

        File file = new File( "target/jcs_lambda-jar-with-dependencies.jar" );
        AwsCloudProvider awsCloudProvider = new AwsCloudProvider();

        for( Map.Entry<String, CloudMethodEntity> entry : cloudMethods.entrySet() ){
            CloudMethodEntity method = entry.getValue();

            String functionName = method.getFullQualifiedName().replace(".", "--");
            String handlerName = method.getTemporaryPackageName() + ".LambdaFunctionHandler::handleRequest";

            method.setUrl( awsCloudProvider.registerMethod( functionName, handlerName, file ) );
        }
        //TODO Upload
    }
}
