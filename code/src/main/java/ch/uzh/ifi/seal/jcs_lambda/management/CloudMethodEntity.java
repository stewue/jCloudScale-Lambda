package ch.uzh.ifi.seal.jcs_lambda.management;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.JcsMessageQueue;
import ch.uzh.ifi.seal.jcs_lambda.exception.CloudRuntimeException;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import ch.uzh.ifi.seal.jcs_lambda.utility.AwsUtil;
import ch.uzh.ifi.seal.jcs_lambda.utility.ByReferenceUtil;
import ch.uzh.ifi.seal.jcs_lambda.utility.ReflectionUtil;
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
    private boolean isReturnTypeVoid;

    private Map<String, Class> classVariables;
    private boolean isParameterNamePresent;

    private String url;

    private int memory;
    private int timeout;

    private String temporaryPackageName;

    /**
     * Init a new cloud entity
     * @param method current method
     */
    public CloudMethodEntity ( Method method ){
        packageName = method.getDeclaringClass().getPackage().getName();
        className = method.getDeclaringClass().getSimpleName();
        methodName = method.getName();
        parameters = ReflectionUtil.getMethodParameters( method );
        returnType = method.getReturnType().getName();

        classVariables = ReflectionUtil.getClassVariables( method );
        isParameterNamePresent = ReflectionUtil.isMethodParameterNamePresent( method );

        fullQualifiedName = Util.getFullQualifiedName( packageName, className, methodName, parameters );

        temporaryPackageName = CodeModifier.TEMPORARY_PACKAGE + "." + fullQualifiedName;

        url = AwsUtil.getRestEndPointUrl( fullQualifiedName );

        // get value from method annotation
        CloudMethod annotation = method.getAnnotation( CloudMethod.class );
        memory = AwsUtil.returnValidMemory( annotation.memory() );
        timeout = AwsUtil.returnValidTimeout( annotation.timeout() );

        if( returnType.equals("void" ) ){
            isReturnTypeVoid = true;
            returnType = "String";
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

    public String getClassName () {
        return className;
    }

    public String getTemporaryPackageName (){
        return temporaryPackageName;
    }

    public Map<String, Class> getClassVariables (){
        return classVariables;
    }

    public int getMemory(){
        return memory;
    }

    public int getTimeout(){
        return timeout;
    }

    public boolean isParameterNamePresent(){
        return isParameterNamePresent;
    }

    public boolean isReturnTypeVoid(){
        return isReturnTypeVoid;
    }
    /**
     * create some trash classes for cloud deployment
     */
    public void modifyCode (){
        // Create DTO classes
        CodeModifier.createRequestClass( temporaryPackageName, parameters, classVariables );
        CodeModifier.createResponseClass( temporaryPackageName, returnType );

        // Create Lambda Function Handler for AWS
        CodeModifier.createLambdaFunctionHandler( this );
    }

    /**
     * run method in cloud
     * @param parameters captured parameters from the innvocation
     * @return return response object from the cloud
     */
    // TODO REFACROTING
    public Object runMethodInCloud( Object context, Map<String, Object> parameters,  Map<String, Object> classVariables ) throws Exception {

        boolean hasAReferenceVariable = ByReferenceUtil.checkIfClassHasAReferenceVariable( context.getClass() );

        JcsMessageQueue messageQueue = null;

        if( hasAReferenceVariable ){
            String uuid = ByReferenceUtil.getUUID( context );

            messageQueue = JcsMessageQueue.getInstance();
            messageQueue.registerObject( uuid, context );
            messageQueue.increasePendingCloudCalculation();
            messageQueue.startAsyncReceiving();
        }

        // Create request dto
        Object requestInstance = null;
        try{
            Class requestClass = Class.forName( temporaryPackageName + ".Request" );
            Field[] requestFields = requestClass.getDeclaredFields();
            requestInstance = requestClass.newInstance();

            // add all parameter and class variable values to the dto
            for( Field field : requestFields ){
                String name = field.getName();
                Object value;

                // check if its a parameter
                if( parameters.get( name ) != null ){
                    value = parameters.get( name );
                }
                // else it is a class variable
                else {
                    value = classVariables.get( name );
                }

                field.set( requestInstance, value );
            }
        }
        catch ( Exception e ){
            throw new RuntimeException( "Unable to create request dto or to set the value" );
        }


        Gson gson = new Gson();

        // handle request dto
        Class responseClass = null;
        String returnJsonObject = "";
        try {
            responseClass = Class.forName(temporaryPackageName + ".Response");
            Logger.debug( "Send request to " + url );
            returnJsonObject = Util.doRequest(url, gson.toJson(requestInstance));
        }
        catch ( Exception e ){
            throw new RuntimeException( "Unable to create response dto or to set/get the value" );
        }

        Object returnObj = gson.fromJson( returnJsonObject, responseClass );

        // check if exception occurred in cloud
        Field fieldException = responseClass.getDeclaredField("exceptionStackTrace" );
        StackTraceElement [] stackTraceElements = (StackTraceElement []) fieldException.get( responseClass.cast(returnObj) );
        if( stackTraceElements != null ){
            throw new CloudRuntimeException( "Something failed in the cloud on runtime!", stackTraceElements );
        }

        if( hasAReferenceVariable ){
            messageQueue.decreasePendingCloudCalculation();
        }

        Logger.debug( "Get response from " + url );
        if( isReturnTypeVoid ){
            return null;
        }
        else{
            // cast returnObj and get the return value
            Field fieldReturnValue = responseClass.getDeclaredField("returnValue" );
            return fieldReturnValue.get( responseClass.cast(returnObj) );
        }
    }
}