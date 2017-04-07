package ch.uzh.ifi.seal.jcs_lambda.testing.primitiv;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;
import org.junit.Assert;
import org.junit.Test;

public class TestPrimitiveTypes {

    @Test
    @StartUp
    public void test(){

        int a = 5;
        int b = 7;

        int result = sum( a, b );

        Assert.assertEquals( result, a + b );
    }

    @CloudMethod
    private int sum( int a, int b ){
        System.out.println("Cloud only");

        return a + b;
    }
}
