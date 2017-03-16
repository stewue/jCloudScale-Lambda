package com.test;

public class AspectJSample {
    public static void main ( String [] args ){
        Calculator calculator = new Calculator();
        System.out.println( "Result: " + calculator.add( 10, 20 ) );
    }
}
