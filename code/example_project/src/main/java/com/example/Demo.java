package com.example;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;

public class Demo
{
    @StartUp
    public static void main ( String [] args ){
        Demo demo = new Demo();

        System.out.println( demo.run() );
    }

    @CloudMethod
    public String run(){
        return "Run code in cloud";
    }
}
