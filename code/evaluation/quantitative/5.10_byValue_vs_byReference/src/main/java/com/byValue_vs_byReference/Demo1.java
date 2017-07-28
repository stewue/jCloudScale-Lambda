package com.byValue_vs_byReference;

import ch.uzh.ifi.seal.jcs_lambda.annotations.ByReference;
import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;

public class Demo1
{
    @StartUp
    public static void main ( String [] args ){
        Demo1 demo = new Demo1();
        demo.a = rand();

        System.out.println( demo.run() );
    }

    @ByReference
    public int a;

    @CloudMethod( memory = 1536, timeout = 10 )
    public int run(){
        return 2 * a;
    }

    private static int rand(){
        return (int) (Math.random() * 20);
    }
}
