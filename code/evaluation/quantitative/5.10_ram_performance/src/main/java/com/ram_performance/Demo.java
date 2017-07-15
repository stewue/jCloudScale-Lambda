package com.ram_performance;

import ch.uzh.ifi.seal.jcs_lambda.annotations.ByReference;
import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;
import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.byReference.JcsMessageQueue;

public class Demo
{
    @StartUp
    public static void main ( String [] args ){
        Demo demo = new Demo();
        demo.a = 7;

        System.out.println( demo.run() );
    }

    @ByReference
    public int a;

    @CloudMethod( timeout = 30, memory = 1536 )
    public String run(){
        System.out.println( a );
        return "Run code in cloud";
    }
}
