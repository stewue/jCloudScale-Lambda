package ch.uzh.ifi.seal.jcs_lambda.testing.asynchronous;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;

public class TestObject {
    @CloudMethod
    public int sum( int a, int b ) throws Exception{
        Logger.info("Cloud only");
        return a + b;
    }
}
