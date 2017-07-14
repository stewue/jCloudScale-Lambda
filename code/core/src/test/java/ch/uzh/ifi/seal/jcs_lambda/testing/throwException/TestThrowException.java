package ch.uzh.ifi.seal.jcs_lambda.testing.throwException;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;
import ch.uzh.ifi.seal.jcs_lambda.exception.CloudRuntimeException;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
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
        Logger.info("Cloud only");

        if( a > 0 || b > 0 ){
            throw new RuntimeException();
        }

        return a + b;
    }
}
