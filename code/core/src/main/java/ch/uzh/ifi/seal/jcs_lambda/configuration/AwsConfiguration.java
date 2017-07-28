package ch.uzh.ifi.seal.jcs_lambda.configuration;

import com.amazonaws.regions.Regions;

public class AwsConfiguration {
    public static final Regions AWS_REGION = Regions.EU_CENTRAL_1;
    public static final int AWS_TIMEOUT = 10;
    public static final int AWS_DEFAULT_MEMORY_SIZE = 1536;

    public static final String AWS_ROLE_ARN = "HERE ROLE ARN";
    public static final String AWS_QUEUE_URL = "HERE URL OF THE SQS QUEUE";

    public static final String AWS_BUCKET_PREFIX = "jcs-lambda-";
    public static final String AWS_API_GATEWAY_NAME = "JCS Lambda REST API";
    public static final String AWS_API_GATEWAY_STAGE_NAME = "prod";
    public static final String AWS_FUNCTION_PREFIX = "jcs-lambda-";
}
