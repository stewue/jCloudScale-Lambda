package ch.uzh.ifi.seal.jcs_lambda.testing.byReferenceValue;

import ch.uzh.ifi.seal.jcs_lambda.annotations.ByReference;
import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;
import ch.uzh.ifi.seal.jcs_lambda.annotations.ReadOnly;
import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;

public class TestObject {
    @ReadOnly
    private int a;

    @ByReference
    private Complex b;

    public void initialize( int a, Complex b){
        this.a = a;
        this.b = b;
    }

    @CloudMethod( timeout = 15, memory = 512 )
    public int doSomething(){
        Logger.info("Cloud only");

        // work
        b = new Complex();

        // doesn't work
        b.y = "TEST STRING";

        return a * 2;
    }
}
