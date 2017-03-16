package com.test;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;

public class Calculator {
    @CloudMethod
    public int add( int a, int b ){
        return a + b;
    }
}
