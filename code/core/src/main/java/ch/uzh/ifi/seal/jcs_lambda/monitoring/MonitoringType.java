package ch.uzh.ifi.seal.jcs_lambda.monitoring;

public enum MonitoringType {
    TOTAL_STARTUP,
    AWS_INIT,
    CODE_MODIFICATION,
    MAVEN,
    UPLOAD,
    CONFIGURE_ENDPOINTS,
    ENDPOINT_AVAILABLE,
    REMOVE_BUCKET,

    TOTAL_RUNTIME,
    REQUEST,
    SERIALIZING,
    NETWORKING,
    COMPUTING,
    DESERIALIZING,
    RESPONSE;
}
