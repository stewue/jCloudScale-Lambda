package com.example;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.ReadOnly;
import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;

public class Demo2
{
    @StartUp(deployToCloud = false)
    public static void main ( String [] args ){
        Demo2 demo = new Demo2();
        demo.a = rand();

        System.out.println( demo.run() );
    }

    @ReadOnly
    public int a;

    @CloudMethod
    public int run(){
        return 2 * a;
    }

    private static int rand(){
        return (int) (Math.random() * 20);
    }
}
