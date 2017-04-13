package ch.uzh.ifi.seal.jcs_lambda.utility;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.AwsCloudProvider;
import ch.uzh.ifi.seal.jcs_lambda.configuration.AwsConfiguration;
import org.apache.commons.codec.digest.DigestUtils;

public class AwsUtil {
    /**
     * convert method name in a hash name
     * @param methodName full qualified method name
     * @return aws compatible function name
     */
    public static String convertMethodName ( String methodName ){
        return DigestUtils.sha1Hex( methodName );
    }

    /**
     * map method to a rest end-point
     * @param methodName full qualified name of the method
     * @return url to the rest end-point
     */
    public static String getRestEndPointUrl ( String methodName ){
        AwsCloudProvider awsCloudProvider = AwsCloudProvider.getInstance();

        String baseUrl = awsCloudProvider.getBaseUrl();
        String pathName = AwsConfiguration.AWS_FUNCTION_PREFIX + convertMethodName( methodName );

        return baseUrl + pathName;
    }
}
