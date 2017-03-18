package ch.uzh.ifi.seal.jcs_lambda.management;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.AbstractResponse;
import ch.uzh.ifi.seal.jcs_lambda.utility.Util;
import ch.uzh.ifi.seal.jcs_lambda.utility.builder.FileBuilder;
import com.google.gson.Gson;

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

    // TODO
    private String url = "https://wcfhgyglqg.execute-api.eu-central-1.amazonaws.com/prod/TEST";
    private String checksum;

    private String temporaryPackageName;

    public CloudMethodEntity ( Method method ){
        packageName = method.getDeclaringClass().getPackage().getName();
        className = method.getDeclaringClass().getSimpleName();
        methodName = method.getName();
        parameters = Util.getMethodParameters( method );
        returnType = method.getReturnType().getSimpleName();

        fullQualifiedName = Util.getFullQualifiedName( packageName, className, methodName, parameters );

        temporaryPackageName = TEMPORARY_PACKAGE + "." + fullQualifiedName;
    }

    public void modifyCode (){
        // Create DTO classes
        FileBuilder.createRequestClass( temporaryPackageName, parameters );
        FileBuilder.createResponseClass( temporaryPackageName, returnType );

        FileBuilder.createLambdaHandler( this );
    }

    public String getFullQualifiedName (){
        return fullQualifiedName;
    }

    public String getPackageName (){
        return packageName;
    }

    public String getClassName (){
        return className;
    }

    public String getTemporaryPackageName (){
        return temporaryPackageName;
    }

    public String getReturnType (){
        return returnType;
    }

    public Object runMethodOnCloud( HashMap<String, Object> parameters ) {
        try{
            Class requestClass = Class.forName( temporaryPackageName + ".Request" );
            Field[] requestFields = requestClass.getDeclaredFields();
            Object requestInstance = requestClass.newInstance();

            for( Field field : requestFields ){
                String name = field.getName();
                Object value = parameters.get( name );

                field.set( requestInstance, value );
            }

           /* Class HelperClass = Class.forName( tmpPackage + ".Helper" );
            Object HelperInstance = HelperClass.newInstance();

            Class params[] = { String.class, Object.class };
            Object paramsObj[] = { url, requestInstance };
            Method HelperMethod = HelperClass.getDeclaredMethod("testing", params );

            return HelperMethod.invoke(HelperInstance, paramsObj);*/

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

    public String getArgumentVariableString (){
        String argumentVariableString = "";

        int i = 0;
        for(Map.Entry<String, Class> entry : parameters.entrySet() ){
            String parameterName = entry.getKey();

            if( i>0 ){
                argumentVariableString += ", ";
            }

            argumentVariableString += "request." + parameterName;
            i++;
        }

        return argumentVariableString;
    }

    public String getArgumentsWithTypeString (){
        String argumentsWithTypeString = "";

        int i = 0;
        for(Map.Entry<String, Class> entry : parameters.entrySet() ){
            String parameterName = entry.getKey();
            Class parameterType = entry.getValue();

            if( i>0 ){
                argumentsWithTypeString += ", ";
            }

            argumentsWithTypeString += parameterType + " " + parameterName;
            i++;
        }

        return argumentsWithTypeString;
    }
}
