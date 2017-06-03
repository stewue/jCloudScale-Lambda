package com.numberOfFunctions;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;

public class Demo
{
    @StartUp(deployToCloud = false)
    public static void main ( String [] args ){
        Demo demo = new Demo();
    }

    @CloudMethod
    public String run1671040845(){
        return "Run code in cloud";
    }
    @CloudMethod
    public String run1963810480(){
        return "Run code in cloud";
    }
}

