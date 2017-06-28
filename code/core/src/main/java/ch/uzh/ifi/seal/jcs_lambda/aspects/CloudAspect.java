package ch.uzh.ifi.seal.jcs_lambda.aspects;

import ch.uzh.ifi.seal.jcs_lambda.annotations.ByReference;
import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.JVMContext;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.ByReferenceHandler;
import ch.uzh.ifi.seal.jcs_lambda.exception.MissingStartUpException;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import ch.uzh.ifi.seal.jcs_lambda.management.CloudManager;
import ch.uzh.ifi.seal.jcs_lambda.management.CloudMethodEntity;
import ch.uzh.ifi.seal.jcs_lambda.utility.AspectUtil;
import ch.uzh.ifi.seal.jcs_lambda.utility.builder.CodeLastModified;
import ch.uzh.ifi.seal.jcs_lambda.utility.builder.CodeModifier;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

@Aspect
public class CloudAspect {

    private static CloudManager cloudManager = null;

    // only pseudo variable, that import optimizer won't remove the startup and ByReference annotation
    private static Class [] annotation = new Class[]{ StartUp.class, ByReference.class };

    /**
     * On Startup register all methods with cloud annotation
     * @param joinPoint current point of execution
     * @throws Throwable throw all errors
     */
    @Before("@annotation(StartUp) && execution(* *(..))")
    public void startUpMethod ( JoinPoint joinPoint ) throws Throwable {

        Logger.info( "@startUp" );
        long startTimestamp = System.currentTimeMillis();

        cloudManager = CloudManager.getInstance();

        cloudManager.setDeployToCloud( AspectUtil.getStartUpAnnotation( joinPoint ) );

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
        // In Cloud run "normal" code
        if( JVMContext.getContext() == true){
            return joinPoint.proceed();
        }
        // Local call proxy
        else {
            // application need a start up point with a annotation
            if( cloudManager == null ){
                throw new MissingStartUpException();
            }

            // get context from invocation
            Object context = joinPoint.getThis();

            String fullQualifiedName = AspectUtil.getFullQualifiedName(joinPoint);
            CloudMethodEntity methodEntity = cloudManager.getMethodByName(fullQualifiedName);

            Map<String, Object> parametersWithValues = AspectUtil.getParametersWithValue( joinPoint, methodEntity );
            Map<String, Object> classVariablesWithValue = AspectUtil.getClassVariablesValues( joinPoint );

            return methodEntity.runMethodInCloud( context, parametersWithValues, classVariablesWithValue );
        }
    }

    /**
     * Invoke all gets from a ByReference field
     * @param joinPoint current point of execution
     * @return return the local value of the variable
     * @throws Throwable throw all errors
     */
    @Around("get( !final !transient * * ) && @annotation(ByReference)")
    public Object getValueFromClient( ProceedingJoinPoint joinPoint ) throws Throwable {
        // in cloud get value from local application
        if( JVMContext.getContext() ){
            ByReferenceHandler referenceHandler = ByReferenceHandler.getInstance();
            Object returnValue = referenceHandler.getVariableAspect( joinPoint );

            Object context = joinPoint.getThis();
            String variableName = joinPoint.getSignature().getName();

            // set in cloud object
            Field field = context.getClass().getDeclaredField( variableName );
            field.setAccessible(true);
            field.set( context, returnValue );
        }

        return joinPoint.proceed();
    }

    /**
     * Invoke all sets from a ByReference field
     * @param joinPoint current point of execution
     * @throws Throwable throw all errors
     */
    @Around("set( !final !transient * * ) && @annotation(ByReference)")
    public void setValueToClient( ProceedingJoinPoint joinPoint ) throws Throwable {
        joinPoint.proceed();

        // in cloud get value from local application
        if( JVMContext.getContext() ){
            ByReferenceHandler referenceHandler = ByReferenceHandler.getInstance();
            referenceHandler.setVariableAspect( joinPoint );
        }
    }
}