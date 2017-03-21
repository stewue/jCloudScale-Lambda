package ch.uzh.ifi.seal.jcs_lambda.management;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.AbstractResponse;
import ch.uzh.ifi.seal.jcs_lambda.utility.Util;
import ch.uzh.ifi.seal.jcs_lambda.utility.builder.CodeModifier;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CloudMethodEntity {
    private static final String TEMPORARY_PACKAGE = "tmp_jcs";

    private String fullQualifiedName;

    private String packageName;
    private String className;
    private String methodName;
    private HashMap<String, Class> parameters;
    private String returnType;

    private String url;
    private String checksum;

    private String temporaryPackageName;

    /**
     * Init a new cloud entity
     * @param method current method
     */
    public CloudMethodEntity ( Method method ){
        packageName = method.getDeclaringClass().getPackage().getName();
        className = method.getDeclaringClass().getSimpleName();
        methodName = method.getName();
        parameters = Util.getMethodParameters( method );
        returnType = method.getReturnType().getSimpleName();

        fullQualifiedName = Util.getFullQualifiedName( packageName, className, methodName, parameters );

        temporaryPackageName = TEMPORARY_PACKAGE + "." + fullQualifiedName;

        calculateChecksum();
    }

    public String getFullQualifiedName (){
        return fullQualifiedName;
    }

    public String getPackageName (){
        return packageName;
    }

    public String getMethodName (){
        return methodName;
    }

    public String getTemporaryPackageName (){
        return temporaryPackageName;
    }

    public void setUrl ( String url ){
        this.url = url;
    }

    public String getChecksum (){
        return checksum;
    }

    /**
     * create some trash classes for cloud deployment
     */
    public void modifyCode (){
        // Create DTO classes
        CodeModifier.createRequestClass( temporaryPackageName, parameters );
        CodeModifier.createResponseClass( temporaryPackageName, returnType );

        // Create Lambda Handler for AWS
        CodeModifier.createLambdaHandler( this );
    }

    /**
     * run method in cloud
     * @param parameters captured parameters from the innvocation
     * @return return response object from the cloud
     */
    public Object runMethodInCloud(HashMap<String, Object> parameters ) {
        try{
            Class requestClass = Class.forName( temporaryPackageName + ".Request" );
            Field[] requestFields = requestClass.getDeclaredFields();
            Object requestInstance = requestClass.newInstance();

            for( Field field : requestFields ){
                String name = field.getName();
                Object value = parameters.get( name );

                field.set( requestInstance, value );
            }

            Gson gson = new Gson();

            Class responseClass = Class.forName( temporaryPackageName + ".Response" );
            String returnJsonObject = Util.doRequest( url, gson.toJson( requestInstance ) );
            AbstractResponse returnObj = (AbstractResponse) gson.fromJson( returnJsonObject, responseClass );

            return returnObj.returnValue;
        }
        catch ( Exception e ){
            //TODO
        }

        return null;
    }

    /**
     * get method signature
     * @return return method signature ( method name and parameters) as string
     */
    public String getMethodSignature (){
        String methodSignature = methodName + " ( ";

        int i = 0;
        for(Map.Entry<String, Class> entry : parameters.entrySet() ){
            String parameterName = entry.getKey();
            Class parameterType = entry.getValue();

            if( i>0 ){
                methodSignature += ", ";
            }

            methodSignature += parameterType + " " + parameterName;
            i++;
        }
        methodSignature += " )";

        return methodSignature;
    }

    /**
     * calculate the checksum hash from the method
     */
    private void calculateChecksum(){
        String methodSignature = getMethodSignature();

        String methodBody = CodeModifier.getMethodBody( methodSignature, className, packageName );

        if( methodBody == null ){
            checksum = null;
        }
        else{
            String sourceCode = methodSignature + " { " + methodBody + " }";

            checksum = DigestUtils.sha256Hex( sourceCode );
        }
    }
}
