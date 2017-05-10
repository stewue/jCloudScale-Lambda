package ch.uzh.ifi.seal.jcs_lambda.annotations;

import ch.uzh.ifi.seal.jcs_lambda.configuration.AwsConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotations is for methods that are deployed to the cloud
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CloudMethod {
    int memory() default AwsConfiguration.AWS_DEFAULT_MEMORY_SIZE;
    int timeout() default AwsConfiguration.AWS_TIMEOUT;
}
