package ch.uzh.ifi.seal.jcs_lambda.management;

import ch.uzh.ifi.seal.jcs_lambda.exception.IllegalDefinitionException;
import ch.uzh.ifi.seal.jcs_lambda.utility.AwsUtil;
import ch.uzh.ifi.seal.jcs_lambda.utility.Util;
import ch.uzh.ifi.seal.jcs_lambda.utility.builder.CodeModifier;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CloudMethodEntity {
    private String fullQualifiedName;

    private String packageName;
    private String className;
    private String methodName;
    private HashMap<String, Class> parameters;
    private String returnType;

    private String url;

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
        returnType = method.getReturnType().getName();

        fullQualifiedName = Util.getFullQualifiedName( packageName, className, methodName, parameters );

        temporaryPackageName = CodeModifier.TEMPORARY_PACKAGE + "." + fullQualifiedName;

        url = AwsUtil.getRestEndPointUrl( fullQualifiedName );

        if( returnType.equals("void" ) ){
            throw new IllegalDefinitionException( "Return type void isn't valid!");
        }
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

    public String getClassName () { return className; }

    public String getTemporaryPackageName (){
        return temporaryPackageName;
    }

    /**
     * create some trash classes for cloud deployment
     */
    public void modifyCode (){
        // Create DTO classes
        CodeModifier.createRequestClass( temporaryPackageName, parameters );
        CodeModifier.createResponseClass( temporaryPackageName, returnType );

        // Create Lambda Function Handler for AWS
        CodeModifier.createLambdaFunctionHandler( this );
    }

    /**
     * run method in cloud
     * @param parameters captured parameters from the innvocation
     * @return return response object from the cloud
     */
    public Object runMethodInCloud( Map<String, Object> parameters ) {
        try{
            // Create request dto
            Class requestClass = Class.forName( temporaryPackageName + ".Request" );
            Field[] requestFields = requestClass.getDeclaredFields();
            Object requestInstance = requestClass.newInstance();

            // add all parameters to the dto
            for( Field field : requestFields ){
                String name = field.getName();
                Object value = parameters.get( name );

                field.set( requestInstance, value );
            }

            Gson gson = new Gson();

            // handle request dto
            Class responseClass = Class.forName( temporaryPackageName + ".Response" );
            String returnJsonObject = Util.doRequest( url, gson.toJson( requestInstance ) );

            Object returnObj = gson.fromJson( returnJsonObject, responseClass );

            // cast returnObj and get the return value
            Field field = responseClass.getField("returnValue" );
            return field.get( responseClass.cast(returnObj) );
        }
        catch ( Exception e ){
            e.printStackTrace();
            throw new RuntimeException( "Unable to create request/response dto or to get or set the value" );
        }
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
            String parameterType = entry.getValue().getSimpleName();

            if( i>0 ){
                methodSignature += ", ";
            }

            methodSignature += parameterType + " " + parameterName;
            i++;
        }
        methodSignature += " )";

        return methodSignature;
    }
}