package ch.uzh.ifi.seal.jcs_lambda.testing.throwException;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;
import ch.uzh.ifi.seal.jcs_lambda.exception.CloudRuntimeException;
import org.junit.Assert;
import org.junit.Test;

public class TestThrowException {

    @Test( expected = CloudRuntimeException.class )
    @StartUp
    public void test(){

        int a = 5;
        int b = 7;

         sum( a, b );
    }

    @CloudMethod
    private int sum( int a, int b ){
        System.out.println("Cloud only");

        if( a > 0 || b > 0 ){
            throw new RuntimeException();
        }
        Exception e = new Exception();

        StackTraceElement [] exceptionStackTrace = e.getStackTrace();

        return a + b;
    }
}
