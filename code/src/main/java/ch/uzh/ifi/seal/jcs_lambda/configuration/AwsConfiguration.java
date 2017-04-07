package ch.uzh.ifi.seal.jcs_lambda.configuration;

import com.amazonaws.regions.Regions;

public class AwsConfiguration {
    public static final String AWS_ACCESS_KEY_ID = "AKIAJ36KVJTVBYMCIZNA";
    public static final String AWS_SECRET_KEY_ID = "/IgxG+SS8LEBwO7iqS2w5/+iB3kmbmElUsUj/9zg";
    public static final Regions AWS_REGION = Regions.EU_CENTRAL_1;
    public static final int AWS_TIMEOUT = 3;
    public static final int AWS_DEFAULT_MEMORY_SIZE = 128;

    public static final String AWS_ROLE_NAME = "JCS-Lambda";
    public static final String AWS_BUCKET_PREFIX = "jcs-lambda";
    public static final String AWS_API_GATEWAY_NAME = "JCS Lambda REST API";
    public static final String AWS_API_GATEWAY_STAGE_NAME = "prod";
    public static final String AWS_FUNCTION_PREFIX = "jcs-lambda";
}
