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
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CloudMethodEntity {
    private String fullQualifiedName;

    private String packageName;
    private String className;
    private String methodName;
    private HashMap<String, Type> parameters;
    private String returnType;
    private boolean isReturnTypeVoid;

    private Map<String, Type> classVariablesReadOnly;
    private boolean isParameterNamePresent;
    private Map<String, Type> classVariablesByReference;

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
        parameters = ReflectionUtil.getMethodParametersWithGenerics( method );
        returnType = method.getReturnType().getName();

        classVariablesReadOnly = ReflectionUtil.getClassVariablesReadOnly( method );
        isParameterNamePresent = ReflectionUtil.isMethodParameterNamePresent( method );
        classVariablesByReference = ReflectionUtil.getClassVariablesByReference( method );

        fullQualifiedName = Util.getFullQualifiedName( packageName, className, methodName,  ReflectionUtil.getMethodParameters( method ) );

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

    public Map<String, Type> getClassVariablesReadOnly(){
        return classVariablesReadOnly;
    }

    public Map<String, Type> getClassVariablesByReference(){
        return classVariablesByReference;
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
        CodeModifier.createRequestClass( temporaryPackageName, parameters, classVariablesReadOnly);
        CodeModifier.createResponseClass( temporaryPackageName, returnType );

        // Create Lambda Function Handler for AWS
        CodeModifier.createLambdaFunctionHandler( this );
    }

    /**
     * run method in cloud
     * @param context object with current context
     * @param parameters captured parameters from the innvocation
     * @param classVariablesReadOnly hash-map with all class variables, that aren't local
     * @return return response object from the cloud
     * @throws Exception throw all exceptions to the aspect
     */
    // TODO REFACROTING
    public Object runMethodInCloud( Object context, Map<String, Object> parameters,  Map<String, Object> classVariablesReadOnly ) throws Exception {

        boolean hasAReferenceVariable = ByReferenceUtil.checkIfClassHasAReferenceVariable( context );

        JcsMessageQueue messageQueue = null;

        if( hasAReferenceVariable ){
            String uuid = ByReferenceUtil.getUUID( context );

            messageQueue = JcsMessageQueue.getInstance();
            messageQueue.registerObject( uuid, context );
            messageQueue.increasePendingRequests();
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

                // if its a uuid
                if( name.equals("_uuid_") ){
                    value = ByReferenceUtil.getUUID( context );
                }
                // check if its a parameter
                else if( parameters.get( name ) != null ){
                    value = parameters.get( name );
                }
                // else it is a class variable
                else {
                    value = classVariablesReadOnly.get( name );
                }

                field.set( requestInstance, value );
            }
        }
        catch ( Exception e ){
            e.printStackTrace();
            throw new RuntimeException( "Unable to create request dto or to set the value" );
        }


        Gson gson = new Gson();

        // handle request dto
        Class responseClass = null;
        try {
            responseClass = Class.forName(temporaryPackageName + ".Response");
        }
        catch ( Exception e ){
            e.printStackTrace();
            throw new RuntimeException( "Unable to create response dto" );
        }

        Logger.debug( "Send request to " + url );
        String returnJsonObject = Util.doRequest(url, gson.toJson(requestInstance));
        Object returnObj = gson.fromJson( returnJsonObject, responseClass );

        // check if exception occurred in cloud
        Field fieldException = responseClass.getDeclaredField("exceptionStackTrace" );
        StackTraceElement [] stackTraceElements = (StackTraceElement []) fieldException.get( responseClass.cast(returnObj) );
        if( stackTraceElements != null ){
            throw new CloudRuntimeException( "Something failed in the cloud on runtime!", stackTraceElements );
        }

        if( hasAReferenceVariable ){
            messageQueue.decreasePendingRequests();
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