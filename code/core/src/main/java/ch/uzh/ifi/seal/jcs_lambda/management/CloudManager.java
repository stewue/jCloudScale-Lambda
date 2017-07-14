package ch.uzh.ifi.seal.jcs_lambda.management;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.lambdaFunction.AmazonApiGateway;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.lambdaFunction.AmazonLambda;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.lambdaFunction.AmazonSimpleStorageService;
import ch.uzh.ifi.seal.jcs_lambda.configuration.AwsConfiguration;
import ch.uzh.ifi.seal.jcs_lambda.exception.IllegalDefinitionException;
import ch.uzh.ifi.seal.jcs_lambda.utility.AwsUtil;
import ch.uzh.ifi.seal.jcs_lambda.utility.builder.CodeLastModified;
import ch.uzh.ifi.seal.jcs_lambda.utility.builder.CodeModifier;
import ch.uzh.ifi.seal.jcs_lambda.utility.builder.JarBuilder;
import com.amazonaws.services.lambda.model.FunctionCode;
import org.aspectj.apache.bcel.classfile.Code;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CloudManager {
    private static CloudManager instance = null;

    private boolean deployToCloud;

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

    public void setDeployToCloud( boolean deployToCloud ){
        this.deployToCloud = deployToCloud;
    }

    /**
     * build a jar file with all dependencies and create/update all necessary functions in the cloud
     */
    public void buildAndUpload (){

        AmazonLambda awsCloudProvider = AmazonLambda.getInstance();

        // check if code is modified
        boolean updateNecessary = CodeLastModified.isModified();

        // if code isn't modified, check if each function in the cloud exists
        if( !updateNecessary ){
            for( Map.Entry<String, CloudMethodEntity> entry : cloudMethods.entrySet() ) {
                CloudMethodEntity method = entry.getValue();

                // check if function already exists
                if( !awsCloudProvider.existsFunction( method.getFullQualifiedName() ) ){
                    updateNecessary = true;
                }
            }
        }

        // check if a function is new or code is modified
        if( deployToCloud && updateNecessary ){
            // build jar file with maven
            JarBuilder.mvnBuild();
            CodeModifier.compileFilesAfterMaven();

            // get jar file
            File file = new File( "target/jcs_lambda-tests.jar" );

            // upload jar file to amazon s3
            AmazonSimpleStorageService amazonS3 = AmazonSimpleStorageService.getInstance();
            FunctionCode functionCode = amazonS3.uploadFile( file );

            // update each function and set new description and code ("link" to file in amazon s3)
            for( Map.Entry<String, CloudMethodEntity> entry : cloudMethods.entrySet() ) {
                CloudMethodEntity method = entry.getValue();

                String functionNameWithPrefix = AwsConfiguration.AWS_FUNCTION_PREFIX + AwsUtil.convertMethodName( method.getFullQualifiedName() );
                String handlerName = method.getTemporaryPackageName() + ".Endpoint::handleRequest";
                FunctionDescription functionDescription = new FunctionDescription( method.getFullQualifiedName() );

                if( handlerName.length() > 120 ){
                    throw new IllegalDefinitionException( "Handlername to long" );
                }
                awsCloudProvider.createOrUpdateFunction( functionNameWithPrefix, handlerName, functionCode, functionDescription, method.getMemory(), method.getTimeout() );
            }

            // Release new deployment stage
            AmazonApiGateway.getInstance().releaseDeploymentStage();

            // Remove buckets
            amazonS3.removeAllTemporaryCreatedBuckets();
        }
    }
}