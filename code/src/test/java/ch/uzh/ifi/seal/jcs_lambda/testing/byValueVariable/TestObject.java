package ch.uzh.ifi.seal.jcs_lambda.testing.byValueVariable;

import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.Local;
import ch.uzh.ifi.seal.jcs_lambda.annotations.ReadOnly;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;

public class TestObject {
    @Local
    public static int a = 10;

    @ReadOnly
    public static int b = 7;

    @ReadOnly
    private int c;

    public void initialize ( int a, int c ){
        this.a = a;
        this.c = c;
    }

    @CloudMethod
    public int sum( int d ){
        Logger.info("Cloud only");
        return a + b + c + d;
    }
}
