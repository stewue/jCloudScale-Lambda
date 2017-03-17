package com.demo;


public class AspectJSample {
    public static void main ( String [] args ){
        Calculator calculator = new Calculator();
        System.out.println( "Result: " + calculator.doSometing() );
        System.out.println(  calculator.sub( 10, 20 ).d );
    }
}
