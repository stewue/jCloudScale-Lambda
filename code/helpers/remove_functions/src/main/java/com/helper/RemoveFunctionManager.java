package com.helper;

import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.lambdaFunction.AmazonLambda;

public class RemoveFunctionManager
{
    public static void main ( String [] args ){
        AmazonLambda amazonLambda = AmazonLambda.getInstance();
        amazonLambda.removeAllFunctions();
    }
}
