package com.example;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;

public class Demo
{
    @StartUp
    public static void main ( String [] args ){
        Demo demo = new Demo();

        System.out.println( demo.run78( 99 ) );
    }

    @CloudMethod
    public String run78( int a ){
        return "Run code in cloud iiiiiiiiiiiii" + a;
    }
}
