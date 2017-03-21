package ch.uzh.ifi.seal.jcs_lambda.utility;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.AwsCloudProvider;

public class AwsUtil {
    /**
     * convert method name in a aws compatible name
     * @param methodName full qualified method name
     * @return aws compatible function name
     */
    public static String convertMethodName ( String methodName ){
        return methodName.replace(".", "--");
    }

    /**
     * map method to a rest end-point
     * @param methodName full qualified name of the method
     * @return url to the rest end-point
     */
    public static String getRestEndPointUrl ( String methodName ){
        AwsCloudProvider awsCloudProvider = AwsCloudProvider.getInstance();

        String baseUrl = awsCloudProvider.getBaseUrl();
        String pathName = convertMethodName( methodName );

        return baseUrl + pathName;
    }
}
