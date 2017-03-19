package ch.uzh.ifi.seal.jcs_lambda.utility;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.AwsCloudProvider;

public class AwsUtil {
    public static String convertMethodName ( String methodName ){
        return methodName.replace(".", "--");
    }

    public static String getRestEndPointUrl ( String methodName ){
        AwsCloudProvider awsCloudProvider = AwsCloudProvider.getInstance();

        String baseUrl = awsCloudProvider.getBaseUrl();
        String pathName = convertMethodName( methodName );

        return baseUrl + pathName;
    }
}
