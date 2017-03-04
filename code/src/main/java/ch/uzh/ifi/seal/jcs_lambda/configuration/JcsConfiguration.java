package ch.uzh.ifi.seal.jcs_lambda.configuration;

import com.amazonaws.regions.Regions;

public class JcsConfiguration {
    public static final String AWS_ACCESS_KEY_ID = "HERE ACCESS KEY ID";
    public static final String AWS_SECRET_KEY_ID = "HERE SECRET KEY ID";
    public static final Regions AWS_REGION = Regions.EU_CENTRAL_1;
    public static final int AWS_TIMEOUT = 3;
    public static final int AWS_DEFAULT_MEMORY_SIZE = 128;
    public static final String AWS_ROLE_NAME = "JCS-Lambda";
}
