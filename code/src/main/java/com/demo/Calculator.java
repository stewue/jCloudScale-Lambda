package com.demo;

import ch.uzh.ifi.seal.jcs_lambda.annotations.*;

public class Calculator {
    public int doSometing(){
        return add( 3, 7 );
    }

    @CloudMethod
    private int add( int a, int b ){
        return a + b;
    }

    public ReturnObj sub (int a, int b ){
        ReturnObj returnObj = new ReturnObj();
        returnObj.c = 3;
        returnObj.d = "aaaaa";
        return returnObj;
    }
}
