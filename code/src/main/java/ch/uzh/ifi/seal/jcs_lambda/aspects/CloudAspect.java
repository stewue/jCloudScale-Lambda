package ch.uzh.ifi.seal.jcs_lambda.aspects;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import ch.uzh.ifi.seal.jcs_lambda.management.CloudManager;
import ch.uzh.ifi.seal.jcs_lambda.management.CloudMethodEntity;
import ch.uzh.ifi.seal.jcs_lambda.utility.Util;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
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

    private static long startTimestamp;

    // only pseudo variable, that import optimizer won't remove the startup annotation
    private static Class clazz = StartUp.class;

    /**
     * On Startup register all methods with cloud annotation
     * @param joinPoint
     * @throws Throwable
     */
    @Before("@annotation(StartUp) && execution(* *(..))")
    public void startUpMethod ( JoinPoint joinPoint ) throws Throwable {

        Logger.info( "@startUp" );
        startTimestamp = System.currentTimeMillis();

        cloudManager = CloudManager.getInstance();

        // Get all method with @CloudMethod annotation
        ConfigurationBuilder reflectionConfig = new ConfigurationBuilder()
                .setUrls( ClasspathHelper.forPackage("") )
                .setScanners( new MethodAnnotationsScanner());
        Reflections reflections = new Reflections( reflectionConfig );
        Set<Method> methods = reflections.getMethodsAnnotatedWith( CloudMethod.class );

        // Register and ............ all methods with annotation
        for( Method currentMethod : methods ){
            CloudMethodEntity methodEntity = new CloudMethodEntity( currentMethod );
            cloudManager.registerMethod( methodEntity );

            methodEntity.modifyCode();
        }

        cloudManager.buildAndUpload();
    }

    @After("@annotation(StartUp) && execution(* *(..))")
    public void beforeExit ( JoinPoint joinPoint ) throws Throwable {
        long endTimestamp = System.currentTimeMillis();
        long different = endTimestamp - startTimestamp;

        Logger.info( "Time needed: " + ( different / 1000.0 ) + " sec" );
    }

    @Around("@annotation(CloudMethod) && execution(* *(..))")
    public Object runMethodInCloud ( ProceedingJoinPoint joinPoint ) throws Throwable {

        String fullQualifiedName = getFullQualifiedName( joinPoint );
        CloudMethodEntity methodEntity = cloudManager.getMethodByName( fullQualifiedName );

        HashMap<String, Object> parametersWithValues = getParametersWithValue( joinPoint );
        return methodEntity.runMethodInCloud( parametersWithValues );
    }

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
