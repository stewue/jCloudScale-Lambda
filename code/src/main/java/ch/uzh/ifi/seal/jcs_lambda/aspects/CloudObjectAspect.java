package ch.uzh.ifi.seal.jcs_lambda.aspects;

import ch.uzh.ifi.seal.jcs_lambda.utility.JarBuilder;
import ch.uzh.ifi.seal.jcs_lambda.utility.UtilFileLoader;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;

@Aspect
public class CloudObjectAspect {
    @Around("@annotation(CloudMethod) && execution(* *(..))")
    public Object createCloudMethod (ProceedingJoinPoint joinPoint) throws Throwable {
        //Default Object that we can use to return to the consumer
        Object returnObject;

        System.out.println("asd");

        UtilFileLoader.createTmpFiles( joinPoint );
        try {
            //We choose to continue the call to the method in question
            returnObject = joinPoint.proceed();
            //If no exception is thrown we should land here and we can modify the returnObject, if we want to.
        } catch (Throwable throwable) {
            //Here we can catch and modify any exceptions that are called
            //We could potentially not throw the exception to the caller and instead return "null" or a default object.
            throw throwable;
        }
        finally {
            //If we want to be sure that some of our code is executed even if we get an exception
        }

        JarBuilder.mvnBuild();

        return returnObject;
    }
}
