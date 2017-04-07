package ch.uzh.ifi.seal.jcs_lambda.aspects;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.JVMContext;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import ch.uzh.ifi.seal.jcs_lambda.management.CloudManager;
import ch.uzh.ifi.seal.jcs_lambda.management.CloudMethodEntity;
import ch.uzh.ifi.seal.jcs_lambda.utility.Util;
import ch.uzh.ifi.seal.jcs_lambda.utility.builder.CodeLastModified;
import ch.uzh.ifi.seal.jcs_lambda.utility.builder.CodeModifier;
import org.aspectj.apache.bcel.classfile.Code;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

@Aspect
public class CloudAspect {

    private static CloudManager cloudManager = null;

    // only pseudo variable, that import optimizer won't remove the startup annotation
    private static Class clazz = StartUp.class;

    /**
     * On Startup register all methods with cloud annotation
     * @param joinPoint current point of execution
     * @throws Throwable throw all errors
     */
    @Before("@annotation(StartUp) && execution(* *(..))")
    public void startUpMethod ( JoinPoint joinPoint ) throws Throwable {

        Logger.info( "@startUp*" );
        long startTimestamp = System.currentTimeMillis();

        cloudManager = CloudManager.getInstance();

        // Get all method with @CloudMethod annotation
        ConfigurationBuilder reflectionConfig = new ConfigurationBuilder()
                .setUrls( ClasspathHelper.forPackage("") )
                .setScanners( new MethodAnnotationsScanner());
        Reflections reflections = new Reflections( reflectionConfig );
        Set<Method> methods = reflections.getMethodsAnnotatedWith( CloudMethod.class );

        // Register and modify all methods with annotation
        for( Method currentMethod : methods ){
            CloudMethodEntity methodEntity = new CloudMethodEntity( currentMethod );

            cloudManager.registerMethod( methodEntity );

            methodEntity.modifyCode();
        }

        cloudManager.buildAndUpload();

        CodeModifier.removeTemporaryClasses();

        CodeLastModified.updateLastModified();

        // Calculate init time
        long endTimestamp = System.currentTimeMillis();
        long different = endTimestamp - startTimestamp;

        Logger.info( "Time needed for initialization: " + ( different / 1000.0 ) + " sec" );
    }

    /**
     * Call CloudManager to get rest-endpoint, run method in cloud and handle return value
     * @param joinPoint current point of execution
     * @return return the return-object of the injected method
     * @throws Throwable throw all errors
     */
    @Around("@annotation(CloudMethod) && execution(* *(..))")
    public Object runMethodInCloud ( ProceedingJoinPoint joinPoint ) throws Throwable {
            if( JVMContext.getContext() == true){
                return joinPoint.proceed();
            }else {
                String fullQualifiedName = getFullQualifiedName(joinPoint);
                CloudMethodEntity methodEntity = cloudManager.getMethodByName(fullQualifiedName);

                HashMap<String, Object> parametersWithValues = getParametersWithValue(joinPoint);
                return methodEntity.runMethodInCloud(parametersWithValues);
            }
    }

    /**
     * convert jointPoint of the method into the full qualified name
     * @param joinPoint current point of execution
     * @return the full qualified name of the method
     */
    private static String getFullQualifiedName ( ProceedingJoinPoint joinPoint ){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String packageName = signature.getMethod().getDeclaringClass().getPackage().getName();
        String className = signature.getMethod().getDeclaringClass().getSimpleName();
        String methodName = signature.getMethod().getName();

        Class[] parameterTypes = signature.getParameterTypes();
        String [] parameterNames = signature.getParameterNames();
        HashMap<String, Class> parameters = new HashMap<>();

        for( int i=0; i<parameterTypes.length; i++ ){
            Class parameterType = parameterTypes[i];
            String parameterName = parameterNames[i];

            parameters.put( parameterName, parameterType );
        }

        return Util.getFullQualifiedName( packageName, className, methodName, parameters );
    }

    /**
     * get all parameters (value + name) of the method
     * @param joinPoint current point of execution
     * @return return a map with the values and parameter names of the injected method
     */
    private static HashMap<String, Object> getParametersWithValue( ProceedingJoinPoint joinPoint ){
        HashMap<String, Object> parameters = new HashMap<>();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String [] parameterNames = signature.getParameterNames();
        Object [] arguments = joinPoint.getArgs();

        for( int i=0; i<arguments.length; i++ ){
            Object parameterValue = arguments[i];
            String parameterName =  parameterNames[i];

            parameters.put( parameterName, parameterValue );
        }

        return parameters;
    }
}