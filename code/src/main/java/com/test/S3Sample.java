package com.test;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.aws.AwsCloudProvider;

import java.io.File;

public class S3Sample {

    public static void main(String[] args) {
        AwsCloudProvider awsCloudProvider = new AwsCloudProvider();

        File file = new File("out/artifacts/ch_uzh_ifi_seal_jcs_lambda_jar/ch.uzh.ifi.seal.jcs_lambda.jar" );
        String handlerName = "com.test.LambdaFunctionHandler";
        String functionName = "TEST";

        awsCloudProvider.registerMethod( functionName, handlerName, file );
    }

}