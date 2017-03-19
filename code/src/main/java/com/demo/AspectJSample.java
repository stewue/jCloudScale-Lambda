package com.demo;

import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;

import java.util.Scanner;

public class AspectJSample {
    @StartUp
    public static void main ( String [] args ){
        Calculator calculator = new Calculator();

        int numberA = 0;
        int numberB = 0;

        Scanner in = new Scanner(System.in);

        System.out.println("Enter the first number:");
        numberA = in.nextInt();

        System.out.println("Enter the second number:");
        numberB = in.nextInt();

        System.out.println( "Result (add): " + calculator.doSometing( numberA, numberB ) );
        System.out.println( "Result (add2): " + calculator.add2( numberA, numberB ) );
        System.out.println( "Result (sub): " + calculator.sub( numberA, numberB ) );
    }
}
