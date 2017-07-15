package com.example;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.lambdaFunction.AmazonLambda;

public class Demo
{
    public static void main ( String [] args ){
        AmazonLambda amazonLambda = AmazonLambda.getInstance();
        amazonLambda.removeAllFunctions();
    }
}
