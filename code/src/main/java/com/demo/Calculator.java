package com.demo;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;

public class Calculator {

    // TODO a public default constructor is necessary, fix it with code transformation or so
    public Calculator(){}

    public int doSometing( int a, int b ){
        return add( a, b );
    }

    @CloudMethod
    private int add( int a, int b ){
        return b + a;
    }

    @CloudMethod
    public int add2( int a, int b ){
        return a + b + returnInt();
    }

    private int returnInt (){
        return 7;
    }

    @CloudMethod
    public int sub( int a, int b ){
        return a - b;
    }
/*
    @CloudMethod
    public OutObject complex( InObject inObject ){
        OutObject outObject = new OutObject();
        outObject.setE( inObject.getA() + inObject.getB() );
        outObject.setF( inObject.getC() + "###" );

        return outObject;
    }*/
}